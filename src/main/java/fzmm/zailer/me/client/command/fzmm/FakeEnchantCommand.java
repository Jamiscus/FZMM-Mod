package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class FakeEnchantCommand implements ISubCommand {
    @Override
    public String alias() {
        return "fakeenchant";
    }

    @Override
    public String syntax() {
        return "fakeenchant <enchantment> <level>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();

            addFakeEnchant(enchant, 1);
            return 1;

        }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer()).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();
            int level = ctx.getArgument("level", int.class);

            addFakeEnchant(enchant, level);
            return 1;
        }))).build();
    }

    private static void addFakeEnchant(Enchantment enchant, int level) {
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);
        MutableText enchantMessage = (MutableText) enchant.getName(level);

        Style style = enchantMessage.getStyle().withItalic(false);
        enchantMessage.getSiblings().forEach(text -> {
            if (!text.getString().isBlank())
                ((MutableText) text).setStyle(style);
        });

        stack = DisplayBuilder.of(stack).addLore(enchantMessage).get();

        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            NbtList enchantments = new NbtList();
            enchantments.add(new NbtCompound());
            tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        }

        ItemUtils.give(stack);
    }
}
