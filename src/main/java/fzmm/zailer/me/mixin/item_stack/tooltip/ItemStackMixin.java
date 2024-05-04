package fzmm.zailer.me.mixin.item_stack.tooltip;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.item.TooltipType;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Unique
    private long fzmm$stackSize = 0;
    @Unique
    private int fzmm$compoundHash = 0;

    @Shadow
    public abstract ItemStack copy();


    @Shadow public abstract ComponentMap getComponents();

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void fzmm$getTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        if (!FzmmClient.CONFIG.general.showItemSize() || !type.isAdvanced())
            return;

        List<Text> tooltipList = cir.getReturnValue();
        for (int i = tooltipList.size() - 1; i > 0; i--) {
            Text tooltipLine = tooltipList.get(i);
            if (tooltipLine.getContent() instanceof TranslatableTextContent translatableTooltipText && translatableTooltipText.getKey().equals("item.components")) {
                tooltipList.add( i + 1, this.fzmm$getSizeMessage());
                return;
            }
        }
    }

    @Unique
    public Text fzmm$getSizeMessage() {
        if (this.fzmm$compoundHash != this.getComponents().hashCode()) {
            this.fzmm$stackSize = FzmmUtils.getLengthInBytes((ItemStack) ((Object) this));
            this.fzmm$compoundHash = this.getComponents().hashCode();
        }

        return (this.fzmm$stackSize > 1023 ?
                Text.translatable("fzmm.item.tooltip.size.kilobytes", FzmmUtils.getLengthInKB(this.fzmm$stackSize)) :
                Text.translatable("fzmm.item.tooltip.size.bytes", this.fzmm$stackSize)
        ).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
    }
}
