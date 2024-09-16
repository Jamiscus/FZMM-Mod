package fzmm.zailer.me.client.logic.resource_pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ResourcePackWriter {
    private String fileName = "resourcepack_" + Util.getFormattedCurrentTime();
    private String description = "";
    private byte[] icon = null;
    private Path from = null;
    private ZipOutputStream resourcePackZip;
    private final Map<Path, byte[]> filesToWrite = new HashMap<>();
    private final Map<Path, byte[]> jsonsToMerge = new HashMap<>();

    public ResourcePackWriter() {
    }

    public ResourcePackWriter from(Path from) {
        try {
            if (!from.toFile().exists()) {
                FzmmClient.LOGGER.warn("[ResourcePackWriter] File '{}' does not exist", from);
                return this;
            }

            if (from.toFile().isDirectory()) {
                FzmmClient.LOGGER.warn("[ResourcePackWriter] File '{}' is a directory", from);
                return this;
            }

            if (!from.toFile().canRead()) {
                FzmmClient.LOGGER.warn("[ResourcePackWriter] File '{}' is not readable", from);
                return this;
            }

            if (!from.toRealPath().startsWith(MinecraftClient.getInstance().getResourcePackDir().toRealPath())) {
                FzmmClient.LOGGER.warn("[ResourcePackWriter] File '{}' is not in the resource pack directory", from);
                return this;
            }

            this.from = from;
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[ResourcePackWriter] Error reading file '{}'", from, e);
        }
        return this;
    }

    public CompletableFuture<Void> write() {
        return CompletableFuture.runAsync(() -> {
            File destination = this.getDestination().resolve(this.fileName + ".zip").toFile();
            if (destination.getParentFile().mkdirs()) {
                FzmmClient.LOGGER.info("[ResourcePackWriter] Created resource pack folder");
            }

            try {
                Path iconPath = Path.of("pack.png");
                if (this.icon != null && !this.filesToWrite.containsKey(iconPath)) {
                    this.filesToWrite.put(iconPath, this.icon);
                }
                this.addMetadata();

                if (this.from != null) {
                    try (var zipInputStream = new ZipInputStream(new FileInputStream(this.from.toFile()))) {
                        this.addFrom(zipInputStream);
                    }
                }
                this.resourcePackZip = new ZipOutputStream(new FileOutputStream(destination));
                for (var entry : this.filesToWrite.entrySet()) {
                    Path path = entry.getKey();
                    this.write(path, entry.getValue());
                }

            } catch (IOException e) {
                FzmmClient.LOGGER.error("[ResourcePackBuilder] Error writing resource pack");
                throw new RuntimeException(e);
            } finally {
                this.icon = null;
                this.filesToWrite.clear();
                if (this.resourcePackZip != null) {
                    try {
                        this.resourcePackZip.close();
                    } catch (IOException e) {
                        FzmmClient.LOGGER.error("[ResourcePackBuilder] Error closing resource pack", e);
                    }
                }
            }

        }, Util.getIoWorkerExecutor());
    }

    private void write(Path path, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(path.toString());
        this.resourcePackZip.putNextEntry(entry);
        this.resourcePackZip.write(data);
        this.resourcePackZip.closeEntry();
    }

    private Path getDestination() {
        return MinecraftClient.getInstance().getResourcePackDir();
    }

    private void addMetadata() {
        Path metadataPath = Path.of("pack.mcmeta");
        if (this.filesToWrite.containsKey(metadataPath)) {
            return;
        }
        int packVersion = SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES);
        JsonObject mcmeta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", packVersion);
        pack.addProperty("description", this.description);
        mcmeta.add("pack", pack);

        this.filesToWrite.put(metadataPath, mcmeta.toString().getBytes());
    }

    private void addFrom(ZipInputStream zipInputStream) throws IOException {
        ZipEntry nextEntry = zipInputStream.getNextEntry();
        while (nextEntry != null) {
            Path jsonPath = Path.of(nextEntry.getName());

            if (this.jsonsToMerge.containsKey(jsonPath)) {
                JsonObject oldJson = JsonParser.parseString(new String(zipInputStream.readAllBytes())).getAsJsonObject();
                JsonObject newJson = JsonParser.parseString(new String(this.jsonsToMerge.get(jsonPath))).getAsJsonObject();
                for (String key : newJson.keySet()) {
                    oldJson.add(key, newJson.get(key));
                }
                this.filesToWrite.put(jsonPath, oldJson.toString().getBytes());
                this.jsonsToMerge.remove(jsonPath);
            } else {
                this.filesToWrite.put(jsonPath, zipInputStream.readAllBytes());
            }

            zipInputStream.closeEntry();
            nextEntry = zipInputStream.getNextEntry();
        }
        this.filesToWrite.putAll(this.jsonsToMerge);
    }

    private String sanitize(String file) {
        String sanitized = file.replaceAll("\\.zip$", "")
                .replaceAll("[^a-zA-Z0-9_-]", "");
        return sanitized.isEmpty() ? this.fileName : sanitized;
    }

    public ResourcePackWriter fileName(String fileName) {
        this.fileName = this.sanitize(fileName);
        return this;
    }

    public ResourcePackWriter description(String description) {
        this.description = description;
        return this;
    }

    public ResourcePackWriter icon(BufferedImage image) {
        this.icon = this.toByteArray(image);
        return this;
    }

    private byte[] toByteArray(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[ResourcePackWriter] Error writing image", e);
        }
        return baos.toByteArray();
    }

    public ResourcePackWriter file(Path path, byte[] data) {
        this.filesToWrite.put(path, data);
        return this;
    }

    public ResourcePackWriter file(Path path, BufferedImage image) {
        this.filesToWrite.put(path, this.toByteArray(image));
        return this;
    }

    public ResourcePackWriter file(Path path, JsonObject json, boolean mergeIfExists) {
        (mergeIfExists ? this.jsonsToMerge : this.filesToWrite).put(path, json.toString().getBytes());
        return this;
    }
}
