package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.util.Identifier;

public interface ISkinMutable {

    Identifier getTextures();

    void setSkin(Identifier skin, boolean isSlim);

    void updateFormat(boolean isSlim);
}
