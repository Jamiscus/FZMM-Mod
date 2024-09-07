package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.HistoryScreen;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.logic.FzmmHistory;
import fzmm.zailer.me.mixin.combined_inventory_getter.PlayerInventoryAccessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.Components;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

public class FzmmUtils {

    public static final SuggestionProvider<FabricClientCommandSource> SUGGESTION_PLAYER = (context, builder) -> {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        String playerInput = builder.getRemainingLowerCase();
        if (clientPlayer != null) {
            List<String> playerNamesList = clientPlayer.networkHandler.getPlayerList().stream()
                    .map(PlayerListEntry::getProfile)
                    .map(GameProfile::getName)
                    .toList();

            for (String playerName : playerNamesList) {
                if (playerName.toLowerCase().contains(playerInput))
                    builder.suggest(playerName);
            }
        }

        return CompletableFuture.completedFuture(builder.build());

    };

    /**
     * Process the hand item to be able to edit it
     *
     * @return Hand item copy and ready to be modified
     */
    public static ItemStack getHandStack(Hand hand) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getStackInHand(hand);
        return processStack(stack);
    }

    public static void giveItem(ItemStack stack) {
        SnackBarManager snackBarManager = SnackBarManager.getInstance();
        snackBarManager.remove(SnackBarManager.GIVE_ID);
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        if (FzmmClient.CONFIG.general.giveItemSizeLimit()) {
            long stackSize = getLengthInBytes(stack);
            long inventorySize = getInventorySizeInBytes();
            if ((stackSize + inventorySize) > 8000000) {
                snackBarManager.add(BaseSnackBarComponent.builder(SnackBarManager.GIVE_ID)
                        .title(Text.translatable("fzmm.giveItem.error"))
                        .details(Text.translatable("fzmm.giveItem.exceedLimit",
                                getLengthInKB(stackSize + inventorySize),
                                getLengthInKB(8000000L)
                        ))
                        .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                        .keepOnLimit()
                        .button(snackBar -> Components.button(Text.translatable("fzmm.gui.title.configs.icon"),
                                buttonComponent -> {
                                    client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, client.currentScreen));
                                    snackBar.close();
                                }))
                        .highTimer()
                        .startTimer()
                        .closeButton()
                        .expandDetails()
                        .build()
                );

                FzmmClient.LOGGER.warn("[FzmmUtils] An attempt was made to give an item with size of {} bytes (with {} bytes already in inventory)",
                        stackSize, inventorySize);
                return;
            }
        }

        FzmmHistory.add(stack);

        if (FzmmClient.CONFIG.general.checkValidCodec() && !isCodecValid(stack)) {
            snackBarManager.add(BaseSnackBarComponent.builder(SnackBarManager.GIVE_ID)
                    .title(Text.translatable("fzmm.giveItem.error"))
                    .details(Text.translatable("fzmm.giveItem.codecError"))
                    .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .keepOnLimit()
                    .button(snackBar -> Components.button(Text.translatable("fzmm.gui.title.configs.icon"),
                            buttonComponent -> {
                                client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, client.currentScreen));
                                snackBar.close();
                            }))
                    .highTimer()
                    .startTimer()
                    .closeButton()
                    .expandDetails()
                    .build()
            );
            FzmmClient.LOGGER.warn("[FzmmUtils] An item with an invalid codec was found: {}", stack.getComponents().toString());
            return;
        }

        if (!isAllowedToGive()) {
            snackBarManager.add(BaseSnackBarComponent.builder(SnackBarManager.GIVE_ID)
                    .title(Text.translatable("fzmm.giveItem.error"))
                    .details(Text.translatable("fzmm.giveItem.notAllowed"))
                    .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .keepOnLimit()
                    .button(snackBar -> Components.button(Text.translatable("fzmm.gui.title.history"),
                            buttonComponent -> {
                                setScreen(new HistoryScreen(client.currentScreen));
                                snackBar.close();
                            }))
                    .highTimer()
                    .startTimer()
                    .closeButton()
                    .expandDetails()
                    .build()
            );
        } else if (FzmmClient.CONFIG.general.giveClientSide()) {
            client.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else {
            assert client.interactionManager != null;
            PlayerInventory playerInventory = client.player.getInventory();

            playerInventory.addPickBlock(stack);
            updateHand(stack);
        }
    }

    /**
     * Process the item to be able to edit it
     *
     * @return The item ready to be modified
     */
    public static ItemStack processStack(ItemStack stack) {
        ItemStack stackCopy = stack.copy();
        if (!FzmmClient.CONFIG.general.removeViaVersionTags()) {
            return stackCopy;
        }
        stackCopy.apply(DataComponentTypes.CUSTOM_DATA, null, nbtComponent -> {
            if (nbtComponent == null) {
                return NbtComponent.DEFAULT;
            }

            NbtCompound customTag = nbtComponent.copyNbt();

            // This affects multiplayer when the server is on a lower version and ViaVersion is used.
            //
            // When removing ViaVersion tags, the cached version for ViaVersion is deleted.
            // These cached versions are used for players on older versions, but these tags
            // are more important than those for the higher version. Consequently, if you
            // modify an item with these tags, it will later revert to the cached version, losing the changes.
            recursiveRemoveTags(customTag, s -> s.startsWith("VV|Protocol"));

            return customTag.getKeys().isEmpty() ? NbtComponent.DEFAULT : NbtComponent.of(customTag);
        });

        return stackCopy;
    }

    public static void recursiveRemoveTags(NbtCompound tags, Predicate<String> keyPredicate) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : tags.getKeys()) {
            if (keyPredicate.test(key)) {
                keysToRemove.add(key);
                continue;
            }

            NbtElement value = tags.get(key);
            if (value instanceof NbtCompound compound) {
                recursiveRemoveTags(compound, keyPredicate);
                continue;
            }

            if (value instanceof NbtList list) {
                for (var element : list) {
                    if (element instanceof NbtCompound compoundElement) {
                        recursiveRemoveTags(compoundElement, keyPredicate);
                    }
                }
            }
        }

        for (String key : keysToRemove) {
            tags.remove(key);
        }
    }

    private static long getInventorySizeInBytes() {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        List<DefaultedList<ItemStack>> combinedInventory = ((PlayerInventoryAccessor) client.player.getInventory()).getCombinedInventory();
        long size = 0;

        for (DefaultedList<ItemStack> defaultedList : combinedInventory) {
            for (ItemStack itemStack : defaultedList) {
                size += getLengthInBytes(itemStack);
            }
        }

        return size;
    }

    public static boolean isCodecValid(ItemStack stack) {
        try {
            CreativeInventoryActionC2SPacket packet = new CreativeInventoryActionC2SPacket(0, stack);
            ByteBuf buf = Unpooled.buffer();
            RegistryByteBuf registryByteBuf = new RegistryByteBuf(buf, getRegistryManager());
            CreativeInventoryActionC2SPacket.CODEC.encode(registryByteBuf, packet);
            CreativeInventoryActionC2SPacket.CODEC.decode(registryByteBuf);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public static void updateHand(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.interactionManager != null;
        assert client.player != null;

        PlayerInventory playerInventory = client.player.getInventory();
        client.interactionManager.clickCreativeStack(stack, PlayerInventory.MAIN_SIZE + playerInventory.selectedSlot);
    }

    public static MutableText disableItalicConfig(MutableText message) {
        Style style = message.getStyle();

        if (FzmmClient.CONFIG.general.disableItalic() && !style.isItalic()) {
            message.setStyle(style.withItalic(false));
        }

        return message;
    }

    public static String getLengthInKB(long length) {
        return new DecimalFormat("#,##0.0").format(length / 1024f);
    }


    public static long getLengthInBytes(ItemStack stack) {
        ByteCountDataOutput byteCountDataOutput = ByteCountDataOutput.getInstance();

        try {
            DynamicRegistryManager registryManager = getRegistryManager();
            NbtIo.write(stack.encode(registryManager), byteCountDataOutput);
        } catch (Exception ignored) {
            return 0;
        }

        long count = byteCountDataOutput.getCount();
        byteCountDataOutput.reset();
        return count;
    }

    public static MutableText disableItalicConfig(String string, boolean useDisableItalicConfig) {
        return disableItalicConfig(Text.literal(string), useDisableItalicConfig);
    }

    public static MutableText disableItalicConfig(MutableText text, boolean useDisableItalicConfig) {
        if (useDisableItalicConfig) {
            return disableItalicConfig(text);
        }
        return text;
    }

    public static String getPlayerUuid(String name) throws IOException, JsonIOException {
        try (var httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name);

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if (((response.getStatusLine().getStatusCode() / 100) != 2) || resEntity == null) {
                return "";
            }

            InputStream inputStream = resEntity.getContent();
            JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(inputStream));
            return obj.get("id").getAsString();
        }
    }

    public static Item getItem(String value) {
        return Registries.ITEM.getOrEmpty(Identifier.of(value)).orElse(Items.STONE);
    }

    public static boolean isAllowedToGive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager == null) {
            return false;
        }

        return client.interactionManager.getCurrentGameMode().isCreative()
                || FzmmClient.CONFIG.general.giveClientSide();
    }

    /**
     * Splits the characters of a message correctly including multibyte characters correctly
     */
    public static List<String> splitMessage(String message) {
        List<String> characters = new ArrayList<>(message.length());
        for (int i = 0; i < message.length(); ) {
            int codePoint = message.codePointAt(i);
            characters.add(new String(Character.toChars(codePoint)));
            i += Character.charCount(codePoint);
        }
        return characters;
    }

    public static int getMaxWidth(Collection<StringVisitable> collection) {
        return getMaxWidth(collection, stringVisitable -> stringVisitable);
    }

    /**
     * @param widthGetter Object is either StringVisitable or OrderedText
     */
    public static <T> int getMaxWidth(Collection<T> collection, Function<T, Object> widthGetter) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int max = 0;

        for (T t : collection) {
            // is an object because a generic object with both interfaces gives this
            // Ambiguous method call. Both getWidth (StringVisitable)
            // in TextRenderer and getWidth (OrderedText) in TextRenderer
            //
            // and 2 methods with polymorphism are not compatible since only one
            // data type within the Function varies, and it detects it as the same method signature
            Object text = widthGetter.apply(t);
            int width;

            if (text instanceof StringVisitable) {
                width = textRenderer.getWidth((StringVisitable) text);
            } else {
                width = textRenderer.getWidth((OrderedText) text);
            }
            max = Math.max(max, width);
        }

        return max;
    }

    public static DyeColor[] getDyeColorsInOrder() {
        DyeColor[] result = new DyeColor[]{
                DyeColor.WHITE,
                DyeColor.LIGHT_GRAY,
                DyeColor.GRAY,
                DyeColor.BLACK,
                DyeColor.BROWN,
                DyeColor.RED,
                DyeColor.ORANGE,
                DyeColor.YELLOW,
                DyeColor.LIME,
                DyeColor.GREEN,
                DyeColor.CYAN,
                DyeColor.LIGHT_BLUE,
                DyeColor.BLUE,
                DyeColor.PURPLE,
                DyeColor.MAGENTA,
                DyeColor.PINK
        };

        return addToArray(result, DyeColor.values());
    }

    public static Formatting[] getFormattingColorsInOrder() {
        Formatting[] result = new Formatting[]{
                Formatting.WHITE,
                Formatting.GRAY,
                Formatting.DARK_GRAY,
                Formatting.BLACK,
                Formatting.DARK_RED,
                Formatting.RED,
                Formatting.GOLD,
                Formatting.YELLOW,
                Formatting.GREEN,
                Formatting.DARK_GREEN,
                Formatting.DARK_AQUA,
                Formatting.AQUA,
                Formatting.BLUE,
                Formatting.DARK_BLUE,
                Formatting.DARK_PURPLE,
                Formatting.LIGHT_PURPLE,
        };

        return addToArray(result, Formatting.values());
    }

    private static <T> T[] addToArray(T[] sortedArray, T[] allArray) {
        int lastIndex = sortedArray.length;
        sortedArray = Arrays.copyOf(sortedArray, allArray.length);

        List<T> notFound = new ArrayList<>(Arrays.asList(allArray));
        notFound.removeAll(Arrays.asList(sortedArray));

        for (var value : notFound) {
            sortedArray[lastIndex++] = value;
        }

        return sortedArray;
    }

    public static DynamicRegistryManager getRegistryManager() {
        assert MinecraftClient.getInstance().player != null;
        return MinecraftClient.getInstance().player.getRegistryManager();
    }

    public static void setScreen(BaseFzmmScreen screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ISnackBarScreen snackBarScreen) {
            snackBarScreen.setScreen(screen);
        } else {
            client.setScreen(screen);
            SnackBarManager.getInstance().moveToScreen(screen);
        }
    }
}