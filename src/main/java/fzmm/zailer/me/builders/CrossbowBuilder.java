package fzmm.zailer.me.builders;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class CrossbowBuilder {

    private final ItemStack stack;
    private final List<ItemStack> chargedProjectiles;

    private CrossbowBuilder() {
        this.stack = Items.CROSSBOW.getDefaultStack();
        this.chargedProjectiles = new ArrayList<>();
    }

    public static CrossbowBuilder builder() {
        return new CrossbowBuilder();
    }

    public CrossbowBuilder putProjectile(ItemStack projectile) {
        this.chargedProjectiles.add(projectile);
        return this;
    }

    public ItemStack get() {
        this.stack.apply(DataComponentTypes.CHARGED_PROJECTILES, null,
                component -> ChargedProjectilesComponent.of(new ArrayList<>(this.chargedProjectiles)));
        return stack.copy();
    }

}
