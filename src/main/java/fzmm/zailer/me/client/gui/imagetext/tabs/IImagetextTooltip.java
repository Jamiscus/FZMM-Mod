package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import net.minecraft.text.Text;

public interface IImagetextTooltip {

    Text getTooltip(ImagetextLogic logic);
}
