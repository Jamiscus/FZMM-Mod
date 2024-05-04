package fzmm.zailer.me.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.skin.GetSkinDecorator;
import fzmm.zailer.me.utils.skin.GetSkinFromCache;
import fzmm.zailer.me.utils.skin.GetSkinFromMineskin;
import fzmm.zailer.me.utils.skin.GetSkinFromMojang;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// I want to remove all the commands so that the mod can be used only through gui
public class FzmmCommand {

    private static final String BASE_COMMAND_ALIAS = "fzmm";
    private static final String BASE_COMMAND = "/" + BASE_COMMAND_ALIAS;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> fzmmCommand = ClientCommandManager.literal(BASE_COMMAND_ALIAS);

        fzmmCommand.then(ClientCommandManager.literal("name")
                .executes(ctx -> sendHelpMessage("commands.fzmm.name.help", BASE_COMMAND + " name <item name>"))
                .then(ClientCommandManager.argument("name", TextArgumentType.text(registryAccess)).executes(ctx -> {

                    Text name = ctx.getArgument("name", Text.class);

                    DisplayBuilder.renameHandItem(name);
                    return 1;
                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("lore")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lore.help", BASE_COMMAND + " lore add/remove"))
                .then(ClientCommandManager.literal("add")
                        .executes(ctx -> sendHelpMessage("commands.fzmm.lore.add.help", BASE_COMMAND + " lore add <message>"))
                        .then(ClientCommandManager.argument("id", TextArgumentType.text(registryAccess)).executes(ctx -> {

                            Text message = ctx.getArgument("id", Text.class);

                            DisplayBuilder.addLoreToHandItem(message);
                            return 1;
                        }))
                ).then(ClientCommandManager.literal("remove")
                        .executes(
                                ctx -> {

                                    removeLore();
                                    return 1;
                                }
                        ).then(ClientCommandManager.argument("line", IntegerArgumentType.integer(0, 32767)).executes(ctx -> {

                            removeLore(ctx.getArgument("line", int.class));
                            return 1;
                        }))
                )
        );


        fzmmCommand.then(ClientCommandManager.literal("give")
                .executes(ctx -> sendHelpMessage("commands.fzmm.give.help", BASE_COMMAND + " give <item> <amount>"))
                .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes((ctx) -> {

                    giveItem(ItemStackArgumentType.getItemStackArgument(ctx, "item"), 1);
                    return 1;

                }).then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 127)).executes((ctx) -> {

                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                    ItemStackArgument item = ItemStackArgumentType.getItemStackArgument(ctx, "item");

                    giveItem(item, amount);
                    return 1;
                })))

        );

        fzmmCommand.then(ClientCommandManager.literal("enchant")
                .executes(ctx -> sendHelpMessage("commands.fzmm.enchant.help", BASE_COMMAND + " enchant <enchantment> <level>"))
                .then(ClientCommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {
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
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("fakeenchant")
                .executes(ctx -> sendHelpMessage("commands.fzmm.fakeenchant.help", BASE_COMMAND + " fakeenchant <enchantment> <level>"))
                .then(ClientCommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {

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
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("nbt")
                .executes(ctx -> {
                    showNbt(ctx);
                    return 1;
                })
        );

        fzmmCommand.then(ClientCommandManager.literal("amount")
                .executes(ctx -> sendHelpMessage("commands.fzmm.amount.help", BASE_COMMAND + " amount <value>"))
                .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 64)).executes(ctx -> {

                    int amount = ctx.getArgument("value", int.class);
                    amount(amount);
                    return 1;

                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("skull")
                .executes(ctx -> sendHelpMessage("commands.fzmm.skull.help", BASE_COMMAND + " skull <skull owner> cache/mineskin/mojang"))
                .then(ClientCommandManager.argument("skull owner", StringArgumentType.word()).suggests(FzmmUtils.SUGGESTION_PLAYER)
                        .executes(ctx -> {

                            String skullOwner = ctx.getArgument("skull owner", String.class);
                            getHead(new GetSkinFromCache(new GetSkinFromMojang()), skullOwner);
                            return 1;

                        }).then(ClientCommandManager.literal("cache")
                                .executes(ctx -> {

                                    String skullOwner = ctx.getArgument("skull owner", String.class);
                                    getHead(new GetSkinFromCache(), skullOwner);

                                    return 1;
                                })).then(ClientCommandManager.literal("mineskin")
                                .executes(ctx -> {

                                    String skullOwner = ctx.getArgument("skull owner", String.class);
                                    getHead(new GetSkinFromMineskin().setCacheSkin(skullOwner), skullOwner);

                                    return 1;
                                })).then(ClientCommandManager.literal("mojang")
                                .executes(ctx -> {

                                    String skullOwner = ctx.getArgument("skull owner", String.class);
                                    getHead(new GetSkinFromMojang(), skullOwner);

                                    return 1;
                                }))
                )
        );

        fzmmCommand.then(ClientCommandManager.literal("fullcontainer")
                .executes(ctx -> sendHelpMessage("commands.fzmm.fullcontainer.help", BASE_COMMAND + " fullcontainer <slots to fill> <first slot>"))
                .then(ClientCommandManager.argument("slots to fill", IntegerArgumentType.integer(1, 27)).executes(ctx -> {

                    fullContainer(ctx.getArgument("slots to fill", int.class), 0);
                    return 1;

                }).then(ClientCommandManager.argument("first slot", IntegerArgumentType.integer(0, 27)).executes(ctx -> {

                    int slotsToFill = ctx.getArgument("slots to fill", int.class);
                    int firstSlot = ctx.getArgument("first slot", int.class);

                    fullContainer(slotsToFill, firstSlot);
                    return 1;

                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("lock")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lock.help", BASE_COMMAND + " lock <key>"))
                .then(ClientCommandManager.argument("key", StringArgumentType.greedyString()).executes(ctx -> {

                    String key = ctx.getArgument("key", String.class);
                    lockContainer(key);
                    return 1;

                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("equip")
                .executes(ctx -> sendHelpMessage("commands.fzmm.equip.help", BASE_COMMAND + " equip <armor>"))
                .then(ClientCommandManager.literal("head").executes(ctx -> {
                    swapItemWithHand(EquipmentSlot.HEAD);
                    return 1;
                })).then(ClientCommandManager.literal("chest").executes(ctx -> {
                    swapItemWithHand(EquipmentSlot.CHEST);
                    return 1;
                })).then(ClientCommandManager.literal("legs").executes(ctx -> {
                    swapItemWithHand(EquipmentSlot.LEGS);
                    return 1;
                })).then(ClientCommandManager.literal("feet").executes(ctx -> {
                    swapItemWithHand(EquipmentSlot.FEET);
                    return 1;
                }))
        );

        fzmmCommand.executes(ctx -> {
            String subcommands = String.join("/", fzmmCommand.getArguments().stream().map(CommandNode::getName).toList());
            return sendHelpMessage("commands.fzmm.help", BASE_COMMAND + " " + subcommands);
        });

        dispatcher.register(fzmmCommand);
    }

    private static int sendHelpMessage(String infoTranslationKey, String syntax) {
        Text infoTranslation = Text.translatable(infoTranslationKey)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        Text syntaxText = Text.literal(syntax)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        Text translation = Text.translatable("commands.fzmm.help.format", infoTranslation, syntaxText)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        player.sendMessage(translation);
        return 1;
    }

    private static void giveItem(ItemStackArgument item, int amount) throws CommandSyntaxException {
        ItemStack itemStack = item.createStack(amount, false);
        FzmmUtils.giveItem(itemStack);
    }

    private static void addEnchant(Enchantment enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}

        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();

        stack.apply(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT, component -> {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(component);
            builder.add(enchant, level);
            return builder.build();
        });

        FzmmUtils.giveItem(stack);
    }

    private static void addFakeEnchant(Enchantment enchant, int level) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();

        stack.apply(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, null, component -> true);

        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> {
            List<Text> lines = new ArrayList<>();

            MutableText enchantMessage = enchant.getName(level).copy();
            enchantMessage = enchant.getName(level).copy().setStyle(enchantMessage.getStyle().withItalic(false));
            Style style = enchantMessage.getStyle();

            enchantMessage.getSiblings().forEach(text -> {
                if (!text.getString().isBlank())
                    ((MutableText) text).setStyle(style);
            });

            lines.add(enchantMessage);
            lines.addAll(component.lines());

            return new LoreComponent(List.copyOf(lines));
        });

        FzmmUtils.giveItem(stack);
    }

    private static void showNbt(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getInventory().getMainHandStack();
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();

        ComponentChanges components = stack.getComponentChanges();
        if (components.isEmpty() || !(stack.encode(registryManager) instanceof NbtCompound nbt) ||
                !nbt.contains(TagsConstant.ENCODE_STACK_COMPONENTS)) {

            ctx.getSource().sendError(Text.translatable("commands.fzmm.item.withoutNbt"));
            return;
        }

        Text nbtMessage = toPrettyPrintedComponent(nbt.getCompound(TagsConstant.ENCODE_STACK_COMPONENTS));
        String nbtString = nbtMessage.getString();
        Text clickToCopyMessage = Text.translatable("commands.fzmm.nbt.click");

        MutableText message = Text.empty()
                .append(Text.literal(stack.getItem().toString())
                        .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR))
                ).append(nbtMessage.copy().setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbtString))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, clickToCopyMessage))
                ));

        Text length = Text.literal(String.valueOf(nbtString.length()))
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        MutableText lengthMessage = Text.translatable("commands.fzmm.nbt.length", length)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        client.inGameHud.getChatHud().addMessage(message.append("\n").append(lengthMessage));
    }

    public static Text toPrettyPrintedComponent(NbtCompound nbt) {
        MutableText result = Text.literal("[");
        List<Text> componentsText = new ArrayList<>();

        for (var key : nbt.getKeys()) {
            MutableText text = Text.empty();

            text.append(Text.literal(key).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_AQUA)));
            text.append(Text.literal("="));
            text.append(NbtHelper.toPrettyPrintedText(nbt.get(key)));

            componentsText.add(text);
        }

        for (int i = 0; i != componentsText.size(); i++) {
            result.append(componentsText.get(i));

            if (i != componentsText.size() - 1) {
                result.append(", ");
            }
        }

        return result.append("]");
    }

    private static void amount(int amount) {
        assert MinecraftClient.getInstance().player != null;

        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();

        stack.setCount(amount);

        FzmmUtils.updateHand(stack);
    }

    private static void getHead(GetSkinDecorator skinDecorator, String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        Optional<ItemStack> optionalStack = skinDecorator.getHead(playerName);
        FzmmUtils.giveItem(optionalStack.orElseGet(() -> {
            FzmmClient.LOGGER.warn("[FzmmCommand] Could not get head for {}", playerName);
            return Items.PLAYER_HEAD.getDefaultStack();
        }));
    }

    private static void fullContainer(int slotsToFill, int firstSlots) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        ItemStack containerStack = client.player.getMainHandStack();
        ItemStack itemStack = client.player.getOffHandStack();

        containerStack.apply(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT, component -> {
            List<ItemStack> stacksCopy = new ArrayList<>(component.stream().toList());
            int finalSlot = firstSlots + slotsToFill;
            if (slotsToFill > stacksCopy.size()) {
                for (int i = stacksCopy.size(); i < finalSlot; i++) {
                    stacksCopy.add(ItemStack.EMPTY);
                }
            }

            for (int i = firstSlots; i != finalSlot; i++) {
                stacksCopy.set(i, itemStack);
            }

            return ContainerComponent.fromStacks(stacksCopy);
        });

        FzmmUtils.giveItem(containerStack);
    }

    private static void lockContainer(String key) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        ItemStack containerStack = client.player.getMainHandStack();
        ItemStack lockStack = client.player.getOffHandStack();

        containerStack.apply(DataComponentTypes.LOCK, ContainerLock.EMPTY, component -> new ContainerLock(key));

        lockStack.apply(DataComponentTypes.CUSTOM_NAME, Text.empty(), component -> Text.literal(key));

        FzmmUtils.giveItem(containerStack);
        assert client.interactionManager != null;
        client.interactionManager.clickCreativeStack(lockStack, PlayerInventory.OFF_HAND_SLOT + PlayerInventory.getHotbarSize());
    }

    private static void removeLore() {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

        LoreComponent loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            removeLore(loreComponent.lines().size() - 1);
        }
    }

    private static void removeLore(int lineToRemove) {
        assert MinecraftClient.getInstance().player != null;

        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

        if (!stack.getComponents().contains(DataComponentTypes.LORE)) {
            return;
        }

        stack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, component -> {
            List<Text> lines = new ArrayList<>(component.lines());

            if (lines.size() < lineToRemove) {
                return component;
            }

            lines.remove(lineToRemove);

            return new LoreComponent(List.copyOf(lines));
        });

        FzmmUtils.giveItem(stack);
    }

    private static void swapItemWithHand(EquipmentSlot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.interactionManager != null;
        ClientPlayerEntity player = client.player;

        if (!player.isCreative()) {
            FzmmClient.LOGGER.warn("[FzmmCommand] Creative mode is necessary to swap items");
            client.inGameHud.getChatHud().addMessage(Text.translatable("fzmm.item.error.actionNotAllowed").setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR)));
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack handStack = player.getMainHandStack();
        ItemStack armorStack = player.getEquippedStack(slot);
        // I don't know why but at least with ClientPlayerInteractionManager#clickCreativeStack
        // they are placed in the reverse order
        int armorSlotId = Math.abs(slot.getOffsetEntitySlotId(0) - 3) + 5;

        // 5 = crafting slot + crafting result result slot
        client.interactionManager.clickCreativeStack(handStack, armorSlotId);
        client.interactionManager.clickCreativeStack(armorStack, PlayerInventory.MAIN_SIZE + inventory.selectedSlot);
    }
}