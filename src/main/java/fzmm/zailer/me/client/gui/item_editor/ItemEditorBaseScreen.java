package fzmm.zailer.me.client.gui.item_editor;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.armor_editor.ArmorEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.block_state_editor.BlockStateEditor;
import fzmm.zailer.me.client.gui.item_editor.color_editor.ColorEditor;
import fzmm.zailer.me.client.gui.item_editor.effect_editor.EffectEditor;
import fzmm.zailer.me.client.gui.item_editor.enchant_editor.EnchantEditor;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.client.gui.utils.selectItem.SelectItemScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemEditorBaseScreen extends BaseFzmmScreen {
    private static final int APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR = 0xff7d7d7d;
    private static final int SELECTED_CATEGORY_BACKGROUND_COLOR = 0xff5da25f;
    private static final int NON_APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR = 0xff585858;
    private static final Text GIVE_ITEM_TEXT = Text.translatable("fzmm.gui.itemEditor.base.label.give");
    private static final String APPLICABLE_EDITORS_TEXT = "fzmm.gui.itemEditor.base.label.applicable_editors";
    private static final int BASE_PANEL_WIDTH = 200;
    private static final String BASE_PANEL_ID = "base-panel";
    private static final String REQUIRED_ITEMS_ID = "required-items";
    private static final String APPLICABLE_EDITORS_ID = "applicable-editors";
    private static final String APPLICABLE_EDITORS_LABEL_ID = "applicable-editors-label";
    private static final String NON_APPLICABLE_EDITORS_ID = "non-applicable-editors";
    private static final String CONTENT_ID = "content";
    private static Class<? extends IItemEditorScreen> selectedEditor = null;
    protected final List<IItemEditorScreen> itemEditorScreens;
    private ScrollContainer<?> basePanelLayout;
    private FlowLayout requiredItemsLayout;
    private FlowLayout applicableEditorsLayout;
    private FlowLayout nonApplicableEditorsLayout;
    private FlowLayout contentLayout;
    private LabelComponent applicableEditorsLabel;
    private IItemEditorScreen currentEditor;
    private ItemStack selectedItem;

    public ItemEditorBaseScreen(@Nullable Screen parent) {
        super("item_editor/base", "itemEditor", parent);
        this.itemEditorScreens = this.getItemEditorScreens();
    }

    private List<IItemEditorScreen> getItemEditorScreens() {
        List<IItemEditorScreen> itemEditorScreens = new ArrayList<>();

        itemEditorScreens.add(new ArmorEditorScreen());
        itemEditorScreens.add(new BannerEditorScreen());
        itemEditorScreens.add(new BlockStateEditor());
        itemEditorScreens.add(new ColorEditor());
        itemEditorScreens.add(new EffectEditor());
        itemEditorScreens.add(new EnchantEditor());

        return itemEditorScreens;
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;
        this.selectedItem = this.client.player.getMainHandStack().copy();

        this.basePanelLayout = rootComponent.childById(ScrollContainer.class, BASE_PANEL_ID);
        checkNull(this.basePanelLayout, "scroll", BASE_PANEL_ID);
        this.basePanelLayout.horizontalSizing(Sizing.fixed(BASE_PANEL_WIDTH));

        this.requiredItemsLayout = rootComponent.childById(FlowLayout.class, REQUIRED_ITEMS_ID);
        checkNull(this.requiredItemsLayout, "flow-layout", REQUIRED_ITEMS_ID);
        this.applicableEditorsLayout = rootComponent.childById(FlowLayout.class, APPLICABLE_EDITORS_ID);
        checkNull(this.applicableEditorsLayout, "flow-layout", APPLICABLE_EDITORS_ID);
        this.nonApplicableEditorsLayout = rootComponent.childById(FlowLayout.class, NON_APPLICABLE_EDITORS_ID);
        checkNull(this.nonApplicableEditorsLayout, "flow-layout", NON_APPLICABLE_EDITORS_ID);

        this.applicableEditorsLabel = rootComponent.childById(LabelComponent.class, APPLICABLE_EDITORS_LABEL_ID);
        checkNull(this.applicableEditorsLabel, "label", APPLICABLE_EDITORS_LABEL_ID);

        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);

        this.selectEditor();
    }

    private void selectEditor() {
        boolean stackEmpty = this.selectedItem.isEmpty();
        if (selectedEditor != null) {
            for (var editor : this.itemEditorScreens) {
                if (editor.getClass() == selectedEditor && editor.isApplicable(this.selectedItem) || stackEmpty) {
                    this.selectEditor(editor);
                    return;
                }
            }
        }

        for (var editor : this.itemEditorScreens) {
            if (editor.isApplicable(this.selectedItem) || stackEmpty) {
                this.selectEditor(editor);
                return;
            }
        }

        this.selectEditor(this.itemEditorScreens.get(0));
    }

    public void selectEditor(IItemEditorScreen editor) {
        selectedEditor = editor.getClass();

        // We make a copy of the selected item to prevent it from being overwritten
        // by an editor in case the editor calls editor#updateItemPreview before calling editor#selectItemAndUpdateParameters
        ItemStack selectedItemCopy = this.selectedItem.copy();

        this.currentEditor = editor;
        this.contentLayout.clearChildren();
        List<RequestedItem> requestedItems = this.currentEditor.getRequestedItems();
        Optional<FlowLayout> editorLayoutOptional = this.currentEditor.getLayoutModel(
                this.basePanelLayout.x() + this.basePanelLayout.width(),
                this.basePanelLayout.y(),
                this.width,
                this.height
        );

        boolean failedGettingLayout = false;
        try {
            FlowLayout editorLayout = editorLayoutOptional.orElseThrow();
            editorLayout = this.currentEditor.getLayout(this, editorLayout);
            this.contentLayout.child(editorLayout);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[ItemEditorBaseScreen] Failed to get editor layout", e);
            this.addErrorMessage(Text.translatable("fzmm.gui.itemEditor.label.error.editorLayout"));
            failedGettingLayout = true;
        }

        this.updateRequestedItemsComponents(requestedItems);

        if (failedGettingLayout)
            return;

        this.selectItemAndUpdateParameters(selectedItemCopy);
        this.selectedItem = selectedItemCopy;

        editor.updateItemPreview();
    }

    private void selectItemAndUpdateParameters(ItemStack stack) {
        try {
            this.currentEditor.selectItemAndUpdateParameters(stack);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[ItemEditorBaseScreen] Failed to select item", e);
            this.updateEditorsComponents();
            Text message = Text.translatable("fzmm.gui.itemEditor.label.error.selectItem",
                    stack.getName(), Registries.ITEM.getId(stack.getItem()).toString());

            this.addErrorMessage(message);
        }
    }

    private void addErrorMessage(Text message) {
        message = message.copy().setStyle(Style.EMPTY.withColor(0xD83F27));

        LabelComponent label = Components.label(message);
        label.horizontalSizing(Sizing.fill(100));
        FlowLayout errorLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        errorLayout.padding(Insets.of(10));
        errorLayout.surface(Surface.DARK_PANEL);
        errorLayout.child(label);

        this.contentLayout.clearChildren();
        this.contentLayout.child(errorLayout);
    }

    private void updateRequestedItemsComponents(List<RequestedItem> requestedItemList) {
        assert this.client != null;
        this.requiredItemsLayout.clearChildren();
        List<Component> componentList = new ArrayList<>();

        for (var requestedItem : requestedItemList) {

            FlowLayout layout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            layout.gap(2);

            ItemComponent itemComponent = Components.item(ItemStack.EMPTY).setTooltipFromStack(true);
            layout.child(itemComponent);
            layout.verticalAlignment(VerticalAlignment.CENTER);

            ButtonComponent selectItemButton = Components.button(Text.empty(),
                    button -> this.client.setScreen(new SelectItemScreen(this, List.of(requestedItem), (itemStack) -> this.updateEditorsComponents())));
            selectItemButton.horizontalSizing(Sizing.fixed(100));
            layout.child(selectItemButton);

            ButtonComponent giveButton = Components.button(GIVE_ITEM_TEXT, button -> FzmmUtils.giveItem(requestedItem.stack()));
            giveButton.horizontalSizing(Sizing.fixed(30));
            layout.child(giveButton);

            requestedItem.setUpdatePreviewConsumer(itemStack -> this.updatePreviewExecute(itemStack, requestedItemList, requestedItem, itemComponent, selectItemButton));

            ItemStack stack = requestedItem.stack();
            this.updateRequestedItemButton(selectItemButton, requestedItem, stack);
            itemComponent.stack(stack);

            componentList.add(layout);
        }

        this.requiredItemsLayout.children(componentList);
    }

    private void updatePreviewExecute(ItemStack itemStack, List<RequestedItem> requestedItemList,
                                      RequestedItem requestedItem, ItemComponent itemComponent, ButtonComponent selectItemButton) {
        boolean firstItem = true;
        for (int i = 0; i != requestedItemList.size(); i++) {
            RequestedItem entry = requestedItemList.get(i);
            if (firstItem && (entry == requestedItem || (requestedItemList.size() - 1) == i)) {
                this.selectedItem = itemStack;
                break;
            } else if (!entry.isEmpty()) {
                firstItem = false;
            }
        }

        this.updateRequestedItem(itemStack, requestedItem, itemComponent, selectItemButton);
    }

    public void updateRequestedItem(ItemStack stack, RequestedItem requestedItem, ItemComponent itemComponent, ButtonComponent selectItemButton) {
        itemComponent.stack(stack);
        this.updateRequestedItemButton(selectItemButton, requestedItem, stack);
        this.updateEditorsComponents();
    }

    private void updateRequestedItemButton(ButtonComponent selectItemButton, RequestedItem requestedItem, ItemStack stack) {
        selectItemButton.setMessage(
                requestedItem.predicate().test(stack) ?
                        requestedItem.title() :
                        requestedItem.title().copy().setStyle(Style.EMPTY.withColor(0xD83F27)).append(" ⚠")
        );
    }

    private void updateEditorsComponents() {
        this.applicableEditorsLabel.text(Text.translatable(APPLICABLE_EDITORS_TEXT, this.selectedItem.getItem().getName().getString()));

        this.updateEditorsComponents(this.applicableEditorsLayout, this.filterEditors(true), true);
        this.updateEditorsComponents(this.nonApplicableEditorsLayout, this.filterEditors(false), false);
    }

    public void updateEditorsComponents(FlowLayout layout, List<IItemEditorScreen> applicableEditors, boolean applicable) {
        assert this.client != null;
        layout.clearChildren();
        List<Component> componentList = new ArrayList<>();

        for (var applicableEditor : applicableEditors)
            componentList.add(this.getEditorRow(applicableEditor, applicable, applicableEditor == this.currentEditor));

        layout.children(componentList);
    }

    public FlowLayout getEditorRow(IItemEditorScreen itemEditorScreen, boolean isApplicable, boolean isSelected) {
        FlowLayout layout = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        layout.gap(2);
        layout.verticalAlignment(VerticalAlignment.CENTER);

        layout.child(Components.item(itemEditorScreen.getExampleItem()));
        layout.child(Components.label(itemEditorScreen.getTitle()));

        int backgroundColor = NON_APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR;

        if (isSelected)
            backgroundColor = SELECTED_CATEGORY_BACKGROUND_COLOR;
        else if (isApplicable)
            backgroundColor = APPLICABLE_EDITORS_CATEGORY_BACKGROUND_COLOR;

        layout.surface(Surface.flat(backgroundColor));

        layout.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (itemEditorScreen.getClass() != selectedEditor) {
                this.selectEditor(itemEditorScreen);
                return true;
            }
            return false;
        });


        return layout;
    }

    private List<IItemEditorScreen> filterEditors(boolean isApplicable) {
        List<IItemEditorScreen> filteredEditors = new ArrayList<>();

        for (var itemEditorScreen : this.itemEditorScreens) {
            if (itemEditorScreen.isApplicable(this.selectedItem) == isApplicable) {
                filteredEditors.add(itemEditorScreen);
            }
        }

        return filteredEditors;
    }

    @Override
    public String getBaseScreenTranslationKey() {
        return super.getBaseScreenTranslationKey() + "." + this.currentEditor.getId();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.currentEditor.keyPressed(keyCode, scanCode, modifiers))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}