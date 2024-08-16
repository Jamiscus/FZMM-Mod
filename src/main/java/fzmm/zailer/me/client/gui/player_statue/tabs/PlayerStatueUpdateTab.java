package fzmm.zailer.me.client.gui.player_statue.tabs;

import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import fzmm.zailer.me.client.gui.utils.select_item.SelectItemScreen;
import fzmm.zailer.me.client.logic.player_statue.PlayerStatue;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerStatueUpdateTab implements IPlayerStatueTab {
    @Override
    public String getId() {
        return "update";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
    }

    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        RequestedItem requestedItem = new RequestedItem(
                PlayerStatue::isPlayerStatue,
                stack -> {
                    if (PlayerStatue.isPlayerStatue(stack)) {
                        ItemStack statue = PlayerStatue.updateStatue(stack, new Vector3f(x, y, z), direction, name);
                        FzmmUtils.giveItem(statue);

                        //TODO: Needs to switch from one screen to another
                        ISnackBarComponent snackBar = BaseSnackBarComponent.builder()
                                .backgroundColor(FzmmStyles.ALERT_SUCCESS_COLOR)
                                .title(Text.translatable("fzmm.snack_bar.playerStatue.updated.title"))
                                .timer(3, TimeUnit.SECONDS)
                                .startTimer()
                                .build();
                        FzmmUtils.addSnackBar(snackBar);
                    }
                },
                new ArrayList<>(),
                Text.translatable("fzmm.gui.playerStatue.option.select.title"),
                true
        );

        client.setScreen(new SelectItemScreen(client.currentScreen, requestedItem));
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public IMementoObject createMemento() {
        return null;
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {

    }
}
