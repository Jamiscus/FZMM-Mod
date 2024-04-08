package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class HideFlagsBuilder {

    private ItemStack stack;
    private NbtCompound nbt;

    private HideFlagsBuilder() {
        this.stack = Items.STONE.getDefaultStack();
        this.nbt = new NbtCompound();
    }

    public static HideFlagsBuilder builder() {
        return new HideFlagsBuilder();
    }

    public HideFlagsBuilder of(ItemStack stack) {
        this.stack = stack.copy();
        // don't ask me why air can and has nbt (part 2)
        this.nbt = this.stack.isEmpty() ? new NbtCompound() : this.stack.getOrCreateNbt();
        return this;
    }

    public ItemStack get() {
        NbtCompound nbtResult = this.nbt;

        if (!this.hasFlags())
            nbtResult.remove(TagsConstant.HIDE_FLAGS);

        if (nbtResult.isEmpty())
            nbtResult = null;

        this.stack.setNbt(nbtResult);

        return this.stack;
    }

    public int hideFlags() {
        return this.nbt.getInt(TagsConstant.HIDE_FLAGS);
    }

    public boolean hasFlags() {
        return this.hideFlags() != 0;
    }

    public HideFlagsBuilder hideFlags(int flags) {
        this.nbt.putInt(TagsConstant.HIDE_FLAGS, flags);
        return this;
    }

    public boolean has(ItemStack.TooltipSection flag) {
        return (this.hideFlags() & flag.getFlag()) != 0;
    }

    public HideFlagsBuilder set(ItemStack.TooltipSection flag) {
        int flags = this.hideFlags();
        flags |= flag.getFlag();
        return this.hideFlags(flags);
    }

    public HideFlagsBuilder remove(ItemStack.TooltipSection flag) {
        int flags = this.hideFlags();
        flags &= ~flag.getFlag();
        return this.hideFlags(flags);
    }

    public HideFlagsBuilder setAll(boolean value) {
        int result = 0;
        if (value) {
            for (var flag : ItemStack.TooltipSection.values())
                result |= flag.getFlag();
        }

        return this.hideFlags(result);
    }
}
