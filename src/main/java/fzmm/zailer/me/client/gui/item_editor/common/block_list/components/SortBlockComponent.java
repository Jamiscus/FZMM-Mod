package fzmm.zailer.me.client.gui.item_editor.common.block_list.components;

import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortComponent;
import net.minecraft.block.Block;

import java.util.List;

public class SortBlockComponent extends BlockOrTagComponent implements ISortComponent<Block, AbstractBlockListBuilder.BlockOrTagData> {

    public SortBlockComponent(List<Block> values, String tag) {
        super(values, tag);
    }

    @Override
    public AbstractBlockListBuilder.BlockOrTagData value() {
        return new AbstractBlockListBuilder.BlockOrTagData(this.values, this.identifier);
    }

    @Override
    public ISortComponent<Block, AbstractBlockListBuilder.BlockOrTagData> getValue() {
        return new SortBlockComponent(this.values, this.identifier);
    }

    @Override
    public void setValue(ISortComponent<Block, AbstractBlockListBuilder.BlockOrTagData> value) {
        SortBlockComponent valueCast = (SortBlockComponent) value;
        this.update(valueCast.values, valueCast.identifier);
    }
}
