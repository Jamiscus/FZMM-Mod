package fzmm.zailer.me.mixin.component.book;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fzmm.zailer.me.client.gui.components.BookComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EditBoxWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditBoxWidget.class)
public class EditBoxWidgetMixin {

    @WrapOperation(method = "renderContents",
            at = @At(value = "INVOKE",
                    target= "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)I"
            ))
    protected int fzmm$replaceDrawText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, Operation<Integer> original) {
        if (((Object) this) instanceof BookComponent) {
            return context.drawText(textRenderer, text, x, y, 0x00000000, false) + 1;
        }

        return original.call(context, textRenderer, text, x, y, color);
    }

    @WrapOperation(method = "renderContents",
            at = @At(value = "INVOKE",
                    target= "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"
            ))
    protected void fzmm$replaceCursorColor(DrawContext instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original) {
        if (((Object) this) instanceof BookComponent) {
            color = 0xFF222222;
        }

        original.call(instance, x1, y1, x2, y2, color);
    }
}
