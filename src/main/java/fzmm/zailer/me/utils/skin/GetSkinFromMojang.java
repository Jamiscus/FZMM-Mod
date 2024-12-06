package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.PropertyMap;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class GetSkinFromMojang extends GetSkinDecorator {

    public GetSkinFromMojang(GetSkinDecorator getSkinDecorator) {
        super(getSkinDecorator);
    }

    public GetSkinFromMojang() {
        super(null);
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) throws IOException {
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

        return ImageUtils.getImageFromUrl(skinTexture.getUrl());
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);
        return profile.map(HeadBuilder::of).or(() -> super.getHead(playerName));
    }

    /**
     * @param playerName the name of the player
     * @return empty {@link Optional} if no profile is found
     */
    public Optional<GameProfile> getProfile(String playerName) {
        try {
            return Optional.of(new ProfileComponent(
                    Optional.of(playerName),
                    Optional.empty(),
                    new PropertyMap()
            ).getFuture()
                    .get()
                    .gameProfile());
        } catch (InterruptedException | ExecutionException e) {
            FzmmClient.LOGGER.error("[GetSkinFromMojang] Failed to get profile for player '{}'", playerName, e);
        }

        return Optional.empty();
    }
}
