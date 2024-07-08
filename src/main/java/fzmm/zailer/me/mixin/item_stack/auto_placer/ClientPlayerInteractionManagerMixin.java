package fzmm.zailer.me.mixin.item_stack.auto_placer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    /**
     * Cancels interactBlockInternal if
     * - the player does not shift,
     * - is not in creative,
     * - item block has nbt,
     * - the block is in the main hand
     * - and coincides with an autoplacer
     */
    @WrapWithCondition(
            method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V")
    )
    public boolean fzmm$openAutoPlacerHud(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
        assert this.client.player != null;

        ItemStack stack = this.client.player.getMainHandStack();

        if (this.client.player.isSneaking() || !FzmmUtils.isAllowedToGive() && !stack.getComponents().isEmpty()) {
            return true;
        }

        return !AutoPlacerHud.check(stack);
    }

    /**
     * Fixes that the mixin above these cancels SequencedPacketCreator and the return is not initialized
     */
    @ModifyReturnValue(
            method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            at = @At("RETURN")
    )
    private ActionResult fzmm$avoidNullActionResult(ActionResult original) {
        return original == null ? ActionResult.PASS : original;
    }

}
