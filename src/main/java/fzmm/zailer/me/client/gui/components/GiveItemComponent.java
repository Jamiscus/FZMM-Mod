package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.item.ItemStack;

public class GiveItemComponent extends ItemComponent {
    public GiveItemComponent(ItemStack stack) {
        super(stack);
        this.showOverlay(true);

        this.setTooltipFromStack(true);

        this.cursorStyle(CursorStyle.HAND);
        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            ItemUtils.give(this.stack);
            UISounds.playButtonSound();
            return true;
        });
    }
}
