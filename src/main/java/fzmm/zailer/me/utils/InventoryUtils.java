package fzmm.zailer.me.utils;

import fzmm.zailer.me.mixin.combined_inventory_getter.PlayerInventoryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static List<ItemStack> getItemsFromContainer(ItemStack container) {
        ContainerComponent result = container.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(new ArrayList<>()));

        return result.stream().toList();
    }

    public static long getInventorySizeInBytes() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        List<DefaultedList<ItemStack>> combinedInventory = ((PlayerInventoryAccessor) client.player.getInventory()).getCombinedInventory();
        long size = 0;

        for (DefaultedList<ItemStack> defaultedList : combinedInventory) {
            for (ItemStack itemStack : defaultedList) {
                size += ItemUtils.getLengthInBytes(itemStack);
            }
        }

        return size;
    }
}
