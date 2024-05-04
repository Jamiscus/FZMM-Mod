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
import fzmm.zailer.me.utils.InventoryUtils;
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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.*;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

// I want to remove all the commands so that the mod can be used only through gui
public class FzmmCommand {

    private static final String BASE_COMMAND_ALIAS = "fzmm";
    private static final String BASE_COMMAND = "/" + BASE_COMMAND_ALIAS;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> fzmmCommand = ClientCommandManager.literal(BASE_COMMAND_ALIAS);

        fzmmCommand.then(ClientCommandManager.literal("name")
                .executes(ctx -> sendHelpMessage("commands.fzmm.name.help", BASE_COMMAND + " name <item name>"))
                .then(ClientCommandManager.argument("name", TextArgumentType.text()).executes(ctx -> {

                    Text name = ctx.getArgument("name", Text.class);

                    DisplayBuilder.renameHandItem(name);
                    return 1;
                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("lore")
                .executes(ctx -> sendHelpMessage("commands.fzmm.lore.help", BASE_COMMAND + " lore add/remove"))
                .then(ClientCommandManager.literal("add")
                        .executes(ctx -> sendHelpMessage("commands.fzmm.lore.add.help", BASE_COMMAND + " lore add <message>"))
                        .then(ClientCommandManager.argument("id", TextArgumentType.text()).executes(ctx -> {

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
                .then(ClientCommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {
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
                .then(ClientCommandManager.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT)).executes(ctx -> {

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

    private static void oldGiveItem(Identifier item, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        oldGiveItem(item, 0, nbtCompound, oldVersion);
    }

    private static void oldGiveItem(Identifier item, int damage, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        CompletableFuture.runAsync(() -> {
            AtomicBoolean error = new AtomicBoolean(false);
            int oldVersionData = oldVersion.getRight();
            NbtCompound fakeHotbarStorageCompound = getFakeHotbarStorageCompound(item.toString(), damage, nbtCompound, oldVersionData);
            // use data fixers to update nbt from a fake HotbarStorageEntry
            fakeHotbarStorageCompound = DataFixTypes.HOTBAR.update(Schemas.getFixer(), fakeHotbarStorageCompound, oldVersionData);

            assert MinecraftClient.getInstance().world != null;
            Optional<ItemStack> result = Optional.empty();
            try {
                NbtCompound stackCompound = fakeHotbarStorageCompound.getList(String.valueOf(0), NbtCompound.COMPOUND_TYPE).getCompound(0);
                result = Optional.of(ItemStack.fromNbt(stackCompound));
            } catch (Exception e) {
                error.set(true);
                FzmmClient.LOGGER.error("[FzmmCommand] Failed to parse update item with '/fzmm old_give'", e);
            }

            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
            MutableText errorMessage = Text.translatable("commands.fzmm.old_give.error", item.toString(), oldVersion.getLeft()).formatted(Formatting.RED);
            if (error.get() || result.isEmpty() || result.get().isEmpty()) {
                chatHud.addMessage(errorMessage);
            } else {
                FzmmUtils.giveItem(result.get());
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


    private static void addEnchant(Enchantment enchant, short level) {
        //{Enchantments:[{message:"minecraft:aqua_affinity",lvl:1s}]}

        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();
        NbtCompound tag = stack.getOrCreateNbt();
        NbtList enchantments = new NbtList();

        if (tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            enchantments = tag.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        }
        enchantments.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchant), level));

        tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        stack.setNbt(tag);
        FzmmUtils.giveItem(stack);
    }

    private static void addFakeEnchant(Enchantment enchant, int level) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getInventory().getMainHandStack();
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

        FzmmUtils.giveItem(stack);
    }

    private static void showNbt(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getInventory().getMainHandStack();

        if (!stack.hasNbt()) {
            ctx.getSource().sendError(Text.translatable("commands.fzmm.item.withoutNbt"));
            return;
        }

        assert stack.getNbt() != null;
        Text nbtMessage = NbtHelper.toPrettyPrintedText(stack.getNbt());
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

        //{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:stone",Count:1b}]}}

        ItemStack containerItemStack = client.player.getInventory().getMainHandStack();
        ItemStack itemStack = client.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList items = fillSlots(new NbtList(), itemStack, slotsToFill, firstSlots);

        blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
        blockEntityTag.putString("id", containerItemStack.getItem().toString());

        if (!(containerItemStack.getNbt() == null)) {
            tag = containerItemStack.getNbt();

            if (!(containerItemStack.getNbt().getCompound(TagsConstant.BLOCK_ENTITY) == null)) {
                items = fillSlots(tag.getCompound(TagsConstant.BLOCK_ENTITY).getList(ShulkerBoxBlockEntity.ITEMS_KEY, 10), itemStack, slotsToFill, firstSlots);
                blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
            }
        }

        tag.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        containerItemStack.setNbt(tag);
        FzmmUtils.giveItem(containerItemStack);
    }

    private static NbtList fillSlots(NbtList slotsList, ItemStack stack, int slotsToFill, int firstSlot) {
        for (int i = 0; i != slotsToFill; i++) {
            InventoryUtils.addSlot(slotsList, stack, i + firstSlot);
        }
        return slotsList;
    }

    private static void lockContainer(String key) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        //{BlockEntityTag:{Lock:"abc"}}

        ItemStack containerItemStack = client.player.getInventory().getMainHandStack();
        ItemStack itemStack = client.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();

        if (containerItemStack.hasNbt() || tag.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
            tag = containerItemStack.getNbt();
            assert tag != null;

            if (tag.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
                tag.getCompound(TagsConstant.BLOCK_ENTITY).putString("Lock", key);
            }

        } else {
            blockEntityTag.putString("Lock", key);
            tag.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        }

        containerItemStack.setNbt(tag);
        itemStack.setCustomName(Text.literal(key));

        FzmmUtils.giveItem(containerItemStack);
        assert client.interactionManager != null;
        client.interactionManager.clickCreativeStack(itemStack, PlayerInventory.OFF_HAND_SLOT + PlayerInventory.getHotbarSize());
    }

    private static void removeLore() {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();

        NbtCompound display = stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);
        if (display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE)) {
            removeLore(display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE).size() - 1);
        }
    }

    private static void removeLore(int lineToRemove) {
        assert MinecraftClient.getInstance().player != null;

        //{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

        ItemStack itemStack = MinecraftClient.getInstance().player.getMainHandStack();

        NbtCompound display = itemStack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);

        if (!display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE))
            return;

        NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        if (lore.size() < lineToRemove)
            return;

        lore.remove(lineToRemove);
        display.put(ItemStack.LORE_KEY, lore);

        itemStack.setSubNbt(ItemStack.DISPLAY_KEY, display);
        FzmmUtils.giveItem(itemStack);
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