package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

public class EnchantCommand implements ISubCommand {
    @Override
    public String alias() {
        return "enchant";
    }

    @Override
    public String syntax() {
        return "enchant <enchantment> <level>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {
            @SuppressWarnings("unchecked")
            Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();

            addEnchant(enchant, (short) 1);
            return 1;

        }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

            @SuppressWarnings("unchecked")
            Enchantment enchant = ((RegistryEntry.Reference<Enchantment>) ctx.getArgument("enchantment", RegistryEntry.Reference.class)).value();
            int level = ctx.getArgument("level", int.class);

            addEnchant(enchant, (short) level);
            return 1;
        }))).build();
    }

    private static void addEnchant(Enchantment enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);
        NbtCompound tag = stack.getOrCreateNbt();
        NbtList enchantments = new NbtList();

        if (tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            enchantments = tag.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        }
        enchantments.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchant), level));

        tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        stack.setNbt(tag);
        ItemUtils.give(stack);
    }
}
