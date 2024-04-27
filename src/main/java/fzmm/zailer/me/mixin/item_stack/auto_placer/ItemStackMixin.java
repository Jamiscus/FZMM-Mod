package fzmm.zailer.me.mixin.item_stack.auto_placer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasNbt();

    @ModifyReturnValue(
            method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;",
            at = @At("RETURN")
    )
    public ActionResult fzmm$openAutoPlacerHud(ActionResult original) {

        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        if (client.player.isSneaking() || !client.player.isCreative() || !this.hasNbt() && client.currentScreen != null) {
            return original;
        }


        ItemStack stack = (ItemStack) ((Object) this);
        if (AutoPlacerHud.check(stack)) {
            return ActionResult.FAIL;
        }

        return original;
    }

}
