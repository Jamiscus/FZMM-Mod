package fzmm.zailer.me.mixin.item_stack.tooltip;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
    private long fzmm$nbtSize = 0;
    @Unique
    private int fzmm$nbtHash = 0;

    @Shadow
    public abstract ItemStack copy();

    @Shadow
    public abstract @Nullable NbtCompound getNbt();

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void fzmm$getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (!FzmmClient.CONFIG.general.showItemSize() && context.isAdvanced())
            return;

        List<Text> tooltipList = cir.getReturnValue();
        for (int i = tooltipList.size() - 1; i > 0; i--) {
            Text tooltipLine = tooltipList.get(i);
            if (tooltipLine.getContent() instanceof TranslatableTextContent translatableTooltipText && translatableTooltipText.getKey().equals("item.nbt_tags")) {
                tooltipList.add( i + 1, this.fzmm$getSizeMessage());
                return;
            }
        }
    }

    @Unique
    public Text fzmm$getSizeMessage() {
        NbtCompound nbt = this.getNbt();
        if (nbt != null && this.fzmm$nbtHash != this.getNbt().hashCode()) {
            ItemStack stack = this.copy();
            this.fzmm$nbtHash = this.getNbt().hashCode();
            this.fzmm$nbtSize = ItemUtils.getLengthInBytes(stack);
        }

        return (this.fzmm$nbtSize > 1023 ?
                Text.translatable("fzmm.item.tooltip.size.kilobytes", ItemUtils.getLengthInKB(this.fzmm$nbtSize)) :
                Text.translatable("fzmm.item.tooltip.size.bytes", this.fzmm$nbtSize)
        ).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
    }
}
