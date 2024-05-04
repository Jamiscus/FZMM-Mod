package fzmm.zailer.me.mixin.modify_fzmm_command.allow_paragraphs_symbol;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fzmm.zailer.me.mixin_interfaces.IAllowParagraphs;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin implements IAllowParagraphs {

    @Shadow public abstract void setText(String text);

    @Shadow public abstract String getText();

    @Shadow public abstract int getCursor();

    @Shadow public abstract void setCursor(int cursor, boolean shiftKeyPressed);

    @Unique
    private boolean fzmm$allowParagraphs = false;

    @WrapOperation(
            method = "write(Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;stripInvalidChars(Ljava/lang/String;)Ljava/lang/String;")
    )
    private String fzmm$allowParagraphInWrite(String string, Operation<String> original) {
        if (this.fzmm$allowParagraphs) {
            return string;
        }

        return original.call(string);
    }

    @WrapOperation(
            method = "charTyped(CI)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/SharedConstants;isValidChar(C)Z")
    )
    private boolean fzmm$allowParagraphInCharTyped(char c, Operation<Boolean> original) {
        if (this.fzmm$allowParagraphs) {
            return true;
        }

        return original.call(c);
    }

    @Unique
    @Override
    public void fzmm$setAllowParagraphs(boolean allowParagraphs) {
        this.fzmm$allowParagraphs = allowParagraphs;

        if (!allowParagraphs) {
            String text = this.getText();
            String strippedText = SharedConstants.stripInvalidChars(text);

            if (!strippedText.equals(text)) {
                int cursorPosition = this.getCursor();
                this.setText(strippedText);
                this.setCursor(MathHelper.clamp(cursorPosition, 0, strippedText.length()), false);
            }
        }
    }
}
