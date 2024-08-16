package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SnackBarBuilder {
    private final ISnackBarComponent snackBar;
    private Color backgroundColor = FzmmStyles.ALERT_SUCCESS_COLOR;
    private final LabelComponent title;
    private LabelComponent details;
    private final List<ButtonComponent> buttons = new ArrayList<>();
    private Sizing horizontalSizing = Sizing.content();
    private Sizing verticalSizing = Sizing.content();
    private boolean closeButton = false;
    private boolean timerEnabled = false;
    private Boolean canClose = null;
    private long timerMillis = -1;

    private SnackBarBuilder(ISnackBarComponent snackBar, LabelComponent title, LabelComponent details) {
        this.snackBar = snackBar;
        this.title = title;
        this.details = details;
    }

    public static SnackBarBuilder builder(ISnackBarComponent snackBar) {
        return builder(snackBar, StyledComponents.label(Text.empty()), null);
    }

    public static SnackBarBuilder builder(ISnackBarComponent snackBar, LabelComponent title, LabelComponent details) {
        return new SnackBarBuilder(snackBar, title, details);
    }

    public SnackBarBuilder title(Text text) {
        this.title.text(text);
        return this;
    }

    public SnackBarBuilder backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    public SnackBarBuilder details(Text details) {
        if (this.details == null) {
            this.details = StyledComponents.label(Text.empty());
        }

        this.details.text(details);
        return this;
    }

    public SnackBarBuilder button(Function<ISnackBarComponent, ButtonComponent> function) {
        ButtonComponent button = function.apply(this.snackBar);
        button.verticalSizing(Sizing.fixed(16));
        this.buttons.add(button);
        return this;
    }

    public SnackBarBuilder timer(long amount, TimeUnit unit) {
        this.timerMillis = TimeUnit.MILLISECONDS.convert(amount, unit);
        return this;
    }

    public SnackBarBuilder sizing(Sizing horizontalSizing, Sizing verticalSizing) {
        this.horizontalSizing = horizontalSizing;
        this.verticalSizing = verticalSizing;
        return this;
    }

    public SnackBarBuilder startTimer() {
        this.timerEnabled = true;
        return this;
    }

    public SnackBarBuilder closeButton() {
        this.closeButton = true;
        return this;
    }

    public SnackBarBuilder canClose(boolean canClose) {
        this.canClose = canClose;
        return this;
    }

    public ISnackBarComponent build() {
        ISnackBarComponent result = this.snackBar;

        if (this.horizontalSizing.isContent()) {
            int width = MinecraftClient.getInstance().textRenderer.getWidth(this.title.text()) + 30;
            this.horizontalSizing = Sizing.fixed(width);
        }

        this.snackBar.sizing(this.horizontalSizing, this.verticalSizing);
        this.snackBar.setTimer(this.timerMillis);
        result.surface(Surface.flat(this.backgroundColor.argb()));

        FlowLayout layout = (FlowLayout) StyledContainers.verticalFlow(this.horizontalSizing, this.verticalSizing)
                .gap(1)
                .padding(Insets.of(3))
                .horizontalAlignment(HorizontalAlignment.LEFT);

        FlowLayout firstRow = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        firstRow.child(this.title);
        if (this.closeButton) {
            firstRow.child(Components.spacer());
            ButtonComponent button = Components.button(Text.translatable("fzmm.snack_bar.close"), buttonComponent -> result.close());
            button.positioning(Positioning.relative(100, 0))
                    .sizing(Sizing.fixed(14));
            button.renderer(ButtonComponent.Renderer.flat(0, 0x60000000, 0));

            firstRow.child(button)
                    .verticalAlignment(VerticalAlignment.CENTER)
                    .verticalSizing(Sizing.fixed(14));
        }
        layout.child(firstRow);

        boolean hasDetails = this.details != null && !this.details.text().toString().isEmpty();
        if (hasDetails || !this.buttons.isEmpty()) {
            FlowLayout secondRow = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
            secondRow.gap(4);
            layout.child(secondRow);
            if (hasDetails) {
                FlowLayout detailsLayout = StyledContainers.verticalFlow(Sizing.content(), Sizing.content());
                layout.child(detailsLayout);

                BooleanButton detailsButton = this.getDetailsButton(detailsLayout);
                secondRow.child(detailsButton);
            }

            secondRow.children(this.buttons);
        }

        result.add(layout);

        if (this.timerEnabled) {
            result.startTimer();
        }

        if (this.canClose != null) {
            this.snackBar.canClose(this.canClose);
        }

        return result;
    }

    private BooleanButton getDetailsButton(FlowLayout detailsLayout) {
        //TODO: animate collapsing button
        BooleanButton detailsButton = new BooleanButton(
                Text.translatable("fzmm.snack_bar.expand.expanded"),
                Text.translatable("fzmm.snack_bar.expand.collapsed")
        );
        detailsButton.renderer(ButtonComponent.Renderer.flat(0, 0x60000000, 0));
        detailsButton.sizing(Sizing.fixed(16));
        this.details.horizontalSizing(Sizing.expand(100));
        detailsButton.onPress(buttonComponent -> {
            if (detailsButton.enabled()) {
                detailsLayout.child(this.details);
            } else {
                detailsLayout.removeChild(this.details);
            }
        });
        return detailsButton;
    }
}
