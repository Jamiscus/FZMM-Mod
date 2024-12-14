package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import fzmm.zailer.me.utils.skin.SkinGetterDecorator;
import fzmm.zailer.me.utils.skin.VanillaSkinGetter;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SkullCommand implements ISubCommand {
    @Override
    public String alias() {
        return "skull";
    }

    @Override
    public String syntax() {
        return "skull <skull owner> cache/mineskin/mojang";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        var argument = ClientCommandManager.argument("skull owner", StringArgumentType.word())
                .suggests(FzmmUtils.SUGGESTION_PLAYER)
                .executes(ctx -> {

                    String skullOwner = ctx.getArgument("skull owner", String.class);
                    this.getHead(new CacheSkinGetter(new VanillaSkinGetter()), skullOwner)
                            .whenComplete((stack, throwable) -> ItemUtils.give(stack));

                    return 1;
                }).build();

        for (var subCommand : this.getArgSubCommands()) {
            argument.addChild(subCommand);
        }

        return builder.then(argument).build();
    }

    private List<LiteralCommandNode<FabricClientCommandSource>> getArgSubCommands() {
        List<LiteralCommandNode<FabricClientCommandSource>> result = new ArrayList<>();

        result.add(ClientCommandManager.literal("cache")
                .executes(ctx -> {

                    String skullOwner = ctx.getArgument("skull owner", String.class);
                    this.getHead(new CacheSkinGetter(), skullOwner)
                            .whenComplete((stack, throwable) -> ItemUtils.give(stack));

                    return 1;
                }).build());

        result.add(ClientCommandManager.literal("mineskin")
                .executes(ctx -> {

                    String skullOwner = ctx.getArgument("skull owner", String.class);
                    CompletableFuture.runAsync(() -> HeadUtils.uploadAndGetHead(skullOwner)
                            .ifPresent(ItemUtils::give), Util.getMainWorkerExecutor());

                    return 1;
                }).build());

        result.add(ClientCommandManager.literal("mojang")
                .executes(ctx -> {

                    String skullOwner = ctx.getArgument("skull owner", String.class);
                    this.getHead(new VanillaSkinGetter(), skullOwner)
                            .whenComplete((stack, throwable) -> ItemUtils.give(stack));

                    return 1;
                }).build());

        return result;
    }

    private CompletableFuture<ItemStack> getHead(SkinGetterDecorator skinDecorator, String playerName) {
        return CompletableFuture.supplyAsync(() -> skinDecorator.getHead(playerName)
                        .orElseGet(() -> HeadBuilder.of(playerName)),
                Util.getMainWorkerExecutor()
        );
    }
}
