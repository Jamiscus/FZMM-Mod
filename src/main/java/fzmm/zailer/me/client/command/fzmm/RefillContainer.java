package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;

public class RefillContainer implements ISubCommand {
    @Override
    public String alias() {
        return "fullcontainer";
    }

    @Override
    public String syntax() {
        return "fullcontainer <slots to fill> <first slot>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("slots to fill", IntegerArgumentType.integer(1, 27)).executes(ctx -> {

            this.fullContainer(ctx.getArgument("slots to fill", int.class), -1);
            return 1;
        }).then(ClientCommandManager.argument("first slot", IntegerArgumentType.integer(0, 27)).executes(ctx -> {

            int slotsToFill = ctx.getArgument("slots to fill", int.class);
            int firstSlot = ctx.getArgument("first slot", int.class);

            this.fullContainer(slotsToFill, firstSlot);
            return 1;
        }))).build();
    }

    /**
     * @param firstSlot if -1, it will fill empty slots starting at 0
     */
    private void fullContainer(int slotsToFill, int firstSlot) {
        ItemStack containerStack = ItemUtils.from(Hand.MAIN_HAND);
        ItemStack itemStack = ItemUtils.from(Hand.OFF_HAND);

        NbtCompound nbt = containerStack.getOrCreateNbt();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList containerItems = nbt.getCompound(TagsConstant.BLOCK_ENTITY)
                .getList(ShulkerBoxBlockEntity.ITEMS_KEY, NbtElement.COMPOUND_TYPE);



        if (firstSlot == -1) {
            fullContainerEmptySlots(containerItems, itemStack, slotsToFill);
        } else {
            fullContainer(containerItems, itemStack, slotsToFill, firstSlot);
        }

        for (int i = 0; i < containerItems.size(); i++) {
            ItemStack stack = ItemStack.fromNbt(containerItems.getCompound(i));
            if (stack.isEmpty() || containerItems.getCompound(i).getInt(TagsConstant.INVENTORY_SLOT) >= ShulkerBoxBlockEntity.INVENTORY_SIZE) {
                containerItems.remove(i);
                i--;
            }
        }

        ItemUtils.give(containerStack);
        blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, containerItems);
        blockEntityTag.putString("id", containerStack.getItem().toString());

        nbt.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        containerStack.setNbt(nbt);
        ItemUtils.give(containerStack);
    }

    private static void fullContainer(NbtList stackList, ItemStack stack, int slotsToFill, int firstSlot) {
        int finalSlot = firstSlot + slotsToFill;
        if (finalSlot > stackList.size()) {
            for (int i = stackList.size(); i < finalSlot; i++) {
                stackList.add(InventoryUtils.getSlotTag(ItemStack.EMPTY, i));
            }
        }

        for (int i = firstSlot; i != finalSlot; i++) {
            stackList.set(i, InventoryUtils.getSlotTag(stack, i));
        }
    }

    private static void fullContainerEmptySlots(NbtList stackList, ItemStack stack, int slotsToFill) {
        int finalSlot = Math.min(stackList.size() + slotsToFill, ShulkerBoxBlockEntity.INVENTORY_SIZE);
        if (finalSlot > stackList.size()) {
            for (int i = stackList.size(); i < finalSlot; i++) {
                stackList.add(InventoryUtils.getSlotTag(ItemStack.EMPTY, i));
            }
        }

        for (int i = 0; i != finalSlot; i++) {
            if (ItemStack.fromNbt(stackList.getCompound(i)).isEmpty()) {
                stackList.set(i, InventoryUtils.getSlotTag(stack, i));
                slotsToFill--;
            }

            if (slotsToFill == 0) {
                break;
            }
        }
    }
}
