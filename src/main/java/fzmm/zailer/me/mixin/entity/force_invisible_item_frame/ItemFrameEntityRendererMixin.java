package fzmm.zailer.me.mixin.entity.force_invisible_item_frame;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin<T extends ItemFrameEntity> {

    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V",
            at = @At("RETURN")
    )
    private void fzmm$disableItemFrameFrameRendering(T itemFrameEntity, ItemFrameEntityRenderState state, float f, CallbackInfo ci) {
        if ((FzmmClient.CONFIG.general.forceInvisibleItemFrame() && !state.itemRenderState.isEmpty())) {
            state.invisible = true;
        }
    }

}
