package fzmm.zailer.me.client.gui.item_editor.item_select;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemSelectEditor implements IItemEditorScreen {
    private RequestedItem stackRequested = null;
    private List<RequestedItem> requestedItems = null;
    private FlowLayout contentLayout;
    private ItemStack stack;
    private FlowLayout groupsItemsLayout;
    private FlowLayout groupContentLayout;
    private List<ItemComponent> groupItems;
    private TextBoxComponent searchBox;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.stackRequested = new RequestedItem(
                stack -> true,
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.label.anyItem"),
                true
        );

        this.requestedItems = List.of(this.stackRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.DIAMOND.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        this.stack = ItemStack.EMPTY.copy();
        this.groupsItemsLayout = null;
        this.groupItems = new ArrayList<>();

        this.contentLayout = editorLayout.childById(FlowLayout.class, "content");
        BaseFzmmScreen.checkNull(this.contentLayout, "flow-layout", "content");

        return editorLayout;
    }

    @Override
    public String getId() {
        return "item_select";
    }

    @Override
    public void updateItemPreview() {
        this.stackRequested.setStack(this.stack);
        this.stackRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.stack = stack;
        this.selectItem(stack.getItem());
    }

    public void selectItem(Item item) {
        ItemStack newStack = item.getDefaultStack();
        int count = this.stack.getCount();
        newStack.setCount(count <= 0 ? 1 : count);
        newStack.setNbt(this.stack.getNbt());

        this.stack = newStack;

        this.updateItemList();
    }

    public void updateItemList() {
        if (this.stack.isEmpty()) {
            this.contentLayout.clearChildren();
            this.contentLayout.child(this.getGroupsItemsLayout());
            return;
        }

        List<Class<?>> itemClasses = this.getItemClass(this.stack.getItem());
        List<FlowLayout> layoutList = new ArrayList<>();
        List<Item> similarItems = new ArrayList<>();

        for (int i = 0; i != itemClasses.size(); i++) {
            Class<?> itemClass = itemClasses.get(i);
            this.getLayout(i, itemClass, similarItems).ifPresent(layoutList::add);
        }

        layoutList.add(this.getGroupsItemsLayout());


        this.contentLayout.clearChildren();
        this.contentLayout.children(layoutList);
    }

    private Optional<FlowLayout> getLayout(int index, Class<?> itemClass, List<Item> similarItems) {
        FlowLayout layout = Containers.verticalFlow(Sizing.content(), Sizing.content());
        layout.padding(Insets.of(6));
        layout.surface(Surface.DARK_PANEL);
        layout.horizontalAlignment(HorizontalAlignment.LEFT);

        FlowLayout itemsLayout = Containers.ltrTextFlow(Sizing.expand(100), Sizing.content());
        List<? extends Component> similarItemsLayout = this.getSimilarItemsLayout(itemClass, similarItems);
        if (similarItemsLayout.isEmpty())
            return Optional.empty();

        itemsLayout.children(similarItemsLayout);

        layout.child(Components.label(Text.translatable("fzmm.gui.itemEditor.item_select.label.similarItems", index + 1)));
        layout.child(itemsLayout);

        return Optional.of(layout);
    }

    private List<? extends Component> getSimilarItemsLayout(Class<?> itemClass, List<Item> similarItems) {
        List<Component> layoutList = new ArrayList<>();

        if (itemClass == BlockItem.class)
            return layoutList;

        for (var item : Registries.ITEM.stream().toList()) {
            Class<?> clazz = item.getClass();
            boolean isSimilar = itemClass.isInstance(item) || (itemClass.isInterface() && Arrays.stream(clazz.getInterfaces())
                    .anyMatch(aClass -> aClass == itemClass));

            if (isSimilar && !similarItems.contains(item)) {

                layoutList.add(this.getItemComponent(item));
                similarItems.add(item);
            }
        }

        return layoutList;
    }

    private FlowLayout getGroupsItemsLayout() {
        if (this.groupsItemsLayout != null)
            return this.groupsItemsLayout;

        FlowLayout result = Containers.verticalFlow(Sizing.content(), Sizing.content());
        result.gap(4);
        result.padding(Insets.of(6));
        result.surface(Surface.DARK_PANEL);

        this.searchBox = Components.textBox(Sizing.fixed(150), "");
        this.searchBox.onChanged().subscribe(value -> this.groupSearch());

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;

        ItemGroups.updateDisplayContext(player.networkHandler.getEnabledFeatures(), true, player.getWorld().getRegistryManager());

        this.groupContentLayout = Containers.ltrTextFlow(Sizing.expand(100), Sizing.content());

        FlowLayout groupLayout = Containers.ltrTextFlow(Sizing.expand(100), Sizing.content());
        List<ButtonComponent> groupButtonList = new ArrayList<>();

        for (var group : ItemGroups.getGroups()) {
            if (!group.getDisplayStacks().isEmpty())
                groupLayout.child(this.getGroupButton(group, this.groupContentLayout, groupButtonList));
        }

        groupButtonList.get(0).onPress();

        result.child(this.searchBox);
        result.child(groupLayout);
        result.child(this.groupContentLayout);

        this.groupsItemsLayout = result;

        return result;
    }

    @NotNull
    private Component getGroupButton(ItemGroup group, FlowLayout content, List<ButtonComponent> groupButtonList) {
        StackLayout stackLayout = Containers.stack(Sizing.fixed(20), Sizing.fixed(20));
        stackLayout.margins(Insets.right(4));
        stackLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        ButtonComponent groupButton = Components.button(Text.empty(), button -> {
        });
        groupButton.onPress(button -> {
            this.updateGroupItems(content, group.getDisplayStacks().stream()
                    .map(ItemStack::getItem)
                    .distinct()
                    .toList()
            );

            for (var buttonComponent : groupButtonList)
                buttonComponent.active = true;

            groupButton.active = false;
        });
        groupButton.horizontalSizing(Sizing.fixed(20));

        stackLayout.child(groupButton);
        stackLayout.child(Components.item(group.getIcon())
                .tooltip(group.getDisplayName())
                .cursorStyle(CursorStyle.HAND)
        );

        groupButtonList.add(groupButton);
        return stackLayout;
    }

    private void updateGroupItems(FlowLayout content, Collection<Item> displayStacks) {
        content.clearChildren();
        List<ItemComponent> componentList = new ArrayList<>();
        for (var item : displayStacks)
            componentList.add(this.getItemComponent(item));

        this.groupItems.clear();
        this.groupItems.addAll(componentList);

        content.children(componentList);

        this.groupSearch();
    }

    private void groupSearch() {
        String value = this.searchBox.getText().toLowerCase();

        this.groupContentLayout.clearChildren();
        List<Component> items = new ArrayList<>();

        for (var item : this.groupItems) {
            if (item.stack().getName().getString().toLowerCase().contains(value))
                items.add(item);
        }

        this.groupContentLayout.children(items);
    }

    private ItemComponent getItemComponent(Item item) {
        ItemComponent itemComponent = Components.item(item.getDefaultStack());
        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.selectItem(item);
            this.updateItemPreview();
            return true;
        });
        itemComponent.margins(Insets.of(1));
        itemComponent.setTooltipFromStack(true);
        itemComponent.cursorStyle(CursorStyle.HAND);

        return itemComponent;
    }

    public List<Class<?>> getItemClass(Item item) {
        Class<?> clazz = item.getClass();

        List<Class<?>> itemClasses = new ArrayList<>(this.getInnerClasses(clazz));

        itemClasses = itemClasses.stream()
                .distinct()
                .toList();

        return itemClasses;
    }

    private List<Class<?>> getInnerClasses(Class<?> clazz) {
        List<Class<?>> innerClasses = new ArrayList<>();
        innerClasses.add(clazz);

        Class<?> superClass = clazz.getSuperclass();

        List<Class<?>> innerClassesList = new ArrayList<>(Arrays.stream(clazz.getInterfaces()).toList());
        if (superClass != null)
            innerClassesList.add(clazz.getSuperclass());

        for (var innerClass : innerClassesList) {
            if (innerClass == Item.class)
                return innerClasses;

            innerClasses.addAll(this.getInnerClasses(innerClass));
        }

        return innerClasses;
    }
}
