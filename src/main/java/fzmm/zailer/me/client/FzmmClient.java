package fzmm.zailer.me.client;

import fzmm.zailer.me.config.FzmmConfig;
import net.fabricmc.api.ClientModInitializer;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class FzmmClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FzmmConfig.init();
        TextObfuscated.init();
    }
}