package fzmm.zailer.me.client.gui.item_editor.common.block_list;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ScrollableButtonComponent;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.components.BlockListSortOverlay;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.components.BlockOrTagComponent;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortEditor;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.*;

public abstract class BlockListEditor implements IItemEditorScreen, ISortEditor {
    private TextBoxComponent searchTextBox;
    private FlowLayout appliedBlocksLayout;
    protected FlowLayout availableBlocksLayout;
    private List<BlockOrTagComponent> appliedBlocksComponents;
    protected ButtonComponent selectedCategoryButton;
    private ButtonComponent sortButton;

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.appliedBlocksComponents = new ArrayList<>();

        // top buttons
        Icon sortIcon = Icon.of(Items.HOPPER);
        this.sortButton = editorLayout.childById(ButtonComponent.class, "sort-blocks");
        BaseFzmmScreen.checkNull(this.sortButton, "button", "sort-blocks");
        this.sortButton.onPress(buttonComponent -> this.addSortOverlay((FlowLayout) editorLayout.root()));
        this.sortButton.horizontalSizing(Sizing.fixed(20));
        this.sortButton.renderer((context, button, delta) -> {
            ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
            sortIcon.render(context, button.x() + 2, button.y() + 2, 0, 0, delta);
        });

        // filters
        this.searchTextBox = editorLayout.childById(TextBoxComponent.class, "search");
        BaseFzmmScreen.checkNull(this.searchTextBox, "text-box", "search");
        this.searchTextBox.onChanged().subscribe(value -> {
            this.selectedCategoryButton.onPress();
            this.updateAppliedValues();
        });

        // categories
        FlowLayout categoriesLayout = editorLayout.childById(FlowLayout.class, "categories");
        BaseFzmmScreen.checkNull(categoriesLayout, "flowLayout", "categories");

        // content
        this.appliedBlocksLayout = editorLayout.childById(FlowLayout.class, "applied-blocks");
        BaseFzmmScreen.checkNull(this.appliedBlocksLayout, "flowLayout", "applied-blocks");

        // content button
        ButtonComponent removeAllButton = editorLayout.childById(ButtonComponent.class, "remove-all");
        BaseFzmmScreen.checkNull(removeAllButton, "button", "remove-all");
        removeAllButton.onPress(buttonComponent -> {
            this.getBuilder().clear();
            this.appliedBlocksLayout.clearChildren();
            this.selectedCategoryButton.onPress();
            this.updateItemPreview();
        });

        // available
        this.availableBlocksLayout = editorLayout.childById(FlowLayout.class, "add-blocks-layout");
        BaseFzmmScreen.checkNull(this.availableBlocksLayout, "flowLayout", "add-blocks-layout");

        // available button
        ButtonComponent addAllButton = editorLayout.childById(ButtonComponent.class, "add-all");
        BaseFzmmScreen.checkNull(addAllButton, "button", "add-all");
        addAllButton.onPress(buttonComponent -> {
            List<Component> children = this.availableBlocksLayout.children();
            for (var blockComponent : List.copyOf(this.availableBlocksLayout.children())) {
                if (children.contains(blockComponent))
                    ((BlockOrTagComponent) blockComponent).ignoreUpdatePreviewExecute();
            }

            this.updateItemPreview();
        });

        this.updateAppliedValues();
        categoriesLayout.children(this.getCategories());

