package fzmm.zailer.me.mixin.item_group.operator_utilities_without_op;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {

    /**
     * Shows the operator utilities tab if it is enabled in the config,
     * this is to maintain compatibility with vanilla which is broken
     * by the changes to show the tab even if you don't have op
     **/
    @ModifyReturnValue(method = "shouldDisplay()Z", at = @At("RETURN"))
    private boolean fzmm$noDisplayOperatorItemGroup(boolean original) {
        if (((Object) this) == Registries.ITEM_GROUP.get(ItemGroups.OPERATOR))
            return MinecraftClient.getInstance().options.getOperatorItemsTab().getValue();

        return original;
    }

    @ModifyVariable(
            method = "updateEntries(Lnet/minecraft/item/ItemGroup$DisplayContext;)V",
            at = @At(value = "HEAD"),
            index = 1,
            argsOnly = true
    )
    public ItemGroup.DisplayContext fzmm$showOperatorUtilitiesWithoutOp(ItemGroup.DisplayContext value) {

        // displays the operator utilities item group even if you do not have op
        if (((Object) this) == Registries.ITEM_GROUP.get(ItemGroups.OPERATOR)) {
            value = new ItemGroup.DisplayContext(value.enabledFeatures(), true, value.lookup());
        }

        return value;
    }
}
