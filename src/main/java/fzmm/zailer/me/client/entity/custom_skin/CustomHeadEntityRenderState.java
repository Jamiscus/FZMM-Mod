package fzmm.zailer.me.client.entity.custom_skin;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

public class CustomHeadEntityRenderState extends LivingEntityRenderState {
    public CustomHeadEntityRenderState(Identifier texture) {
        this.texture = texture;
    }

    public Identifier texture;
}
