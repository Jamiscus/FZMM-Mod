package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.Items;

public class CanDestroyBuilder extends AbstractBlockListBuilder {

    private CanDestroyBuilder() {
        this.stack = Items.STONE.getDefaultStack();
    }

    public static CanDestroyBuilder builder() {
        return new CanDestroyBuilder();
    }

    @Override
    public String getNbtKey() {
        return TagsConstant.CAN_DESTROY;
    }
}