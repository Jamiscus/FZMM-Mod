package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.command.ISubCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.text.Text;

public class NameCommand implements ISubCommand {
    @Override
    public String alias() {
        return "name";
    }

    @Override
    public String syntax() {
        return "name <item name>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("name", TextArgumentType.text(registryAccess))
                .executes(ctx -> {

                    Text name = ctx.getArgument("name", Text.class);

                    DisplayBuilder.renameHandItem(name.copy());
                    return 1;
                })).build();
    }
}
