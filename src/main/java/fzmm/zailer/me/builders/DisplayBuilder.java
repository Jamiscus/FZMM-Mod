package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayBuilder {
    private ItemStack stack;
    private List<Text> lore = new ArrayList<>();
    @Nullable
    private Text customName = null;

    public DisplayBuilder() {
        this.stack = Items.STONE.getDefaultStack();
    }

    public static DisplayBuilder builder() {
        return new DisplayBuilder();
    }

    public static DisplayBuilder of(ItemStack stack) {
        return builder().stack(stack.copy());
    }

    public static void addLoreToHandItem(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        ItemStack stack = of(mc.player.getMainHandStack())
                .addLore(FzmmUtils.disableItalicConfig(text))
                .get();

        FzmmUtils.giveItem(stack);
    }

    public static void renameHandItem(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        ItemStack stack = client.player.getInventory().getMainHandStack();

        stack.apply(DataComponentTypes.CUSTOM_NAME, null, component -> FzmmUtils.disableItalicConfig(text));
        FzmmUtils.giveItem(stack);
    }

    public DisplayBuilder item(Item item) {
        return this.stack(item.getDefaultStack());
    }

    public DisplayBuilder stack(ItemStack stack) {
        this.stack = stack.copy();
        this.lore = new ArrayList<>(stack.getComponents().getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines());
        this.customName = stack.getComponents().getOrDefault(DataComponentTypes.CUSTOM_NAME, null);

        if (this.customName != null) {
            this.customName = this.customName.copy();
        }

        return this;
    }

    public Text getName() {
        return this.customName == null ? Text.empty() : this.customName;
    }

    public List<Text> getLoreText() {
        List<Text> result = this.stack.getComponents().getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines();

        return new ArrayList<>(result);
    }

    public ItemStack get() {
        if (!this.lore.isEmpty()) {
            if (this.lore.size() > LoreComponent.MAX_LORES) {
                this.lore = this.lore.subList(0, LoreComponent.MAX_LORES - 1);
            }

            this.stack.apply(DataComponentTypes.LORE, null, component -> new LoreComponent(List.copyOf(this.lore)));
        }

        if (this.customName != null) {
            this.stack.apply(DataComponentTypes.CUSTOM_NAME, null, component -> this.customName.copy());
        }

        return this.stack;
    }

    public DisplayBuilder setLore(List<Text> lore) {
        this.lore = lore;
        return this;
    }

    public DisplayBuilder setName(Text name) {
        this.customName = FzmmUtils.disableItalicConfig(name, true);
        return this;
    }

    public DisplayBuilder setName(String name) {
        return this.setName(name, false);
    }

    public DisplayBuilder setName(String name, boolean useDisableItalicConfig) {
        return this.setName(FzmmUtils.disableItalicConfig(name, useDisableItalicConfig));
    }

    public DisplayBuilder setName(Text name, int color) {
        return this.setName(name.getString(), color);
    }

    public DisplayBuilder setName(String name, int color) {
        return this.setName(Text.literal(name).setStyle(Style.EMPTY.withColor(color)));
    }

    public DisplayBuilder addLore(List<Text> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public DisplayBuilder addLore(String[] loreArr) {
        List<Text> loreList = Arrays.stream(loreArr)
                .map(loreLine -> FzmmUtils.disableItalicConfig(loreLine, true))
                .toList();

        return this.addLore(loreList);
    }

    public DisplayBuilder addLore(String lore) {
        return this.addLore(Text.literal(lore));
    }

    public DisplayBuilder addLore(Text lore) {
        this.lore.add(lore);
        return this;
    }

    public DisplayBuilder addLore(Text lore, int messageColor) {
        return this.addLore(lore.copy().setStyle(Style.EMPTY.withColor(messageColor)));
    }

    public DisplayBuilder addLore(String message, int messageColor) {
        return this.addLore(Text.literal(message).setStyle(Style.EMPTY.withColor(messageColor)));
    }
}
