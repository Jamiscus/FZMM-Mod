package fzmm.zailer.me.client.gui.item_editor.common.levelable;

import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortBuilder;
import net.minecraft.item.ItemStack;

public interface ILevelableBuilder<V, DATA extends ILevelable<V>> extends ISortBuilder<DATA> {

    ItemStack get();

    ILevelableBuilder<V, DATA> add(DATA value);

    ILevelableBuilder<V, DATA> remove(int index);

    /**
     * @return  the same level of the parameter if it does not exceed the maximum possible level (in creative)
     */
    int getMaxLevel(int level);

    boolean isOverMaxLevel();

    ILevelableBuilder<V, DATA> allowDuplicates(boolean value);

    boolean allowDuplicates();

    ILevelableBuilder<V, DATA> clear();

    ILevelableBuilder<V, DATA> stack(ItemStack stack);

    ItemStack stack();

    DATA getValue(int index);

    boolean contains(V value);


}
