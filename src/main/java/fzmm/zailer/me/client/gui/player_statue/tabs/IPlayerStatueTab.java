package fzmm.zailer.me.client.gui.player_statue.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.utils.memento.IMemento;

public interface IPlayerStatueTab extends IScreenTab, IMemento {

    void execute(HorizontalDirectionOption direction, float x, float y, float z, String name);

    boolean canExecute();
}
