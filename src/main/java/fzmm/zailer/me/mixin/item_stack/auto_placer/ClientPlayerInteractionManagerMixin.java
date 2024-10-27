package fzmm.zailer.me.mixin.item_stack.auto_placer;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fzmm.zailer.me.client.gui.utils.auto_placer.AutoPlacerHud;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
     * - and coincides with an auto-placer
     */
    @WrapWithCondition(
            method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V")
    )
    public boolean fzmm$openAutoPlacerHud(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
        assert this.client.player != null;

        ItemStack stack = this.client.player.getMainHandStack();

        if (this.client.player.isSneaking() || ItemUtils.isNotAllowedToGive() && !stack.getComponents().isEmpty()) {
            return true;
        }

        return !AutoPlacerHud.check(stack);
    }

    /**
     * Fixes related to the mixin {@link #fzmm$openAutoPlacerHud(ClientPlayerInteractionManager, ClientWorld, SequencedPacketCreator)}
     * which cancels {@link ClientPlayerInteractionManager#sendSequencedPacket(ClientWorld, SequencedPacketCreator)}
     * and the return value could be null if it returns false (indicating that the auto-placer HUD is opened)
     */
    @SuppressWarnings("JavadocReference")
    @ModifyVariable(
            method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            at = @At("STORE")
    )
    private MutableObject<ActionResult> fzmm$initActionResult(MutableObject<ActionResult> mutableObject) {
        mutableObject.setValue(ActionResult.PASS);
        return mutableObject;
    }

}