        return editorLayout;
    }

    @Override
    public String getUIModelId() {
        return "block_list";
    }

    @Override
    public void updateItemPreview() {
        RequestedItem requestedItem = this.getRequestedItem();

        requestedItem.setStack(this.getBuilder().get());
        requestedItem.updatePreview();
        this.updateSortButton();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        AbstractBlockListBuilder builder = this.getBuilder();
        builder.of(stack);

        this.updateAppliedValues();
        this.selectedCategoryButton.onPress();
    }


    public abstract AbstractBlockListBuilder getBuilder();

    public abstract RequestedItem getRequestedItem();

    private Collection<ScrollableButtonComponent> getCategories() {
        List<ScrollableButtonComponent> result = new ArrayList<>();
        assert MinecraftClient.getInstance().world != null;
        Registry<Block> blockRegistry = MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.BLOCK);

        // all
        List<AbstractBlockListBuilder.BlockOrTagData> allBlocks = blockRegistry.stream()
                .filter(block -> block != Blocks.AIR)
                .map(block -> new AbstractBlockListBuilder.BlockOrTagData(blockRegistry, block))
                .toList();
        ScrollableButtonComponent allButton = new ScrollableButtonComponent(Text.translatable("fzmm.gui.button.category.all"),
                buttonComponent -> this.applyCategory(allBlocks, result, buttonComponent));
        result.add(allButton);
        allButton.onPress();

        // tags
        List<AbstractBlockListBuilder.BlockOrTagData> allTags = blockRegistry.streamTags()
                .map(blockTagKey -> new AbstractBlockListBuilder.BlockOrTagData(blockRegistry, blockTagKey)).toList();
        result.add(new ScrollableButtonComponent(Text.translatable("fzmm.gui.itemEditor.block_list.label.category.allTags"),
                buttonComponent -> this.applyCategory(allTags, result, buttonComponent)));

        // groups
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        ItemGroups.updateDisplayContext(player.networkHandler.getEnabledFeatures(), true, player.getWorld().getRegistryManager());
        for (var group : ItemGroups.getGroups()) {
            List<Block> blockList = new ArrayList<>();
            for (var stack : group.getDisplayStacks()) {
                if (stack.getItem() instanceof BlockItem block)
                    blockList.add(block.getBlock());
            }

            if (blockList.isEmpty())
                continue;

            List<AbstractBlockListBuilder.BlockOrTagData> blockOrTagData = blockList.stream()
                    .map(block -> new AbstractBlockListBuilder.BlockOrTagData(blockRegistry, block))
                    .toList();

            ScrollableButtonComponent groupButton = new ScrollableButtonComponent(group.getDisplayName(),
                    buttonComponent -> this.applyCategory(blockOrTagData, result, buttonComponent));

            result.add(groupButton);
        }

        // mods
        Set<String> modsIdSet = new HashSet<>();
        for (var identifier : blockRegistry.getIds()) {
            modsIdSet.add(identifier.getNamespace());
        }

        for (var modId : modsIdSet) {
            Text translation = modId.equals("minecraft") ?
                    Text.translatable("fzmm.gui.button.category.vanilla") :
                    Text.translatable("fzmm.gui.button.category.mod", modId);

            List<AbstractBlockListBuilder.BlockOrTagData> blockOrTagData = new ArrayList<>(allTags);
            blockOrTagData.addAll(allBlocks);

            String namespace = modId + ":";
            List<AbstractBlockListBuilder.BlockOrTagData> blockOrTagDataFinal = blockOrTagData.stream()
                    .filter(blockOrTagData1 -> {
                        String id = blockOrTagData1.getBlockStr();
                        return id.startsWith(namespace) || id.startsWith("#" + namespace);
                    }).toList();

            result.add(new ScrollableButtonComponent(translation, buttonComponent ->
                    this.applyCategory(blockOrTagDataFinal, result, buttonComponent)));
        }

        for (var button : result) {
            button.horizontalSizing(Sizing.fill(100));
            button.renderer(ButtonComponent.Renderer.flat(0x20000000, 0x40000000, 0x80000000));
        }

        return result;
    }

    protected void applyCategory(List<AbstractBlockListBuilder.BlockOrTagData> blockOrTagData,
                                 List<ScrollableButtonComponent> buttonList, ButtonComponent button) {
        for (var entry : buttonList)
            entry.active = true;

        button.active = false;
        this.selectedCategoryButton = button;

        List<BlockOrTagComponent> currentCategoryAddBlocksComponents = blockOrTagData.stream()
                .map(blockOrTagData1 -> this.getComponent(blockOrTagData1.getBlocks(), blockOrTagData1.getBlockStr(), true))
                .toList();

        List<BlockOrTagComponent> components = this.applyAddBlocksFilters(currentCategoryAddBlocksComponents);

        this.availableBlocksLayout.clearChildren();
        this.availableBlocksLayout.children(components);
        this.updateSortButton();
    }

    public void updateAppliedValues() {
        List<AbstractBlockListBuilder.BlockOrTagData> blockList = this.getBuilder().getBlockList();

        this.appliedBlocksComponents.clear();
        for (var blockOrTag : blockList) {
            BlockOrTagComponent component = this.getComponent(blockOrTag.getBlocks(), blockOrTag.getBlockStr(), false);
            this.appliedBlocksComponents.add(component);
        }

        this.applyAppliedBlocksFilters();
        this.updateSortButton();
    }

    protected BlockOrTagComponent getComponent(List<Block> blockList, String identifier, boolean isAdd) {
        return new BlockOrTagComponent(blockList, identifier, this, this.getBuilder(),
                blockOrTagComponent -> this.selectedCategoryButton.onPress(),
                blockOrTagComponent -> this.appliedBlocksLayout.child(blockOrTagComponent),
                isAdd);
    }

    private void applyAppliedBlocksFilters() {
        String search = this.searchTextBox.getText();

        List<Component> appliedBlocks = new ArrayList<>();
        for (var component : this.appliedBlocksComponents) {
            if (!component.filter(search))
                continue;

            appliedBlocks.add(component);
        }
        this.appliedBlocksLayout.clearChildren();
        this.appliedBlocksLayout.children(appliedBlocks);
    }

    private List<BlockOrTagComponent> applyAddBlocksFilters(List<BlockOrTagComponent> currentCategoryAddBlocksComponents) {
        List<BlockOrTagComponent> blockOrTagComponentList = new ArrayList<>();
        String search = this.searchTextBox.getText();
        AbstractBlockListBuilder builder = this.getBuilder();

        for (var component : currentCategoryAddBlocksComponents) {
            if (!component.filter(search))
                continue;

            if (builder.contains(component.identifier()))
                continue;

            blockOrTagComponentList.add(component);
        }
        return blockOrTagComponentList;
    }

    private void updateSortButton() {
        this.sortButton.active = this.getBuilder().values().size() > 1;
    }


    private void addSortOverlay(FlowLayout rootComponent) {
        OverlayContainer<FlowLayout> sortOverlay = new BlockListSortOverlay(this, this.getBuilder());
        sortOverlay.zIndex(300);
        rootComponent.child(sortOverlay);
    }
}
