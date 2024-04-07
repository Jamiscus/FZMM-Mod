package fzmm.zailer.me.client.gui.item_editor.common.block_list.components;

import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.sort.AbstractSortOverlay;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortComponent;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortEditor;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockListSortOverlay extends AbstractSortOverlay<Block, AbstractBlockListBuilder.BlockOrTagData, AbstractBlockListBuilder> {
    public BlockListSortOverlay(ISortEditor editor, AbstractBlockListBuilder builder) {
        super(editor, builder);
    }

    @Override
    protected ISortComponent<Block, AbstractBlockListBuilder.BlockOrTagData> getComponent(AbstractBlockListBuilder.BlockOrTagData value,
                                                                                          ISortEditor editor,
                                                                                          AbstractBlockListBuilder builder) {
        return new SortBlockComponent(value.getBlocks(), value.getBlockStr());
    }

    @Override
    protected List<EnumWidget> getSorters(FlowLayout layout) {
        List<EnumWidget> components = new ArrayList<>();

        EnumWidget alphabetic = this.getEnumWidget("fzmm.gui.itemEditor.block_list.option.sort.alphabetic");
        alphabetic.init(SortOption.DISABLE);
        alphabetic.onPress(buttonComponent -> this.sorterExecute(alphabetic, this::sortAlphabetic));

        components.add(alphabetic);

        return components;
    }

    private void sortAlphabetic(SortOption option) {
        Comparator<AbstractBlockListBuilder.BlockOrTagData> comparator = Comparator.comparing(AbstractBlockListBuilder.BlockOrTagData::getBlockStr);

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.valueList.sort(comparator);
    }
}
