package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.logic.playerStatue.statueHeadSkin.SkinPart;
import fzmm.zailer.me.mixin.PlayerSkinTextureAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Optional;

public class ImageUtils {

    public static Optional<BufferedImage> getImageFromIdentifier(Identifier identifier) {
        try {
            Optional<Resource> imageResource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            return imageResource.isEmpty() ? Optional.empty() : Optional.of(ImageIO.read(imageResource.get().getInputStream()));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<BufferedImage> getPlayerSkin(String name) throws IOException, NullPointerException, JsonIOException {
        Optional<BufferedImage> skin = getPlayerSkinFromCache(name);

        return skin.isEmpty() ? getPlayerSkinFromMojang(name) : skin;
    }

    public static Optional<BufferedImage> getPlayerSkinFromMojang(String name) throws IOException {
        String stringUuid = FzmmUtils.getPlayerUuid(name);
        try (var httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + stringUuid);

            httpGet.addHeader("content-statusType", "image/jpeg");

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity resEntity = response.getEntity();
            if ((response.getStatusLine().getStatusCode() / 100) != 2)
                return Optional.empty();

            InputStream inputStream = resEntity.getContent();
            JsonObject obj = (JsonObject) JsonParser.parseReader(new InputStreamReader(inputStream));
            JsonObject properties = (JsonObject) obj.getAsJsonArray("properties").get(0);

            String valueJsonStr = new String(Base64.getDecoder().decode(properties.get("value").getAsString()));
            obj = (JsonObject) JsonParser.parseString(valueJsonStr);
            String skinUrl = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();

            return getImageFromUrl(skinUrl);
        }
    }

    public static Optional<BufferedImage> getPlayerSkinFromCache(String name) throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ClientPlayNetworkHandler clientPlayNetworkHandler = client.player.networkHandler;
        PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(name);
        if (playerListEntry == null)
            return Optional.empty();

        Identifier skinIdentifier = playerListEntry.getSkinTexture();
        AbstractTexture texture = client.getTextureManager().getTexture(skinIdentifier);
        // if the player is invisible the texture is not an instance of PlayerSkinTexture
        if (!(texture instanceof PlayerSkinTexture skinTexture))
            return Optional.empty();

        File skinFile = ((PlayerSkinTextureAccessor) skinTexture).getCacheFile();

        return Optional.of(getImageFromPath(skinFile.getPath()));
    }

    public static BufferedImage getImageFromPath(String path) throws IOException {
        File imgFile = new File(path);
        return ImageIO.read(imgFile);
    }

    public static Optional<BufferedImage> getImageFromUrl(String urlLocation) throws IOException {
        URL url = new URL(urlLocation);
        return Optional.ofNullable(ImageIO.read(url));
    }

    public static Optional<NativeImage> toNativeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", stream);
            byte[] bytes = stream.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(bytes.length).put(bytes);
            data.flip();
            return Optional.of(NativeImage.read(data));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static boolean isAlexModel(int scale, BufferedImage skin) {
        int color = skin.getRGB((SkinPart.LEFT_ARM.x() + 15) * scale, (SkinPart.LEFT_ARM.y() + 15) * scale);
        int alpha = new Color(color, true).getAlpha();
        return alpha == 0;
    }

    public static BufferedImage convertInSteveModel(BufferedImage skin, int scale) {
        BufferedImage modifiedSkin = convertInSteveModel(skin, SkinPart.LEFT_ARM, scale);
        return convertInSteveModel(modifiedSkin, SkinPart.RIGHT_ARM, scale);
    }

    private static BufferedImage convertInSteveModel(BufferedImage playerSkin, SkinPart skinPart, int scale) {
        return convertInSteveModel(playerSkin, skinPart.x(), skinPart.y(), scale);
    }

    private static BufferedImage convertInSteveModel(BufferedImage skin, int x, int y, int scale) {
        x *= scale;
        y *= scale;
        int imageSize = 64 * scale;
        int space = 4 * scale;
        int steveArmWidth = 4 * scale;
        int alexArmWidth = 3 * scale;
        int skinPartSize = 16 * scale;
        BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        // copy skin
        g2d.drawImage(skin, 0, 0, imageSize, imageSize, 0, 0, imageSize, imageSize, null);
        // clear skin part
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(x, y, skinPartSize, skinPartSize);
        // copy side 1
        g2d.drawImage(skin, x, y + space, x + steveArmWidth, y + skinPartSize, x, y + space, x + steveArmWidth, y + skinPartSize, null);
        // stretching face 2
        g2d.drawImage(skin, x + steveArmWidth, y + space, x + steveArmWidth * 2, y + skinPartSize, x + steveArmWidth, y + space, x + steveArmWidth + alexArmWidth, y + skinPartSize, null);
        // moving face 3
        g2d.drawImage(skin, x + steveArmWidth * 2, y + space, x + steveArmWidth * 3, y + skinPartSize, x + steveArmWidth + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth, y + skinPartSize, null);
        // stretching and moving face 4
        g2d.drawImage(skin, x + steveArmWidth * 3, y + space, x + steveArmWidth * 4, y + skinPartSize, x + steveArmWidth * 2 + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth * 2, y + skinPartSize, null);
        // stretching top/down face 1
        g2d.drawImage(skin, x + space, y, x + steveArmWidth + space, y + space, x + space, y, x + alexArmWidth + space, y + space, null);
        // stretching and moving top/down face 2
        g2d.drawImage(skin, x + space + steveArmWidth, y, x + steveArmWidth * 2 + space, y + space, x + space + alexArmWidth, y, x + alexArmWidth * 2 + space, y + space, null);

        g2d.dispose();
        return bufferedImage;
    }


}
