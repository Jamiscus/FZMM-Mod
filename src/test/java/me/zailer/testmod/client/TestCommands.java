package me.zailer.testmod.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.zailer.testmod.client.test.HeadGeneratorTest;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class TestCommands {

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        var testCommand = ClientCommandManager.literal("fzmm:test");

        testCommand.then(ClientCommandManager.literal("head_generator:write")
                .executes(ctx -> {
                    HeadGeneratorTest.writeSkins();
                    return 0;
                })
        );

        testCommand.then(ClientCommandManager.literal("head_generator:check_format")
                .executes(ctx -> {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Missing arguments"));
                    return 0;
                }).then(ClientCommandManager.argument("isSlim", BoolArgumentType.bool()).executes(ctx -> {
                    var isSlim = ctx.getArgument("isSlim", Boolean.class);

                    HeadGeneratorTest.checkFormat(isSlim);
                    return 0;
                }))
        );

        testCommand.then(ClientCommandManager.literal("head_generator:check_pixel")
                .executes(ctx -> {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Missing arguments"));
                    return 0;
                }).then(ClientCommandManager.argument("x", IntegerArgumentType.integer(0, 63)).executes(ctx -> {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Missing arguments"));

                    return 0;
                }).then(ClientCommandManager.argument("y", IntegerArgumentType.integer(0, 63)).executes(ctx -> {
                    var x = ctx.getArgument("x", Integer.class);
                    var y = ctx.getArgument("y", Integer.class);

                    HeadGeneratorTest.checkPixel(x, y, false);
                    return 0;
                })))
        );

        dispatcher.register(testCommand);
    }
}