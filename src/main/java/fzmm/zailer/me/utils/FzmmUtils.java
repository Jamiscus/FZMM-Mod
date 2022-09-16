package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fi.dy.masa.malilib.util.Color4f;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.mixin.PlayerSkinTextureAccessor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.resource.Resource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    public static void giveItem(ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        if (getLength(stack) > 1950000) {
            mc.inGameHud.getChatHud().addMessage(Text.translatable("giveItem.exceedLimit").setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return;
        }

        if (Configs.Generic.GIVE_CLIENT_SIDE.getBooleanValue()) {
            mc.player.equipStack(EquipmentSlot.MAINHAND, stack);
        } else {
            assert mc.interactionManager != null;
            PlayerInventory playerInventory = mc.player.getInventory();

            playerInventory.addPickBlock(stack);
            updateHand(stack);
        }
    }

    public static void updateHand(ItemStack stack) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.interactionManager != null;
        assert client.player != null;

        PlayerInventory playerInventory = client.player.getInventory();
        client.interactionManager.clickCreativeStack(stack, PlayerInventory.MAIN_SIZE + playerInventory.selectedSlot);
    }

    public static BufferedImage getImageFromPath(String path) throws IOException {
        File imgFile = new File(path);
        return ImageIO.read(imgFile);
    }

    @Nullable
    public static BufferedImage getImageFromUrl(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return ImageIO.read(url);
    }

    @Nullable
    public static InputStream httpGetRequest(String url, boolean isImage) throws IOException {
        InputStream inputResponse = null;
        try (var httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            if (isImage)
                httpGet.addHeader("content-type", "image/jpeg");

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK)
                inputResponse = resEntity.getContent();

        }

        return inputResponse;
    }

    public static Text disableItalicConfig(Text message) {
        Style style = message.getStyle();

        if (Configs.Generic.DISABLE_ITALIC.getBooleanValue() && !style.isItalic()) {
            ((MutableText) message).setStyle(style.withItalic(false));
        }

        return message;
    }

    public static String getLengthInKB(ItemStack stack) {
        return new DecimalFormat("#,##0.0").format(getLength(stack) / 1024f) + "KB";
    }

    public static long getLength(ItemStack stack) {
        ByteCountDataOutput byteCountDataOutput = ByteCountDataOutput.getInstance();

        try {
            stack.writeNbt(new NbtCompound()).write(byteCountDataOutput);
        } catch (Exception ignored) {
            return 0;
        }

        long count = byteCountDataOutput.getCount();
        byteCountDataOutput.reset();
        return count;
    }

    public static NbtString toNbtString(String string, boolean useDisableItalicConfig) {
        Text text = Text.of(string);
        return toNbtString(text, useDisableItalicConfig);
    }

    public static NbtString toNbtString(Text text, boolean useDisableItalicConfig) {
        if (useDisableItalicConfig)
            disableItalicConfig(text);
        return NbtString.of(Text.Serializer.toJson(text));
    }

    public static void renameHandItem(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        ItemStack stack = mc.player.getInventory().getMainHandStack();
        stack.setCustomName(FzmmUtils.disableItalicConfig(text));
        FzmmUtils.giveItem(stack);
    }

    public static Color4f RGBAtoRGB(Color4f color) {
        return new Color4f(color.r, color.g, color.b, 0);
    }

    @Nullable
    public static BufferedImage getPlayerSkin(String name) throws IOException, NullPointerException, JsonIOException {
        BufferedImage skin = getPlayerSkinFromCache(name);

        return skin == null ? getPlayerSkinFromMojang(name) : skin;
    }

    @Nullable
    public static BufferedImage getPlayerSkinFromMojang(String name) throws IOException {
        String stringUuid = getPlayerUuid(name);
        InputStream inputStream = httpGetRequest("https://sessionserver.mojang.com/session/minecraft/profile/" + stringUuid, false);
        if (inputStream == null)
            return null;

        JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonObject properties = (JsonObject) obj.getAsJsonArray("properties").get(0);

        String valueJsonStr = new String(Base64.getDecoder().decode(properties.get("value").getAsString()));
        obj = (JsonObject) JsonParser.parseString(valueJsonStr);
        String skinUrl = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

        return getImageFromUrl(skinUrl);
    }

    @Nullable
    public static BufferedImage getPlayerSkinFromCache(String name) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ClientPlayNetworkHandler clientPlayNetworkHandler = client.player.networkHandler;
        PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(name);
        if (playerListEntry == null)
            return null;

        Identifier skinIdentifier = playerListEntry.getSkinTexture();
        AbstractTexture texture = client.getTextureManager().getTexture(skinIdentifier);
        // if the player is invisible the texture is not an instance of PlayerSkinTexture
        if (!(texture instanceof PlayerSkinTexture skinTexture))
            return null;

        File skinFile = ((PlayerSkinTextureAccessor) skinTexture).getCacheFile();

        return ImageIO.read(skinFile);
    }

    public static String getPlayerUuid(String name) throws IOException, JsonIOException {
        InputStream response = httpGetRequest("https://api.mojang.com/users/profiles/minecraft/" + name, false);
        if (response == null)
            return "";

        JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(response));
        return obj.get("id").getAsString();
    }

    public static void saveBufferedImageAsIdentifier(BufferedImage bufferedImage, Identifier identifier) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", stream);
        byte[] bytes = stream.toByteArray();

        ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
        data.flip();
        NativeImage img = NativeImage.read(data);
        NativeImageBackedTexture texture = new NativeImageBackedTexture(img);

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.getTextureManager().registerTexture(identifier, texture));
    }

    public static Identifier saveBufferedImageAsIdentifier(BufferedImage bufferedImage) {
        Identifier textureIdentifier = new Identifier(FzmmClient.MOD_ID, UUID.randomUUID().toString());
        try {
            FzmmUtils.saveBufferedImageAsIdentifier(bufferedImage, textureIdentifier);
        } catch (IOException ignored) {
        }
        return textureIdentifier;
    }

    @Nullable
    public static BufferedImage getImageFromIdentifier(Identifier identifier) {
        try {
            Optional<Resource> imageResource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            return imageResource.isEmpty() ? null : ImageIO.read(imageResource.get().getInputStream());
        } catch (IOException ignored) {
            return null;
        }
    }
}