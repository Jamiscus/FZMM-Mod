package fzmm.zailer.me.client.gui.text_format.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.utils.memento.IMemento;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import net.minecraft.text.Text;

import java.util.function.Consumer;


public interface ITextFormatTab extends IScreenTab, IMemento {
    Text getText(TextFormatLogic logic);

    void setRandomValues();

    void componentsCallback(Consumer<Object> callback);

    boolean hasStyles();
}
