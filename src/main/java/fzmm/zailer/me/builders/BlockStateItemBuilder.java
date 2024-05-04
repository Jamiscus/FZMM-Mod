package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class BlockStateItemBuilder {

    private final Item item;
    @Nullable
    private final String itemName;
    private final BlockStateComponent blockStateComponent = new BlockStateComponent(new HashMap<>());

    public BlockStateItemBuilder(Item item, String itemNameTranslationKey) {
        this.item = item;
        this.itemName = Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".item." + itemNameTranslationKey).getString();
    }

    public BlockStateItemBuilder(Item item, String translationKey, Item translationItem) {
        this.item = item;
        this.itemName = Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".item." + translationKey, translationItem.getName().getString()).getString();
    }

    public ItemStack get() {
        DisplayBuilder displayBuilder = DisplayBuilder.builder().stack(this.item.getDefaultStack());
        if (this.itemName != null) {
            int color = FzmmClient.CONFIG.colors.usefulBlockStates().rgb();

            displayBuilder.setName(this.itemName, color)
                    .addLore(Text.translatable(FzmmItemGroup.USEFUL_BLOCK_STATES_BASE_TRANSLATION_KEY + ".place").getString(), color);
        }
        ItemStack stack = displayBuilder.get();

        stack.apply(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT, component -> blockStateComponent);
        return stack;
    }

    public BlockStateItemBuilder add(String key, String value) {
        this.blockStateComponent.properties().put(key, value);
        return this;
    }

    public BlockStateItemBuilder add(String key, boolean value) {
        return this.add(key, String.valueOf(value));
    }

    public BlockStateItemBuilder add(String key, int value) {
        return this.add(key, String.valueOf(value));
    }
}
