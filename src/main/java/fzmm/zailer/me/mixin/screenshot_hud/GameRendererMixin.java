package fzmm.zailer.me.mixin.screenshot_hud;

import fzmm.zailer.me.client.gui.components.image.source.ScreenshotSource;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderHand(Lnet/minecraft/client/render/Camera;FLorg/joml/Matrix4f;)V", at = @At("HEAD"), cancellable = true)
    private void fzmm$removeHandInScreenshotHud(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        if (ScreenshotSource.hasInstance()) {
            ci.cancel();
        }
    }
}
