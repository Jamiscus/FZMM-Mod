package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class VanillaSkinGetter extends SkinGetterDecorator {

    public VanillaSkinGetter(SkinGetterDecorator skinGetterDecorator) {
        super(skinGetterDecorator);
    }

    public VanillaSkinGetter() {
        super();
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) {
        MinecraftSessionService sessionService = MinecraftClient.getInstance().getSessionService();

        Optional<GameProfile> profile = this.getProfile(playerName);
        if (profile.isEmpty()) {
            return super.getSkin(playerName);
        }

        MinecraftProfileTexture skinTexture = sessionService.getTextures(profile.get()).skin();
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
        try {
            return SkullBlockEntity.fetchProfile(playerName).get();
        } catch (ExecutionException | InterruptedException ignored) {
            return Optional.empty();
        }
    }
}
