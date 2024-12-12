package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.client.command.argument_type.StackArgumentType;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.ItemStack;

public class GiveCommand implements ISubCommand {
    @Override
    public String alias() {
        return "give";
    }

    @Override
    public String syntax() {
        return "give <item> <amount>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("item", StackArgumentType.itemStack(registryAccess)).executes((ctx) -> {

            this.giveItem(StackArgumentType.getItemStackArgument(ctx, "item"), 1);
            return 1;
        }).then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 99)).executes((ctx) -> {

            int amount = IntegerArgumentType.getInteger(ctx, "amount");
            ItemStackArgument item = StackArgumentType.getItemStackArgument(ctx, "item");

            this.giveItem(item, amount);
            return 1;
        }))).build();
    }

    private void giveItem(ItemStackArgument item, int amount) throws CommandSyntaxException {
        ItemStack itemStack = item.createStack(amount, false);
        ItemUtils.give(ItemUtils.process(itemStack));
    }
}
