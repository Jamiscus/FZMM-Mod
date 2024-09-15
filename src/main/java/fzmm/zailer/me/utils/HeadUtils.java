package fzmm.zailer.me.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.skin.GetSkinFromCache;
import io.wispforest.owo.Owo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HeadUtils {
    public static final String MINESKIN_API = "https://api.mineskin.org/";
    private static final String BOUNDARY = UUID.randomUUID().toString();
    private String skinValue;
    private String signature;
    private String url;
    private boolean skinGenerated;
    private int httpResponseCode;
    private int delayForNextInMillis;

    public HeadUtils() {
        this.skinValue = "";
        this.signature = "";
        this.url = "";
        this.skinGenerated = false;
        this.httpResponseCode = 0;
        this.delayForNextInMillis = 6000;
    }

    public HeadBuilder getBuilder() {
        return HeadBuilder.builder()
                .skinValue(this.skinValue)
                .signature(this.signature);
    }

    public String getSkinValue() {
        return this.skinValue;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isSkinGenerated() {
        return this.skinGenerated;
    }

    public int getHttpResponseCode() {
        return this.httpResponseCode;
    }

    public int getDelayForNext(TimeUnit unit) {
        return (int) unit.convert(this.delayForNextInMillis, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<HeadUtils> uploadHead(BufferedImage headSkin, String skinName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection conn = null;
            try {
                FzmmConfig.Mineskin config = FzmmClient.CONFIG.mineskin;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(headSkin, "png", baos);
                byte[] skin = baos.toByteArray();

                URL url = URI.create(MINESKIN_API + "generate/upload").toURL();
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestProperty("User-Agent", FzmmClient.HTTP_USER_AGENT);
                conn.setRequestMethod("POST");
                if (!config.apiKey().isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + config.apiKey());
                }
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

                try (DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream())) {
                    dataOutputStream.writeBytes("--" + BOUNDARY + "\r\n");
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"visibility\"\r\n");
                    dataOutputStream.writeBytes("Content-Type: text/plain\r\n\r\n");
                    dataOutputStream.writeBytes(config.publicSkins() ? "0" : "1");
                    dataOutputStream.writeBytes("\r\n--" + BOUNDARY + "\r\n");
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"head\"\r\n");
                    dataOutputStream.writeBytes("Content-Type: application/x-www-form-urlencoded\r\n\r\n");
                    dataOutputStream.write(skin);
                    dataOutputStream.writeBytes("\r\n--" + BOUNDARY + "--\r\n");
                }

                this.httpResponseCode = conn.getResponseCode();
                if (this.httpResponseCode / 100 == 2) {
                    try (InputStreamReader streamReader = new InputStreamReader(conn.getInputStream())) {
                        StringBuilder stringBuilder = new StringBuilder();
                        int character;
                        while ((character = streamReader.read()) != -1) {
                            stringBuilder.append((char) character);
                        }
                        this.useResponse(stringBuilder.toString());
                        FzmmClient.LOGGER.info("[HeadUtils] '{}' head generated using mineskin", skinName);
                    } catch (NullPointerException e) {
                        FzmmClient.LOGGER.error("[HeadUtils] Failed to get head values from mineskin api", e);
                    }
                } else {
                    FzmmClient.LOGGER.error("[HeadUtils] HTTP error {} generating skin in '{}'", this.httpResponseCode, skinName);
                }
            } catch (IOException e) {
                FzmmClient.LOGGER.error("[HeadUtils] Head '{}' could not be generated", skinName, e);
                this.skinValue = "";
                this.skinGenerated = false;
                if (this.httpResponseCode == 0) {
                    this.httpResponseCode = 400;
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (!this.skinGenerated) {
                this.delayForNextInMillis = 6000;
            }

            return this;
        }, Util.getDownloadWorkerExecutor());
    }

    private void useResponse(String reply) {
        //https://rest.wiki/?https://api.mineskin.org/openapi.yml
        JsonObject json = (JsonObject) JsonParser.parseString(reply);
        JsonObject texture = json.getAsJsonObject("data").getAsJsonObject("texture");
        this.skinValue = texture.get("value").getAsString();
        this.signature = texture.get("signature").getAsString();
        this.url = texture.get("url").getAsString();
        this.skinGenerated = true;
        this.delayForNextInMillis = json.getAsJsonObject("delayInfo").get("millis").getAsInt();
        if (Owo.DEBUG) {
            FzmmClient.LOGGER.info("[DEBUG] [HeadUtils] HTTP Code: {}, Delay: {}, Received response: {}", this.httpResponseCode, this.delayForNextInMillis, reply);
        }
    }

    public static Optional<BufferedImage> getSkin(ItemStack stack) throws IOException {
        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
        if (profileComponent == null) {
            return Optional.empty();
        }

        return new GetSkinFromCache().getSkin(profileComponent.gameProfile());
    }

    public static Optional<SkinTextures> getSkinTextures(ItemStack stack) {
        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
        if (profileComponent == null) {
            return Optional.empty();
        }

        return Optional.of(MinecraftClient.getInstance()
                .getSkinProvider()
                .getSkinTextures(profileComponent.gameProfile())
        );
    }
}
