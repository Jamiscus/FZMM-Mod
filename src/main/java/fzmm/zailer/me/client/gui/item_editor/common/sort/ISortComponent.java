package fzmm.zailer.me.client.gui.item_editor.common.sort;

import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;

import java.util.List;

public interface ISortComponent<V, D> extends ParentComponent, IListEntry<ISortComponent<V, D>> {

    FlowLayout clearChildren();

    default FlowLayout getLayout(ParentComponent layout, ButtonComponent upButton, ButtonComponent downButton) {
        FlowLayout result = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        result.gap(4);

        return result.children( List.of(upButton, downButton, layout));
    }

    D value();
}
