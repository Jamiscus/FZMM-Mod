package fzmm.zailer.me.client.entity.custom_skin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class CustomPlayerSkinEntity extends OtherClientPlayerEntity implements ISkinMutable {

    private SkinTextures textures;
    
    public CustomPlayerSkinEntity(ClientWorld world) {
        super(world, new GameProfile(UUID.randomUUID(), ""));
        this.getDataTracker().set(PLAYER_MODEL_PARTS, Byte.MAX_VALUE);
    }

    @Override
    public SkinTextures getSkinTextures() {
        return this.textures;
    }

    @Override
    public Identifier getTextures() {
        return this.textures.texture();
    }

    @Override
    public void setSkin(Identifier skin, boolean isSlim) {
        this.textures = new SkinTextures(skin,
                null,
                null,
                null,
                isSlim ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE,
                false
        );
    }

    @Override
    public void updateFormat(boolean isSlim) {
        this.setSkin(this.textures.texture(), isSlim);
    }
}
