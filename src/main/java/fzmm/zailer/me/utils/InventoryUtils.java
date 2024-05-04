package fzmm.zailer.me.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    private final ItemStack container;
    private final List<ItemStack> items;

    public InventoryUtils(ItemStack container) {
        this.container = container;
        this.items = getItemsFromContainer(container);
    }

    public InventoryUtils addItem(List<ItemStack> items) {
        this.items.addAll(items);
        return this;
    }

    public ItemStack get() {
        this.container.apply(DataComponentTypes.CONTAINER, null, component -> ContainerComponent.fromStacks(this.items));

        return this.container;
    }

    public static List<ItemStack> getItemsFromContainer(ItemStack container) {
        ContainerComponent result = container.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(new ArrayList<>()));

        return result.stream().toList();
    }
}
