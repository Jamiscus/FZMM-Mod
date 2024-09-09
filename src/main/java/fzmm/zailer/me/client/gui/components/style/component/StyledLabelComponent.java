package fzmm.zailer.me.client.gui.components.style.component;

import fzmm.zailer.me.client.FzmmClient;
import io.wispforest.owo.ui.component.LabelComponent;
import net.minecraft.text.Text;

public class StyledLabelComponent extends LabelComponent {

    public StyledLabelComponent(Text text) {
        super(text);

        // improves text readability with translucent background
        boolean oldBackground = FzmmClient.CONFIG.guiStyle.oldBackground();
        if (!oldBackground) {
            this.shadow(true);
        }
    }

    private Text applyStyle(Text text) {
//        if (!text.getStyle().isEmpty()) {
//            return text;
//        }
//
////        boolean darkMode = FzmmClient.CONFIG.guiStyle.darkMode();
////        int color = (darkMode ? Color.WHITE : Color.BLACK).argb();
//
//
//        return text.copy().setStyle(text.getStyle().withColor(color));
        return text;
    }

    @Override
    public LabelComponent text(Text text) {
        return super.text(this.applyStyle(text));
    }

}
