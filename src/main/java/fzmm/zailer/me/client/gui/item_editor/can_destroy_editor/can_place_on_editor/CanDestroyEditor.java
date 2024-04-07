package fzmm.zailer.me.client.gui.item_editor.can_destroy_editor.can_place_on_editor;

import fzmm.zailer.me.builders.CanDestroyBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.BlockListEditor;
import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class CanDestroyEditor extends BlockListEditor {
    private RequestedItem blockRequested = null;
    private List<RequestedItem> requestedItems = null;
    private final CanDestroyBuilder builder = CanDestroyBuilder.builder();

    @Override
    public List<RequestedItem> getRequestedItems() {

        if (this.requestedItems != null)
            return this.requestedItems;

        this.blockRequested = new RequestedItem(
                itemStack -> !itemStack.isEmpty(),
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.label.anyItem"),
                true
        );

        this.requestedItems = List.of(this.blockRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.DIAMOND_PICKAXE.getDefaultStack();
    }

    @Override
    public String getId() {
        return "can_destroy";
    }

    @Override
    public AbstractBlockListBuilder getBuilder() {
        return this.builder;
    }

    @Override
    public RequestedItem getRequestedItem() {
        return this.blockRequested;
    }
}
