package fzmm.zailer.me.client.gui.components.snack_bar;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;

import java.util.List;
import java.util.stream.Collectors;

public interface ISnackBarManager {

    FlowLayout getSnackBarLayout();

    default void addSnackBar(ISnackBarComponent toast) {
        List<ISnackBarComponent> snackBarList = this.getSnackBars();

        if (snackBarList.size() >= this.getSnackBarLimit() && snackBarList.stream().anyMatch(ISnackBarComponent::canClose)) {
            for (var snackBar : snackBarList) {
                if (snackBar.canClose()) {
                    this.removeSnackBar(snackBar);
                    break;
                }
            }
        }

        this.getSnackBarLayout().child(toast);
    }

    default void removeSnackBar(ISnackBarComponent snackBar) {
        this.getSnackBarLayout().removeChild(snackBar);
    }

    /**
     * Add the overlay below the toast
     */
    default void addOverlay(Component overlay) {
        FlowLayout snackBarLayout = this.getSnackBarLayout();
        ParentComponent root = this.getSnackBarLayout().root();

        if (root instanceof FlowLayout rootLayout) {
            rootLayout.removeChild(snackBarLayout);
            rootLayout.child(overlay);
            rootLayout.child(snackBarLayout);
        }
    }

    default void clearSnackBars() {
        this.getSnackBarLayout().clearChildren();
    }

    default int getSnackBarLimit() {
        return 5;
    }

    default List<ISnackBarComponent> getSnackBars() {
        return this.getSnackBarLayout().children().stream()
                .filter(component -> component instanceof ISnackBarComponent)
                .map(component -> (ISnackBarComponent) component)
                .collect(Collectors.toList());
    }
}
