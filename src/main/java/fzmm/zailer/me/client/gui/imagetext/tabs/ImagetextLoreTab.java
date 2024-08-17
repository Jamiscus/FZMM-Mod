package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.LoreOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class ImagetextLoreTab implements IImagetextTab {
    private static final String LORE_MODE_ID = "loreMode";
    private EnumWidget loreModeOption;

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        ItemStack stack = this.getStack((LoreOption) this.loreModeOption.getValue());
        Text imagetext = logic.getText();

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
        this.loreModeOption = EnumRow.setup(rootComponent, LORE_MODE_ID, LoreOption.ADD, null);
    }


    @Override
    public IMementoObject createMemento() {
        return new LoreMementoTab((LoreOption) this.loreModeOption.getValue());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        LoreMementoTab memento = (LoreMementoTab) mementoTab;
        this.loreModeOption.setValue(memento.mode);
    }

    private ItemStack getStack(LoreOption option) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);

        if (stack.isEmpty()) {
            stack = FzmmUtils.getItem(FzmmClient.CONFIG.imagetext.defaultItem()).getDefaultStack();
        }

        if (option == LoreOption.REPLACE) {
            stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY).remove(ItemStack.LORE_KEY);
        }

        return stack;
    }

    private record LoreMementoTab(LoreOption mode) implements IMementoObject {
    }
}
