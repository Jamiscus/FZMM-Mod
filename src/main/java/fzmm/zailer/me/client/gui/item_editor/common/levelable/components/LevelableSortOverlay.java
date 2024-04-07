package fzmm.zailer.me.client.gui.item_editor.common.levelable.components;

import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable.SortLevelableComponent;
import fzmm.zailer.me.client.gui.item_editor.common.sort.AbstractSortOverlay;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortComponent;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortEditor;
import io.wispforest.owo.ui.container.FlowLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LevelableSortOverlay<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>> extends AbstractSortOverlay<V, D, B> {

    public LevelableSortOverlay(LevelableEditor<V, D, B> editor, B builder) {
        super(editor, builder);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ISortComponent<V, D> getComponent(D value, ISortEditor editor, B builder) {
        return new SortLevelableComponent<>(value, (LevelableEditor<V, D, ? super B>) editor, builder);
    }

    @Override
    protected List<EnumWidget> getSorters(FlowLayout layout) {
        List<EnumWidget> components = new ArrayList<>();

        EnumWidget alphabetic = this.getEnumWidget("fzmm.gui.itemEditor.levelable.option.sort.alphabetic");
        alphabetic.init(SortOption.DISABLE);
        alphabetic.onPress(buttonComponent -> this.sorterExecute(alphabetic, this::sortAlphabetic));

        EnumWidget levels = this.getEnumWidget("fzmm.gui.itemEditor.levelable.option.sort.levels");
        levels.init(SortOption.DISABLE);
        levels.onPress(buttonComponent -> this.sorterExecute(levels, this::sortLevels));

        components.add(alphabetic);
        components.add(levels);

        return components;
    }

    private void sortAlphabetic(SortOption option) {
        Comparator<D> comparator = Comparator.comparing(data -> data.getName().getString());

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.valueList.sort(comparator);
    }

    private void sortLevels(SortOption option) {
        Comparator<D> comparator = Comparator.comparing(D::getLevel);

        if (option == SortOption.DESCENDING)
            comparator = comparator.reversed();

        this.valueList.sort(comparator);
    }
}
