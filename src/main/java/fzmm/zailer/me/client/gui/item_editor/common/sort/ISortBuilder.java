package fzmm.zailer.me.client.gui.item_editor.common.sort;

import java.util.List;

public interface ISortBuilder<V> {

    List<V> values();

    ISortBuilder<V> values(List<V> values);

    V getValue(int i);
}
