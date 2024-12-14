package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
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

        Optional<SkinTextures> textures;
        try {
            // if the list of players was not loaded, fetch it and gets the skin from there,
            // which can give a skin provided by the server
            textures = client.getSkinProvider().fetchSkinTextures(profile).get();
        } catch (ExecutionException | InterruptedException ignored) {
            return Optional.empty();
        }

        if (textures.isEmpty()) {
            return Optional.empty();
        }

        AbstractTexture texture = client.getTextureManager().getTexture(textures.get().texture());
        if (!(texture instanceof NativeImageBackedTexture nativeTexture)) {
            return Optional.empty();
        }

        NativeImage nativeImage = nativeTexture.getImage();
        if (nativeImage == null) {
            return Optional.empty();
        }

        return Optional.of(ImageUtils.getBufferedImgFromNativeImg(nativeImage));
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
