package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

public class UpdatableSnackBarComponent extends BaseSnackBarComponent {
    protected LabelComponent title;
    protected LabelComponent details;

    protected UpdatableSnackBarComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);
        this.title = StyledComponents.label(Text.empty());
        this.details = StyledComponents.label(Text.empty());
    }

    public void updateTitle(Text text) {
        this.title.text(text);
    }

    public void updateDetails(Text text) {
        this.details.text(text);
    }

    public static SnackBarBuilder builder(String id) {
        UpdatableSnackBarComponent component = new UpdatableSnackBarComponent(Sizing.content(), Sizing.content());
        return SnackBarBuilder.builder(component, component.title, component.details, id);
    }
}
