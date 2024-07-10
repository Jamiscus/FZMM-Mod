package fzmm.zailer.me.client.gui.components.style;

import fzmm.zailer.me.client.gui.components.style.component.StyledLabelComponent;
import net.minecraft.text.Text;

public class StyledComponents {
    public static StyledLabelComponent label(Text text) {
        return new StyledLabelComponent(text);
    }
}
