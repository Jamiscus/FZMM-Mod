package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;

public class GiveItemComponent extends ItemComponent {
    public GiveItemComponent(ItemStack stack) {
        super(stack);
        this.showOverlay(true);

        MinecraftClient client = MinecraftClient.getInstance();
        this.tooltip(stack.getTooltip(
                Item.TooltipContext.DEFAULT,
                client.player,
                client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC
        ));

        this.cursorStyle(CursorStyle.HAND);
        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            FzmmUtils.giveItem(this.stack);
            UISounds.playButtonSound();
            return true;
        });
    }
}
