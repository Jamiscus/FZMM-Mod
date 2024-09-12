package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.LoreOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.List;

public class ImagetextLoreTab implements IImagetextTab, IImagetextTooltip {
    private static final String LORE_MODE_ID = "loreMode";
    private ContextMenuButton loreModeButton;
    private LoreOption loreMode;

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        ItemStack stack = this.getStack(this.loreMode);
        List<Text> imagetext = logic.getWrappedText();

        DisplayBuilder display = DisplayBuilder.of(stack);
        display.addLore(imagetext).get();

        FzmmUtils.giveItem(display.get());
    }

    @Override
    public String getId() {
        return "lore";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.loreModeButton = rootComponent.childById(ContextMenuButton.class, LORE_MODE_ID);
        BaseFzmmScreen.checkNull(this.loreModeButton, "context-menu-button", LORE_MODE_ID);
        this.loreModeButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : LoreOption.values()) {
                dropdownComponent.button(Text.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateLoreMode(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateLoreMode(LoreOption.ADD);
    }

    private void updateLoreMode(LoreOption mode) {
        this.loreMode = mode;
        this.loreModeButton.setMessage(Text.translatable(this.loreMode.getTranslationKey()));
    }


    @Override
    public Text getTooltip(ImagetextLogic logic) {
        ItemStack stack = this.getStack(this.loreMode);
        int loreSize = stack.getComponents()
                .getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines().size() + logic.getHeight();

        MutableText currentLore = Text.literal(String.valueOf(loreSize));
        if (loreSize > LoreComponent.MAX_LORES) {
           currentLore.setStyle(currentLore.getStyle().withColor(FzmmStyles.TEXT_ERROR_COLOR.rgb()));
        }

        return Text.translatable("fzmm.gui.imagetext.tab.lore.tooltip", currentLore, LoreComponent.MAX_LORES);
    }

    private ItemStack getStack(LoreOption option) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);

        if (stack.isEmpty()) {
            stack = FzmmUtils.getItem(FzmmClient.CONFIG.imagetext.defaultItem()).getDefaultStack();
        }

        return switch (option) {
            case ADD -> stack;
            case REPLACE -> {
                stack.apply(DataComponentTypes.LORE, null, component -> new LoreComponent(List.of()));
                yield stack;
            }
        };
    }

    @Override
    public IMementoObject createMemento() {
        return new LoreMementoTab(this.loreMode);
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        LoreMementoTab memento = (LoreMementoTab) mementoTab;
        this.updateLoreMode(memento.mode);
    }

    private record LoreMementoTab(LoreOption mode) implements IMementoObject {
    }
}
