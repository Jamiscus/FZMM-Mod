package fzmm.zailer.me.mixin.cache_skin_getter;

import net.minecraft.client.texture.PlayerSkinTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(PlayerSkinTexture.class)
public interface PlayerSkinTextureAccessor {

    @Accessor
    File getCacheFile();
}
