package fzmm.zailer.me.mixin.modify_fzmm_command.modify_chat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fzmm.zailer.me.mixin_interfaces.IAllowParagraphs;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "onChatFieldUpdate", at = @At("HEAD"))
    private void fzmm$onChatFieldUpdate(String chatText, CallbackInfo ci) {
        this.fzmm$tryModifyChat(chatText);
    }

    @WrapOperation(
            method = "setChatFromHistory",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;setText(Ljava/lang/String;)V")
    )
    private void fzmm$setText(TextFieldWidget instance, String text, Operation<Void> original) {
        this.fzmm$tryModifyChat(text);
        original.call(instance, text);
    }

    @Unique
    private void fzmm$tryModifyChat(String message) {
        if (this.fzmm$isFzmmCommand(message)) {
            this.chatField.setMaxLength(Integer.MAX_VALUE);
            ((IAllowParagraphs) this.chatField).fzmm$setAllowParagraphs(true);
        } else {
            if (this.chatField.getCursor() > 256) {
                this.chatField.setCursor(Math.min(256, this.chatField.getText().length()), false);
            }
            this.chatField.setMaxLength(256);
            ((IAllowParagraphs) this.chatField).fzmm$setAllowParagraphs(false);
        }
    }

    @Unique
    private boolean fzmm$isFzmmCommand(String message) {
        return message.startsWith("/fzmm ");
    }

    @ModifyReturnValue(method = "normalize", at = @At(value = "RETURN"))
    private String fzmm$avoidNormalizeWithFzmmCommand(String str) {
        if (this.fzmm$isFzmmCommand(str))
            return this.chatField.getText();

        return str;
    }
}
