package fzmm.zailer.me.compat.symbol_chat.font;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;

@SuppressWarnings("UnstableApiUsage")
public class FontTextBoxComponent extends ConfigTextBox {
    private boolean fontProcessEnabled;

    public FontTextBoxComponent(Sizing horizontalSizing) {
        super();
        this.horizontalSizing(horizontalSizing);
        this.fontProcessEnabled = false;
    }

    public void enableFontProcess(boolean enabled) {
        this.fontProcessEnabled = enabled;
    }

    @Override
    public void write(String text) {
        if (!this.fontProcessEnabled) {
            super.write(text);
        } else if (MinecraftClient.getInstance().currentScreen instanceof BaseFzmmScreen screen) {
            screen.getSymbolChatCompat().processFont(this, text, super::write);
        }
    }

}
