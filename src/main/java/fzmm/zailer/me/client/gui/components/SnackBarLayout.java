package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;

public class SnackBarLayout extends StyledFlowLayout implements ISnackBarScreen {
    public SnackBarLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.positioning(Positioning.relative(100, 0));
        this.margins(Insets.right(3).withTop(3));
        this.horizontalAlignment(HorizontalAlignment.RIGHT);
    }

    @Override
    public FlowLayout getSnackBarLayout() {
        return this;
    }
}
