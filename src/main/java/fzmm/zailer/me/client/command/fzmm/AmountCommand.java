package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AmountCommand implements ISubCommand {
    @Override
    public String alias() {
        return "amount";
    }

    @Override
    public String syntax() {
        return "amount <value>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 64)).executes(ctx -> {

            int amount = ctx.getArgument("value", int.class);
            this.amount(amount);
            return 1;
        })).build();
    }

    private void amount(int amount) {
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);
        stack.setCount(amount);
        ItemUtils.updateHand(stack);
    }
}
