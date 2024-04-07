package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.Items;

public class CanPlaceOnBuilder extends AbstractBlockListBuilder {

    private CanPlaceOnBuilder() {
        this.stack = Items.STONE.getDefaultStack();
    }

    public static CanPlaceOnBuilder builder() {
        return new CanPlaceOnBuilder();
    }

    @Override
    public String getNbtKey() {
        return TagsConstant.CAN_PLACE_ON;
    }
}