package fzmm.zailer.me.client.gui.utils.auto_placer;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.player_statue.PlayerStatuePlacerScreen;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAutoPlacer extends BaseFzmmScreen {
    private static final int DELAY_IN_MILLISECONDS = 500;
    private static final String MAIN_LAYOUT_ID = "main-layout";
    private static final String EXECUTE_ID = "execute";
    private static final String CANCEL_ID = "cancel";
    private static final String LOADING_BAR_ID = "loading-bar";
    private static final String LOADING_LABEL_ID = "loading-label";
    private static final String INFO_LABELS_ID = "info-labels";
    protected FlowLayout loadingBarLayout;
    protected LabelComponent loadingLabel;
    protected ButtonComponent cancelButton;

    public AbstractAutoPlacer(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(screenPath, baseScreenTranslationKey, parent);
    }

    @Override
    protected void setup(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;

        FlowLayout mainLayout = rootComponent.childById(FlowLayout.class, MAIN_LAYOUT_ID);
        checkNull(mainLayout, "flow-layout", MAIN_LAYOUT_ID);

        ButtonComponent executeButton = rootComponent.childById(ButtonComponent.class, EXECUTE_ID);
        checkNull(executeButton, "button", EXECUTE_ID);
        executeButton.setMessage(Text.translatable(BaseFzmmScreen.getOptionBaseTranslationKey(this.baseScreenTranslationKey) + "execute"));

        this.cancelButton = rootComponent.childById(ButtonComponent.class, CANCEL_ID);
        checkNull(this.cancelButton, "button", CANCEL_ID);
        this.cancelButton.onPress(buttonComponent -> this.close());

        this.loadingBarLayout = rootComponent.childById(FlowLayout.class, LOADING_BAR_ID);
        checkNull(this.loadingBarLayout, "flow-layout", LOADING_BAR_ID);

        this.loadingLabel = rootComponent.childById(LabelComponent.class, LOADING_LABEL_ID);
        checkNull(this.loadingLabel, "label", LOADING_LABEL_ID);

        FlowLayout infoLabels = rootComponent.childById(FlowLayout.class, INFO_LABELS_ID);
        checkNull(infoLabels, "flow-layout", INFO_LABELS_ID);

        infoLabels.children(this.getInfoLabels());

        executeButton.onPress(buttonComponent -> this.execute());
    }

    protected abstract List<Component> getInfoLabels();

    public void execute() {
        assert this.client != null;
        assert this.client.player != null;
        this.client.execute(() -> {
            //noinspection resource
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            PlayerStatuePlacerScreen.isActive = true;
            this.cancelButton.active = false;

            SimpleOption<Boolean> sneakToggled = this.client.options.getSneakToggled();
            boolean isSneakToggled = sneakToggled.getValue();
            sneakToggled.setValue(true);
            this.client.options.sneakKey.setPressed(true);

            List<ItemStack> items = new ArrayList<>(this.getItems());
            items.add(null);
            int containerItemsSize = items.size();

            // Update the hand item first, this is to avoid that some servers when
            // using auto placer the first item becomes the block used to open auto placer.
            if (items.size() > 1) {
                scheduler.schedule(() -> ItemUtils.updateHand(items.get(0)), 0, TimeUnit.MILLISECONDS);
            }

            for (int i = 1; i < containerItemsSize; i++) {
                @Nullable
                ItemStack itemStack = items.get(i);
                int index = i;

                scheduler.schedule(() -> this.execute(itemStack, index, containerItemsSize),
                        (index + 1) * (long) DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS
                );
            }

            scheduler.schedule(() -> {
                ItemUtils.updateHand(this.getFinalStack());

                PlayerStatuePlacerScreen.isActive = false;
                this.cancelButton.active = true;
                Text backText = Text.translatable("fzmm.gui.button.back");
                this.cancelButton.setMessage(backText);
                this.cancelButton.horizontalSizing(Sizing.fixed(this.client.textRenderer.getWidth(backText) + BaseFzmmScreen.BUTTON_TEXT_PADDING));

                sneakToggled.setValue(isSneakToggled);
                this.client.options.sneakKey.setPressed(false);
            }, (containerItemsSize + 2) * (long) DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);

            scheduler.shutdown();
        });
    }

    private void execute(@Nullable ItemStack itemStack, int index, int containerItemsSize) {
        assert this.client != null;

        this.client.doItemUse();
        if (itemStack != null) {
            ItemUtils.updateHand(itemStack);
        }

        this.updateLoadingBar(index, containerItemsSize);
    }

    protected void updateLoadingBar(int index, int maxIndex) {
        int percent = (int) (((index + 1) / (float) maxIndex) * 100);
        this.loadingBarLayout.horizontalSizing(Sizing.fill(percent));
        this.loadingLabel.text(Text.literal(percent + "%"));
    }

    protected abstract ItemStack getFinalStack();

    protected abstract List<ItemStack> getItems();

    protected abstract boolean isActive();

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (this.isActive()) {
            return false;
        }

        return super.shouldCloseOnEsc();
    }
}
