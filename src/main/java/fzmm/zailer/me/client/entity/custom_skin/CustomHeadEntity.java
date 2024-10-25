package fzmm.zailer.me.client.entity.custom_skin;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class CustomHeadEntity extends MobEntity implements ISkinMutable {
    public static final EntityType<CustomHeadEntity> CUSTOM_HEAD_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            FzmmClient.CUSTOM_HEAD_ENTITY,
            EntityType.Builder.<CustomHeadEntity>create((type, world) -> new CustomHeadEntity(world), SpawnGroup.MISC)
                    .disableSaving()
                    .disableSummon()
                    .dimensions(0.8f, 0.8f)
                    .maxTrackingRange(32)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, FzmmClient.CUSTOM_HEAD_ENTITY))
    );

    private Identifier customHeadTexture = DefaultSkinHelper.getTexture();

    public CustomHeadEntity(World world) {
        super(CUSTOM_HEAD_ENTITY_TYPE, world);
    }

    @Override
    public Identifier getTextures() {
        return this.customHeadTexture;
    }

    @Override
    public void setSkin(Identifier skin, boolean isSlim) {
        this.customHeadTexture = skin;
    }

    @Override
    public void updateFormat(boolean isSlim) {

    }
}