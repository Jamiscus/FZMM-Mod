package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.mixin.cache_skin_getter.PlayerSkinTextureAccessor;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.ItemStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CacheSkinGetter extends SkinGetterDecorator {

    public CacheSkinGetter(SkinGetterDecorator skinGetterDecorator) {
        super(skinGetterDecorator);
    }

    public CacheSkinGetter() {
        super();
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) {
        Optional<GameProfile> profileOptional = this.getProfile(playerName);
        if (profileOptional.isEmpty()) {
            return super.getSkin(playerName);
        }

        Optional<BufferedImage> cacheSkin = this.getSkin(profileOptional.get());

        return cacheSkin.isPresent() ? cacheSkin : super.getSkin(playerName);
    }

    public Optional<BufferedImage> getSkin(GameProfile profile) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        SkinTextures textures;
        try {
            // if the list of players was not loaded, fetch it and gets the skin from there,
            // which can give a skin provided by the server
            textures = client.getSkinProvider().fetchSkinTextures(profile).get();
        } catch (ExecutionException | InterruptedException ignored) {
            return Optional.empty();
        }
        AbstractTexture texture = client.getTextureManager().getTexture(textures.texture());
        // if the player is invisible the texture is not an instance of PlayerSkinTexture
        if (!(texture instanceof PlayerSkinTexture skinTexture)) {
            return Optional.empty();
        }

        File skinFile = ((PlayerSkinTextureAccessor) skinTexture).getCacheFile();

        try {
            return Optional.of(ImageIO.read(skinFile));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        Optional<GameProfile> profile = this.getProfile(playerName);

        return profile.map(HeadBuilder::of).or(() -> super.getHead(playerName));
    }

    @Override
    protected Optional<GameProfile> getProfile(String playerName) {
        PlayerListEntry playerListEntry = FzmmUtils.getOnlinePlayer(playerName);
        if (playerListEntry == null) {
            return Optional.empty();
        }

        return Optional.of(playerListEntry.getProfile());
    }
}
