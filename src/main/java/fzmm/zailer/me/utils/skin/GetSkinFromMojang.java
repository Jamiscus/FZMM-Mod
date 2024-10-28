package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.UserCache;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public class GetSkinFromMojang extends GetSkinDecorator {

    public GetSkinFromMojang(GetSkinDecorator getSkinDecorator) {
        super(getSkinDecorator);
    }

    public GetSkinFromMojang() {
        super(null);
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) throws IOException {
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        if (server == null || server.getSessionService() == null) {
            return super.getSkin(playerName);
        }
        MinecraftSessionService sessionService = server.getSessionService();

        Optional<GameProfile> profile = this.getProfile(playerName);
        if (profile.isEmpty()) {
            return super.getSkin(playerName);
        }

        MinecraftProfileTexture skinTexture = sessionService.getTextures(profile.get()).skin();
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
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        if (server == null || server.getUserCache() == null || server.getSessionService() == null) {
            return Optional.empty();
        }

        UserCache userCache = server.getUserCache();
        MinecraftSessionService sessionService = server.getSessionService();
        Optional<GameProfile> profileEntry = userCache.findByName(playerName);
        if (profileEntry.isEmpty()) {
            return Optional.empty();
        }

        GameProfile profileWithUuid = profileEntry.get();
        ProfileResult result = sessionService.fetchProfile(profileWithUuid.getId(), false);
        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(result.profile());
    }
}
