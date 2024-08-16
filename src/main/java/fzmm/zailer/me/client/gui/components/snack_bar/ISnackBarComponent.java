package fzmm.zailer.me.client.gui.components.snack_bar;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import net.minecraft.client.MinecraftClient;


public interface ISnackBarComponent extends ParentComponent {

    void setTimer(long timerMillis);
    
    void updateTimer(long timeElapsed);

    void updateTimerBar(float percent);

    ISnackBarComponent startTimer();

    boolean canClose();

    void canClose(boolean value);

    default void close() {
        if (MinecraftClient.getInstance().currentScreen instanceof ISnackBarManager manager) {
            manager.removeSnackBar(this);
        }
    }

    void add(Component snackBar);
}
