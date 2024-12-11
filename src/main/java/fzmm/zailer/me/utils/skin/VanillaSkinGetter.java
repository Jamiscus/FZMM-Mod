package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public class VanillaSkinGetter extends SkinGetterDecorator {

    public VanillaSkinGetter(SkinGetterDecorator skinGetterDecorator) {
        super(skinGetterDecorator);
    }

    public VanillaSkinGetter() {
        super();
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);
        if (profile.isEmpty()) {
            return super.getSkin(playerName);
        }

        MinecraftProfileTexture skinTexture = MinecraftClient.getInstance().getSessionService()
                .getTextures(profile.get())
                .skin();
        if (skinTexture == null) {
            return super.getSkin(playerName);
        }

        try {
            Optional<BufferedImage> result = ImageUtils.getImageFromUrl(skinTexture.getUrl());
            if (result.isPresent()) {
                return result;
            }
        } catch (IOException ignored) {
        }

        return super.getSkin(playerName);
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);
        return profile.map(HeadBuilder::of).or(() -> super.getHead(playerName));
    }

    @Override
    public Optional<GameProfile> getProfile(String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

        if (networkHandler == null) {
            return Optional.empty();
        }

        // implementation from in 1.20.5 branch fixes issue with case-sensitive
        PlayerListEntry playerListEntry = networkHandler.getPlayerListEntry(playerName);

        return playerListEntry == null ? Optional.empty() : Optional.of(playerListEntry.getProfile());
    }
}
