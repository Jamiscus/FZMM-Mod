package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.texture.Sprite;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnchantmentBuilder implements ILevelableBuilder<Enchantment, EnchantmentBuilder.EnchantmentData> {

    private ItemStack stack;
    private final List<EnchantmentData> enchantments;
    private boolean glint;
    private boolean allowDuplicates;

    private EnchantmentBuilder() {
        this.enchantments = new ArrayList<>();
        this.glint = false;
        this.allowDuplicates = true;
        this.stack = Items.DIAMOND_SWORD.getDefaultStack();
    }

    public static EnchantmentBuilder builder() {
        return new EnchantmentBuilder();
    }

    @Override
    public EnchantmentBuilder add(EnchantmentData enchantment) {
        this.enchantments.add(enchantment);
        return this;
    }

    public EnchantmentBuilder add(@Nullable Enchantment enchantment, Identifier id, int level) {
        return this.add(new EnchantmentData(enchantment, id, level));
    }

    public EnchantmentBuilder add(@Nullable Enchantment enchantment, Identifier id) {
        this.add(enchantment, id, 1);
        return this;
    }

    public EnchantmentBuilder addAll(NbtList enchantments) {
        for (var element : enchantments) {
            if (element instanceof NbtCompound compound) {
                Identifier enchantId = EnchantmentHelper.getIdFromNbt(compound);
                if (enchantId == null || enchantId.getPath().isEmpty())
                    continue;

                // is not used EnchantmentHelper.getLevelFromNbt() because it makes a MathHelper.clamp
                int level = compound.getShort(TagsConstant.ENCHANTMENTS_LVL);
                this.add(Registries.ENCHANTMENT.get(enchantId), enchantId, level);
            }
        }

        return this;
    }

    public EnchantmentBuilder remove(Enchantment enchantment) {
        this.enchantments.removeIf(enchantmentData -> {
            Optional<Enchantment> enchantmentOptional = enchantmentData.getValue();
            return enchantmentOptional.isPresent() && enchantmentOptional.get() == enchantment;
        });
        return this;
    }

    @Override
    public EnchantmentBuilder remove(int index) {
        if (index >= 0 && index < this.enchantments.size())
            this.enchantments.remove(index);
        return this;
    }

    @Override
    public EnchantmentBuilder clear() {
        this.enchantments.clear();
        return this;
    }

    @Override
    public EnchantmentData getValue(int index) {
        if (index >= 0 && index < this.enchantments.size())
            return this.enchantments.get(index);
        return new EnchantmentData(Enchantments.AQUA_AFFINITY, Registries.ENCHANTMENT.getId(Enchantments.AQUA_AFFINITY), 1);
    }

    @Override
    public List<EnchantmentData> values() {
        return new ArrayList<>(this.enchantments);
    }

    @Override
    public EnchantmentBuilder values(List<EnchantmentData> enchantments) {
        this.enchantments.clear();
        this.enchantments.addAll(enchantments);
        return this;
    }

    @Override
    public boolean contains(Enchantment enchantment) {
        for (var enchantmentData : this.enchantments) {
            Optional<Enchantment> enchantmentOptional = enchantmentData.getValue();
            if (enchantmentOptional.isPresent() && enchantmentOptional.get() == enchantment)
                return true;

        }
        return false;
    }

    public EnchantmentBuilder removeDuplicates() {
        List<EnchantmentData> newEnchantments = new ArrayList<>();

        for (var effectData : this.enchantments) {
            boolean found = false;
            for (var enchantmentData : newEnchantments) {
                if (effectData.getValue().equals(enchantmentData.getValue())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newEnchantments.add(effectData);
            }
        }

        this.enchantments.clear();
        this.enchantments.addAll(newEnchantments);

        return this;
    }

    public boolean isCompatibleWith(Enchantment enchantment) {
        for (var enchantmentData : this.enchantments) {
            Optional<Enchantment> enchantmentOptional = enchantmentData.getValue();
            if (enchantmentOptional.isPresent() &&
                    enchantment != enchantmentOptional.get() &&
                    !enchantmentOptional.get().canCombine(enchantment))
                return false;
        }
        return true;
    }

    public boolean hasDuplicates() {
        for (int i = 0; i != this.enchantments.size(); i++) {
            for (int j = i + 1; j != this.enchantments.size(); j++) {
                if (i != j && this.enchantments.get(i).getValue() == this.enchantments.get(j).getValue())
                    return true;
            }
        }
        return false;
    }

    public boolean isOverMaxLevel() {
        for (var enchantmentData : this.enchantments) {
            if (this.getMaxLevel(enchantmentData.level) != enchantmentData.level)
                return true;
        }
        return false;
    }

    public boolean onlyCompatibleEnchants() {
        for (var enchantmentData : this.enchantments) {
            Optional<Enchantment> enchantmentOptional = enchantmentData.getValue();
            if (enchantmentOptional.isPresent() && !this.isCompatibleWith(enchantmentOptional.get()))
                return false;
        }
        return true;
    }

    @Override
    public EnchantmentBuilder allowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
        return this;
    }

    public boolean allowDuplicates() {
        return this.allowDuplicates;
    }

    @Override
    public EnchantmentBuilder stack(ItemStack stack) {
        this.stack = stack.copy();

        NbtCompound compound = this.stack.getOrCreateNbt();
        NbtList enchants = compound.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        this.clear().addAll(enchants);

        this.glint(enchants.size() == 1 && enchants.get(0) instanceof NbtCompound nbt && nbt.isEmpty());

        return this.allowDuplicates(this.hasDuplicates());
    }

    @Override
    public ItemStack stack() {
        return this.stack;
    }

    public EnchantmentBuilder glint(boolean glint) {
        this.glint = glint;
        return this;
    }

    public boolean glint() {
        return this.glint;
    }

    public ItemStack get() {
        NbtList enchants = new NbtList();

        if (!this.allowDuplicates)
            this.removeDuplicates();

        if (this.glint && this.enchantments.isEmpty()) {
            enchants.add(new NbtCompound());
        } else {
            for (var enchantmentData : this.enchantments)
                enchants.add(enchantmentData.createNbt());
        }

        if (enchants.isEmpty())
            this.stack.removeSubNbt(ItemStack.ENCHANTMENTS_KEY);
        else
            this.stack.setSubNbt(ItemStack.ENCHANTMENTS_KEY, enchants);

        return this.stack.copy();
    }

    @Override
    public int getMaxLevel(int level) {
        NbtCompound nbt = new NbtCompound();
        // I didn't see a static where the max level is declared,
        // so doing this should at least avoid breaking compatibility
        // if another mod decides to change the max level of the enchants
        EnchantmentHelper.writeLevelToNbt(nbt, level);
        return EnchantmentHelper.getLevelFromNbt(nbt);
    }

    public static final class EnchantmentData implements ILevelable<Enchantment> {
        @Nullable
        private final Enchantment enchantment;
        private final Identifier id;
        private int level;

        public EnchantmentData(@Nullable Enchantment enchantment, Identifier id, int level) {
            this.enchantment = enchantment;
            this.id = id;
            this.level = level;
        }

        public NbtCompound createNbt() {
            int level = MathHelper.clamp(this.level, Short.MIN_VALUE, Short.MAX_VALUE); // avoid short overflow
            return EnchantmentHelper.createNbt(this.id, level);
        }

        @Override
        public Optional<Enchantment> getValue() {
            return Optional.ofNullable(this.enchantment);
        }

        @Override
        public Identifier valueId() {
            return this.id;
        }

        @Override
        public Text getName() {
            Text text = this.getValue()
                    .map(enchantment -> enchantment.getName(1))
                    .orElseGet(() -> Text.literal(this.id.toString()));
            return text.copyContentOnly().setStyle(text.getStyle()); // remove levelable level of levelable name
        }

        @Override
        public String getTranslationKey() {
            return this.getValue()
                    .map(Enchantment::getTranslationKey)
                    .orElseGet(this.id::toTranslationKey);
        }

        @Override
        public boolean isAcceptableItem(ItemStack stack) {
            return this.getValue()
                    .map(enchantment -> enchantment.isAcceptableItem(stack))
                    .orElse(true);
        }

        @Override
        public @Nullable Sprite getSprite() {
            return null;
        }

        @Override
        public boolean canHaveSprite() {
            return false;
        }

        @Override
        public int getLevel() {
            return this.level;
        }

        @Override
        public void setLevel(int level) {
            this.level = level;
        }

        public int getMaxLevel() {

            return this.getValue()
                    .map(Enchantment::getMaxLevel)
                    .orElse(255);
        }
    }
}
