package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
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
        return builder.then(ClientCommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {

            @SuppressWarnings("unchecked")
            RegistryEntry.Reference<Enchantment> enchant = ctx.getArgument("enchantment", RegistryEntry.Reference.class);

            this.addFakeEnchant(enchant, 1);
            return 1;
        }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer()).executes(ctx -> {

            @SuppressWarnings("unchecked")
            RegistryEntry.Reference<Enchantment> enchant = ctx.getArgument("enchantment", RegistryEntry.Reference.class);
            int level = ctx.getArgument("level", int.class);

            this.addFakeEnchant(enchant, level);
            return 1;
        }))).build();
    }

    private void addFakeEnchant(RegistryEntry.Reference<Enchantment> enchant, int level) {
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);

        stack.apply(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, null, component -> true);

        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> {
            List<Text> lines = new ArrayList<>();

            MutableText enchantMessage = Enchantment.getName(enchant, level).copy();
            enchantMessage = Enchantment.getName(enchant, level).copy().setStyle(enchantMessage.getStyle().withItalic(false));
            Style style = enchantMessage.getStyle();

            enchantMessage.getSiblings().forEach(text -> {
                if (!text.getString().isBlank())
                    ((MutableText) text).setStyle(style);
            });

            lines.add(enchantMessage);
            lines.addAll(component.lines());

            return new LoreComponent(List.copyOf(lines));
        });

        ItemUtils.give(stack);
    }
}
