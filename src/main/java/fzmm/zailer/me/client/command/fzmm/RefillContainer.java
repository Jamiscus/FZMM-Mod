package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

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

        containerStack.apply(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT, component -> {
            List<ItemStack> stacksCopy = new ArrayList<>(component.stream().toList());

            if (firstSlot == -1) {
                this.fullContainerEmptySlots(stacksCopy, itemStack, slotsToFill);
            } else {
                this.fullContainer(stacksCopy, itemStack, slotsToFill, firstSlot);
            }

            return ContainerComponent.fromStacks(stacksCopy);
        });

        ItemUtils.give(containerStack);
    }

    private void fullContainer(List<ItemStack> stackList, ItemStack stack, int slotsToFill, int firstSlot) {
        int finalSlot = firstSlot + slotsToFill;
        if (slotsToFill > stackList.size()) {
            for (int i = stackList.size(); i < finalSlot; i++) {
                stackList.add(ItemStack.EMPTY);
            }
        }

        for (int i = firstSlot; i != finalSlot; i++) {
            stackList.set(i, stack);
        }
    }

    private void fullContainerEmptySlots(List<ItemStack> stackList, ItemStack stack, int slotsToFill) {
        int finalSlot = Math.min(stackList.size() + slotsToFill, ShulkerBoxBlockEntity.INVENTORY_SIZE);
        if (finalSlot > stackList.size()) {
            for (int i = stackList.size(); i < finalSlot; i++) {
                stackList.add(ItemStack.EMPTY);
            }
        }

        for (int i = 0; i != finalSlot; i++) {
            if (stackList.get(i).isEmpty()) {
                stackList.set(i, stack);
                slotsToFill--;
            }

            if (slotsToFill == 0) {
                break;
            }
        }
    }
}
