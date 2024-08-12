package me.zailer.testmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;


@Environment(EnvType.CLIENT)
public class TestClient implements ClientModInitializer {

    public static final String MOD_ID = "testmod";


    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(TestCommands::registerCommands);
    }
}