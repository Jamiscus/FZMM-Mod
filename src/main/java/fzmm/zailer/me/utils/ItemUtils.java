package fzmm.zailer.me.utils;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.HistoryScreen;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.logic.FzmmHistory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.Components;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ItemUtils {
    private static final Text GIVE_ITEM_ERROR = Text.translatable("fzmm.giveItem.error");

    /**
     * Process the hand item to be able to edit it
     *
     * @return Hand item copy and ready to be modified
     */
    public static ItemStack from(Hand hand) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getStackInHand(hand);

        return process(stack);
    }

    public static Item from(String value) {
        return Registries.ITEM.getOrEmpty(Identifier.of(value)).orElse(Items.STONE);
    }

    /**
     * @return {@code true} if the item was successfully given
     */
    public static boolean give(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        Optional<ISnackBarComponent> snackBar = canGive(stack);
        if (snackBar.isPresent()) {
            MinecraftClient.getInstance().execute(() ->
                    SnackBarManager.getInstance().remove(SnackBarManager.GIVE_ID).add(snackBar.get())
            );
            return false;
        }


        if (FzmmClient.CONFIG.general.giveClientSide()) {
            client.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else {
            assert client.interactionManager != null;
            PlayerInventory playerInventory = client.player.getInventory();

            playerInventory.addPickBlock(stack);
            updateHand(stack);
        }

        return true;
    }

    /**
     * @return Empty {@link Optional} if the item can be given
     */
    public static Optional<ISnackBarComponent> canGive(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        SnackBarBuilder builder = BaseSnackBarComponent.builder(SnackBarManager.GIVE_ID)
                .title(GIVE_ITEM_ERROR)
                .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                .keepOnLimit()
                .highTimer()
                .startTimer()
                .closeButton()
                .expandDetails();


        if (FzmmClient.CONFIG.general.giveItemSizeLimit()) {
            long stackSize = getLengthInBytes(stack);
            long inventorySize = InventoryUtils.getInventorySizeInBytes();
            if ((stackSize + inventorySize) > 8000000) {
                FzmmClient.LOGGER.warn("[FzmmUtils] An attempt was made to give an item with size of {} bytes (with {} bytes already in inventory)",
                        stackSize, inventorySize);

                return Optional.of(builder.details(Text.translatable("fzmm.giveItem.exceedLimit",
                                getLengthInKB(stackSize + inventorySize),
                                getLengthInKB(8000000L)
                        ))
                        .button(snackBar -> Components.button(Text.translatable("fzmm.gui.title.configs.icon"),
                                buttonComponent -> {
                                    client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, client.currentScreen));
                                    snackBar.close();
                                }))
                        .build());
            }
        }

        FzmmHistory.add(stack);

        if (FzmmClient.CONFIG.general.checkValidCodec() && !isCodecValid(stack)) {
            FzmmClient.LOGGER.warn("[FzmmUtils] An item with an invalid codec was found: {}", stack.getComponents().toString());
            return Optional.of(builder.details(Text.translatable("fzmm.giveItem.codecError"))
                    .backgroundColor(FzmmStyles.ALERT_WARNING_COLOR)
                    .button(snackBar -> Components.button(Text.translatable("fzmm.gui.title.configs.icon"),
                            buttonComponent -> {
                                client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, client.currentScreen));
                                snackBar.close();
                            }))
                    .build()
            );
        }

        if (isNotAllowedToGive()) {
            return Optional.of(builder.details(Text.translatable("fzmm.giveItem.notAllowed"))
                    .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .button(snackBar -> Components.button(Text.translatable("fzmm.gui.title.history"),
                            buttonComponent -> {
                                FzmmUtils.setScreen(new HistoryScreen(client.currentScreen));
                                snackBar.close();
                            }))
                    .build()
            );
        }

        return Optional.empty();
    }

    /**
     * Process the item to be able to edit it
     *
     * @return The item ready to be modified
     */
    public static ItemStack process(ItemStack stack) {
        ItemStack stackCopy = stack.copy();
        if (!FzmmClient.CONFIG.general.removeViaVersionTags()) {
            return stackCopy;
        }
        stackCopy.apply(DataComponentTypes.CUSTOM_DATA, null, nbtComponent -> {
            if (nbtComponent == null) {
                return null;
            }

            NbtCompound customTag = nbtComponent.copyNbt();

            // This affects multiplayer when the server is on a lower version and ViaVersion is used.
            //
            // When removing ViaVersion tags, the cached version for ViaVersion is deleted.
            // These cached versions are used for players on older versions, but these tags
            // are more important than those for the higher version. Consequently, if you
            // modify an item with these tags, it will later revert to the cached version, losing the changes.
            recursiveRemoveTags(customTag, s -> s.startsWith("VV|Protocol"));

            return customTag.getKeys().isEmpty() ? null : NbtComponent.of(customTag);
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

    public static boolean isCodecValid(ItemStack stack) {
        try {
            CreativeInventoryActionC2SPacket packet = new CreativeInventoryActionC2SPacket(0, stack);
            ByteBuf buf = Unpooled.buffer();
            RegistryByteBuf registryByteBuf = new RegistryByteBuf(buf, FzmmUtils.getRegistryManager());
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

    public static String getLengthInKB(long length) {
        return new DecimalFormat("#,##0.0").format(length / 1024f);
    }

    public static long getLengthInBytes(ItemStack stack) {
        ByteCountDataOutput byteCountDataOutput = ByteCountDataOutput.getInstance();

        try {
            DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();
            NbtIo.write(stack.encode(registryManager), byteCountDataOutput);
        } catch (Exception ignored) {
            return 0;
        }

        long count = byteCountDataOutput.getCount();
        byteCountDataOutput.reset();
        return count;
    }

    public static boolean isNotAllowedToGive() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager == null) {
            return true;
        }

        return !(client.interactionManager.getCurrentGameMode().isCreative()
                || FzmmClient.CONFIG.general.giveClientSide());
    }
}
