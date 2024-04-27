package fzmm.zailer.me.client.gui.item_editor.can_place_on_editor;

import fzmm.zailer.me.builders.CanPlaceOnBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.BlockListEditor;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class CanPlaceOnEditor extends BlockListEditor {
    private RequestedItem blockRequested = null;
    private List<RequestedItem> requestedItems = null;
    private final CanPlaceOnBuilder builder = CanPlaceOnBuilder.builder();

    @Override
    public List<RequestedItem> getRequestedItems() {

        if (this.requestedItems != null)
            return this.requestedItems;

        this.blockRequested = new RequestedItem(
                itemStack -> itemStack.getItem() instanceof BlockItem,
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.can_place_on.item"),
                true
        );

        this.requestedItems = List.of(this.blockRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.GRASS_BLOCK.getDefaultStack();
    }

    @Override
    public String getId() {
        return "can_place_on";
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
