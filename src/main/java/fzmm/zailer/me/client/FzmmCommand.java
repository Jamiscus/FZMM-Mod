package fzmm.zailer.me.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.argument_type.VersionArgumentType;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.skin.GetSkinDecorator;
import fzmm.zailer.me.utils.skin.GetSkinFromCache;
import fzmm.zailer.me.utils.skin.GetSkinFromMineskin;
import fzmm.zailer.me.utils.skin.GetSkinFromMojang;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.*;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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

                    DisplayBuilder.renameHandItem(name.copy());
                    return 1;
                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("lore")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lore.help", BASE_COMMAND + " lore add/remove"))
                .then(ClientCommandManager.literal("add")
                        .executes(ctx -> sendHelpMessage("commands.fzmm.lore.add.help", BASE_COMMAND + " lore add <message>"))
                        .then(ClientCommandManager.argument("message", TextArgumentType.text(registryAccess)).executes(ctx -> {

                            Text message = ctx.getArgument("message", Text.class);

                            DisplayBuilder.addLoreToHandItem(message.copy());
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

        fzmmCommand.then(ClientCommandManager.literal("old_give")
                .executes(ctx -> sendHelpMessage("commands.fzmm.old_give.help", BASE_COMMAND + " old_give <item> <nbt> <version_code> or old_give <item> <damage> <nbt> <version_code>"))
                .then(ClientCommandManager.argument("item", IdentifierArgumentType.identifier()).executes((ctx) -> {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("commands.fzmm.old_give.nbt_required").formatted(Formatting.RED));
                            return 1;
                        }).then(ClientCommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes((ctx) -> {

                            Identifier item = ctx.getArgument("item", Identifier.class);
                            NbtCompound nbt = NbtCompoundArgumentType.getNbtCompound(ctx, "nbt");

                            oldGiveItem(item, nbt, VersionArgumentType.VERSIONS.get(0));
                            return 1;
                        }).then(ClientCommandManager.argument("item_version", VersionArgumentType.version()).executes(ctx -> {
                            Identifier item = ctx.getArgument("item", Identifier.class);
                            NbtCompound nbt = NbtCompoundArgumentType.getNbtCompound(ctx, "nbt");
                            Pair<String, Integer> version = VersionArgumentType.getVersion(ctx, "item_version");

                            oldGiveItem(item, nbt, version);
                            return 1;
                        }))).then(ClientCommandManager.argument("damage", IntegerArgumentType.integer()).executes((ctx) -> {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("commands.fzmm.old_give.nbt_required").formatted(Formatting.RED));

                            return 1;
                        }).then(ClientCommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes((ctx) -> {

                            Identifier item = ctx.getArgument("item", Identifier.class);
                            int damage = IntegerArgumentType.getInteger(ctx, "damage");
                            NbtCompound nbt = NbtCompoundArgumentType.getNbtCompound(ctx, "nbt");

                            oldGiveItem(item, damage, nbt, VersionArgumentType.VERSIONS.get(0));
                            return 1;
                        }).then(ClientCommandManager.argument("item_version", VersionArgumentType.version()).executes(ctx -> {
                            Identifier item = ctx.getArgument("item", Identifier.class);
                            int damage = IntegerArgumentType.getInteger(ctx, "damage");
                            NbtCompound nbt = NbtCompoundArgumentType.getNbtCompound(ctx, "nbt");
                            Pair<String, Integer> version = VersionArgumentType.getVersion(ctx, "item_version");

                            oldGiveItem(item, damage, nbt, version);
                            return 1;
                        }))))

                )

        );

        fzmmCommand.then(ClientCommandManager.literal("enchant")
                .executes(ctx -> sendHelpMessage("commands.fzmm.enchant.help", BASE_COMMAND + " enchant <enchantment> <level>"))
                .then(ClientCommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {
                    @SuppressWarnings("unchecked")
                    RegistryEntry.Reference<Enchantment> enchant = ctx.getArgument("enchantment", RegistryEntry.Reference.class);

                    addEnchant(enchant, (short) 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

                    @SuppressWarnings("unchecked")
                    RegistryEntry.Reference<Enchantment> enchant = ctx.getArgument("enchantment", RegistryEntry.Reference.class);
                    int level = ctx.getArgument("level", int.class);

                    addEnchant(enchant, (short) level);
                    return 1;
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("fakeenchant")
                .executes(ctx -> sendHelpMessage("commands.fzmm.fakeenchant.help", BASE_COMMAND + " fakeenchant <enchantment> <level>"))
                .then(ClientCommandManager.argument("enchantment", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {

                    @SuppressWarnings("unchecked")
                    RegistryEntry.Reference<Enchantment> enchant = ctx.getArgument("enchantment", RegistryEntry.Reference.class);

                    addFakeEnchant(enchant, 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer()).executes(ctx -> {

                    @SuppressWarnings("unchecked")
                    RegistryEntry.Reference<Enchantment> enchant = ctx.getArgument("enchantment", RegistryEntry.Reference.class);
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

                    fullContainer(ctx.getArgument("slots to fill", int.class), -1);
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
        FzmmUtils.giveItem(FzmmUtils.processStack(itemStack));
    }

    private static void oldGiveItem(Identifier item, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        oldGiveItem(item, 0, nbtCompound, oldVersion);
    }

    private static void oldGiveItem(Identifier item, int damage, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        CompletableFuture.runAsync(() -> {
            AtomicReference<String> errorStr = new AtomicReference<>(null);
            int oldVersionData = oldVersion.getRight();
            NbtCompound fakeHotbarStorageCompound = getFakeHotbarStorageCompound(item.toString(), damage, nbtCompound, oldVersionData);
            // use data fixers to update nbt from a fake HotbarStorageEntry
            fakeHotbarStorageCompound = DataFixTypes.HOTBAR.update(Schemas.getFixer(), fakeHotbarStorageCompound, oldVersionData);

            // get stack from fake hotbar
            HotbarStorageEntry parsedHotbarStorage = HotbarStorageEntry.CODEC
                    .parse(NbtOps.INSTANCE, fakeHotbarStorageCompound.get(String.valueOf(0)))
                    .resultOrPartial(error -> {
                        errorStr.set(error);
                        FzmmClient.LOGGER.error("[FzmmCommand] Failed to parse update item with '/fzmm old_give': {}", error);
                    })
                    .orElseGet(HotbarStorageEntry::new);

            DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();
            List<ItemStack> result = parsedHotbarStorage.deserialize(registryManager);

            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
            MutableText errorMessage = Text.translatable("commands.fzmm.old_give.error", item.toString(), oldVersion.getLeft()).formatted(Formatting.RED);
            if (errorStr.get() != null) {
                chatHud.addMessage(errorMessage.append("\n").append(Text.literal(errorStr.get())));
            } else if (result.isEmpty() || result.get(0).isEmpty()) {
                chatHud.addMessage(errorMessage);
            } else {
                FzmmUtils.giveItem(FzmmUtils.processStack(result.get(0)));
                chatHud.addMessage(Text.translatable("commands.fzmm.old_give.success", item.toString(), oldVersion.getLeft())
                                .withColor(FzmmClient.CHAT_BASE_COLOR)
                        );
            }
        });
    }

    @NotNull
    private static NbtCompound getFakeHotbarStorageCompound(String item, int damage, NbtCompound nbtCompound, int oldVersion) {
        NbtCompound fakeHotbarStorageCompound = new NbtCompound();

        try {
            int version1204 = VersionArgumentType.parse("1.20.4").getRight();
            String countKey = oldVersion > version1204 ? "count" : "Count";
            String tagKey = oldVersion > version1204 ? "components" : "tag";

            for (int i = 0; i < 9; i++) {
                NbtList entry = new NbtList();
                if (i == 0) {
                    NbtCompound itemCompound = new NbtCompound();
                    itemCompound.putByte(countKey, (byte) 1);
                    itemCompound.putString("id", item);
                    itemCompound.put(tagKey, nbtCompound);
                    if (oldVersion <= VersionArgumentType.parse("1.12.2").getRight()) {
                        itemCompound.putInt("Damage", damage);
                    }

                    entry.add(itemCompound);
                }

                for (int j = entry.size(); j != 9; j++) {
                    NbtCompound emptyCompound = new NbtCompound();
                    emptyCompound.putByte(countKey, (byte) 1);
                    emptyCompound.putString("id", "minecraft:air");

                    entry.add(emptyCompound);
                }

                fakeHotbarStorageCompound.put(String.valueOf(i), entry);
            }
        } catch (CommandSyntaxException e) {
            FzmmClient.LOGGER.error("[FzmmCommand] Failed to get fake hotbar storage compound", e);
            return new NbtCompound();
        }

        return fakeHotbarStorageCompound;
    }


    private static void addEnchant(RegistryEntry.Reference<Enchantment> enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);

        stack.apply(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT, component -> {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(component);
            builder.add(enchant, level);
            return builder.build();
        });

        FzmmUtils.giveItem(stack);
    }

    private static void addFakeEnchant(RegistryEntry.Reference<Enchantment>  enchant, int level) {
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);

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

        // vanilla chat lines = 100
        final int MAX_CHAT_LINES = 90;
        final int MAX_HOVER_LENGTH = 15000;
        String nbtString = toFormatedComponent(nbt.getCompound(TagsConstant.ENCODE_STACK_COMPONENTS), false).getString();
        String nbtStringHover = nbtString;
        int nbtLength = nbtString.length();
        MutableText nbtMessage;

        // check if the message length fits within 90% of the maximum chat lines in vanilla
        // in order to avoid writing a message too long which could cause crash with mods
        // that increase the limit beyond vanilla (and lag spike in vanilla)
        //
        // note: the final result could be more than 90% due to formatting adding spaces.
        if (client.textRenderer.getWidth(nbtString) > ChatHud.getWidth(client.options.getChatWidth().getValue()) * MAX_CHAT_LINES) {
            String message = String.format("[%s]", Text.translatable("commands.fzmm.nbt.tooLong").getString());
            nbtMessage = Text.literal(message).setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        } else {
            nbtMessage = toFormatedComponent(nbt.getCompound(TagsConstant.ENCODE_STACK_COMPONENTS), true);
        }

        // if the hover text is too long it gives a lot of lag with cursor over it (and doesn't fit on the screen)
        if (nbtLength > MAX_HOVER_LENGTH) {
            nbtStringHover = "..." + nbtStringHover.substring(nbtLength - MAX_HOVER_LENGTH, nbtLength);
        }

        Text clickToCopyMessage = Text.literal(" (").append(Text.translatable("commands.fzmm.nbt.click")).append(")")
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        MutableText message = Text.empty()
                .append(Text.literal(stack.getItem().toString())
                        .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR))
                ).append(nbtMessage.copy().setStyle(nbtMessage.getStyle()
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbtString))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(nbtStringHover))))
                        .append(clickToCopyMessage)
                );

        Text length = Text.literal(String.valueOf(nbtLength))
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        MutableText lengthMessage = Text.translatable("commands.fzmm.nbt.length", length)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        client.inGameHud.getChatHud().addMessage(message.append("\n").append(lengthMessage));
    }

    public static MutableText toFormatedComponent(NbtCompound nbt, boolean prettyPrint) {
        MutableText result = Text.literal("[");
        List<Text> componentsText = new ArrayList<>();

        for (var key : nbt.getKeys()) {
            MutableText text = Text.empty();
            NbtElement tag = nbt.get(key);

            if (tag == null) {
                tag = new NbtCompound();
            }

            text.append(Text.literal(key).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_AQUA)));
            text.append(Text.literal("="));
            if (prettyPrint) {
                text.append(NbtHelper.toPrettyPrintedText(tag));
            } else {
                text.append(tag.toString());
            }

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
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);
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

    /**
     * @param firstSlot if -1, it will fill empty slots starting at 0
     */
    private static void fullContainer(int slotsToFill, int firstSlot) {
        ItemStack containerStack = FzmmUtils.getHandStack(Hand.MAIN_HAND);
        ItemStack itemStack = FzmmUtils.getHandStack(Hand.OFF_HAND);

        containerStack.apply(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT, component -> {
            List<ItemStack> stacksCopy = new ArrayList<>(component.stream().toList());

            if (firstSlot == -1) {
                fullContainerEmptySlots(stacksCopy, itemStack, slotsToFill);
            } else {
                fullContainer(stacksCopy, itemStack, slotsToFill, firstSlot);
            }

            return ContainerComponent.fromStacks(stacksCopy);
        });

        FzmmUtils.giveItem(containerStack);
    }

    private static void fullContainer(List<ItemStack> stackList, ItemStack stack, int slotsToFill, int firstSlot) {
        int finalSlot = firstSlot + slotsToFill;
        if (slotsToFill > stackList.size()) {
            for (int i = stackList.size(); i < finalSlot; i++) {
                stackList.add(ItemStack.EMPTY);
            }
        }

        for (int i = firstSlot; i != finalSlot; i++) {
            stackList.set(i, stack);
        }
    }

    private static void fullContainerEmptySlots(List<ItemStack> stackList, ItemStack stack, int slotsToFill) {
        int finalSlot = Math.min(stackList.size() + slotsToFill, ShulkerBoxBlockEntity.INVENTORY_SIZE);
        if (finalSlot > stackList.size()) {
            for (int i = stackList.size(); i < finalSlot; i++) {
                stackList.add(ItemStack.EMPTY);
            }
        }

        for (int i = 0; i != finalSlot; i++) {
            if (stackList.get(i).isEmpty()) {
                stackList.set(i, stack);
                slotsToFill--;
            }

            if (slotsToFill == 0) {
                break;
            }
        }
    }


    private static void lockContainer(String key) {
        MinecraftClient client = MinecraftClient.getInstance();

        ItemStack containerStack = FzmmUtils.getHandStack(Hand.MAIN_HAND);
        ItemStack lockStack = FzmmUtils.getHandStack(Hand.OFF_HAND);

        containerStack.apply(DataComponentTypes.LOCK, ContainerLock.EMPTY, component -> new ContainerLock(key));

        lockStack.apply(DataComponentTypes.CUSTOM_NAME, Text.empty(), component -> Text.literal(key));

        FzmmUtils.giveItem(containerStack);
        assert client.interactionManager != null;
        // PlayerInventory.OFF_HAND_SLOT is 40, but OFF_HAND_SLOT is 45 (PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE)
        client.interactionManager.clickCreativeStack(lockStack, PlayerInventory.MAIN_SIZE + PlayerInventory.getHotbarSize());
    }

    private static void removeLore() {
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);

        LoreComponent loreComponent = stack.getComponents().get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            removeLore(loreComponent.lines().size() - 1);
        }
    }

    private static void removeLore(int lineToRemove) {
        ItemStack stack = FzmmUtils.getHandStack(Hand.MAIN_HAND);

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

        if (!FzmmUtils.isAllowedToGive()) {
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