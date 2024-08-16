package fzmm.zailer.me.client.gui.components.snack_bar;

import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import org.jetbrains.annotations.Nullable;

public class BaseSnackBarComponent extends StyledFlowLayout implements ISnackBarComponent {
    protected boolean timerEnabled;
    protected long timerMillis = -1;
    protected long startTimeMillis = 0;
    @Nullable
    protected FlowLayout timerComponent = null;
    protected boolean canClose = true;

    protected BaseSnackBarComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.timerEnabled = false;
        this.zIndex(900);
    }

    @Override
    public ISnackBarComponent startTimer() {
        if (!this.timerEnabled) {
            FlowLayout timerLayout = StyledContainers.horizontalFlow(Sizing.expand(100), Sizing.fixed(2));
            timerLayout.positioning(Positioning.relative(0, 100));
            this.timerComponent = StyledContainers.horizontalFlow(Sizing.fixed(0), Sizing.expand(100));
            this.timerComponent.surface(Surface.flat(Color.WHITE.argb()));
            timerLayout.child(this.timerComponent);
            this.child(timerLayout);
        }

        this.canClose(false);
        this.timerEnabled = true;
        this.startTimeMillis = System.currentTimeMillis();

        return this;
    }

    @Override
    public boolean canClose() {
        return this.canClose;
    }

    @Override
    public void canClose(boolean value) {
        this.canClose = value;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        if (this.timerComponent != null) {
            this.updateTimer(System.currentTimeMillis() - this.startTimeMillis);
        }
    }

    @Override
    public void setTimer(long timerMillis) {
        this.timerMillis = timerMillis;
    }

    @Override
    public void updateTimer(long time) {
        if (!this.timerEnabled || this.timerMillis <= 0) {
            return;
        }

        float percent = time / (float) this.timerMillis;
        this.updateTimerBar(percent);

        if (time > this.timerMillis) {
            this.timerEnabled = false;
            this.canClose(true);
            this.close();
        }
    }

    @Override
    public void updateTimerBar(float percent) {
        int totalWidth = this.width();
        int width = (int) (totalWidth * percent);

        if (this.timerComponent != null) {
            this.timerComponent.horizontalSizing(Sizing.fixed(width));
        }
    }


    //TODO: add animation when add or close
    // animations algorithms: fade, slide

    @Override
    public void add(Component toast) {
        this.child(toast);
    }

    public static SnackBarBuilder builder() {
        return SnackBarBuilder.builder(new BaseSnackBarComponent(Sizing.content(), Sizing.content()));
    }
}
