package fzmm.zailer.me.client.logic.head_generator;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.*;
import fzmm.zailer.me.client.logic.head_generator.model.steps.*;
import fzmm.zailer.me.client.logic.head_generator.model.steps.select.ModelSelectColorStep;
import fzmm.zailer.me.client.logic.head_generator.model.steps.select.ModelSelectDestinationStep;
import fzmm.zailer.me.client.logic.head_generator.model.steps.select.ModelSelectTextureStep;
import fzmm.zailer.me.client.logic.head_generator.texture.HeadTextureEntry;
import io.wispforest.owo.ui.core.Color;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;

public class HeadResourcesLoader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {

    private static ImmutableList<AbstractHeadEntry> LOADED_RESOURCES = ImmutableList.<AbstractHeadEntry>builder().build();
    public static final String HEADS_TEXTURES_FOLDER = "textures/heads";
    public static final String FZMM_MODELS_FOLDER = "fzmm_models";
    public static final String INTERNAL_FOLDER = "internal";
    public static final String INTERNAL_MODELS_FOLDER = FZMM_MODELS_FOLDER + "/" + INTERNAL_FOLDER;

    public static ImmutableList<AbstractHeadEntry> getPreloaded() {
        return LOADED_RESOURCES;
    }

    public static Optional<AbstractHeadEntry> getByPath(String id) {
        for (AbstractHeadEntry entry : LOADED_RESOURCES) {
            if (entry.getPath().equals(id)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(FzmmClient.MOD_ID, "head-resources-loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        try {
            List<AbstractHeadEntry> builder = new ArrayList<>();

            builder.addAll(loadHeadsModels(manager, INTERNAL_MODELS_FOLDER, true));
            builder.addAll(loadHeadsModels(manager, FZMM_MODELS_FOLDER + "/heads", false));
            builder.addAll(loadHeadsTextures(manager));

            builder.sort(Comparator.comparing(AbstractHeadEntry::isFirstResult)
                    .reversed()
                    .thenComparing(AbstractHeadEntry::getPath));

            LOADED_RESOURCES = ImmutableList.copyOf(builder);

            InternalModels.reload();
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[HeadResourcesLoader] Error reloading resources", e);
        }

        this.validate();
    }

    public void validate() {
        // If there's a model that isn't valid and another model requires it later,
        // an error will occur because it's no longer loaded.
        // Therefore, it's necessary to validate and load it twice the same amount.
        int previousValidResources = -1;
        while (previousValidResources != LOADED_RESOURCES.size()) {
            List<AbstractHeadEntry> builder = new ArrayList<>();

            for (var entry : LOADED_RESOURCES) {
                try {
                    if (entry instanceof HeadModelEntry modelEntry) {
                        if (modelEntry.validate()) {
                            builder.add(modelEntry);
                        } else {
                            FzmmClient.LOGGER.warn("[HeadResourcesLoader] '{}' is not valid", modelEntry.getPath());
                        }
                    } else {
                        builder.add(entry);
                    }
                } catch (Exception e) {
                    FzmmClient.LOGGER.error("[HeadResourcesLoader] Error validating '{}'", entry.getPath(), e);
                    addChatMessageError(e, entry.getPath());
                }
            }

            previousValidResources = LOADED_RESOURCES.size();
            LOADED_RESOURCES = ImmutableList.copyOf(builder);
        }
    }


    private static Set<HeadTextureEntry> loadHeadsTextures(ResourceManager manager) {
        Set<HeadTextureEntry> entries = new HashSet<>();

        manager.findResources(HEADS_TEXTURES_FOLDER, identifier -> identifier.getPath().endsWith(".png")).forEach(((identifier, resource) -> {
            InputStream inputStream = null;
            try {
                inputStream = resource.getInputStream();
                BufferedImage nativeImage = ImageIO.read(inputStream);
                String path = identifier.getPath();
                String fileName = path.substring(HEADS_TEXTURES_FOLDER.length() + 1, path.length() - ".png".length());

                entries.add(new HeadTextureEntry(nativeImage, fileName));
            } catch (IOException e) {
                FzmmClient.LOGGER.error("[HeadResourcesLoader] Error loading head generator texture", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        FzmmClient.LOGGER.error("[HeadResourcesLoader] Error closing texture input stream", e);
                    }
                }
            }
        }));
        return entries;
    }

    /**
     * @param path     resource pack path since "assets/fzmm/"
     * @param internal if true, it should not be displayed to the user (i.e. it will not be displayed in the head generator).
     */
    private static Set<HeadModelEntry> loadHeadsModels(ResourceManager manager, String path, boolean internal) {
        Set<HeadModelEntry> entries = new HashSet<>();


        manager.findResources(path, identifier -> identifier.getPath().endsWith(".json")).forEach(((identifier, resource) -> {
            InputStream inputStream = null;
            try {
                inputStream = resource.getInputStream();
                entries.add(getHeadModel(path, identifier, inputStream));
            } catch (Exception e) {
                FzmmClient.LOGGER.error("[HeadResourcesLoader] Error loading head generator model: {}", identifier.getPath(), e);

                addChatMessageError(e, identifier.getPath());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        FzmmClient.LOGGER.error("[HeadResourcesLoader] Error closing model input stream", e);
                    }
                }
            }
        }));

        for (HeadModelEntry entry : entries) {
            entry.isInternal(internal);
        }

        return entries;
    }

    public static HeadModelEntry getHeadModel(String basePath, Identifier identifier, InputStream inputStream) {
        String path = identifier.getPath();
        path = path.substring(0, path.length() - ".json".length());
        String[] folders = path.split("/");
        path = folders.length > 1 ? folders[folders.length - 2] + "/" + folders[folders.length - 1] : basePath;

        JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        Optional<ParameterList<BufferedImage>> textures = getParameterList(jsonObject, "textures", HeadResourcesLoader::textureParser);
        Optional<ParameterList<Color>> colors = getParameterList(jsonObject, "colors", HeadResourcesLoader::colorParser);
        Optional<ParameterList<OffsetParameter>> offsets = getParameterList(jsonObject, "offsets", HeadResourcesLoader::offsetParser);
        boolean isPaintableModel = jsonObject.has("paintable") && jsonObject.get("paintable").getAsBoolean();
        boolean isEditingSkinBody = jsonObject.has("is_editing_skin_body") && jsonObject.get("is_editing_skin_body").getAsBoolean();
        boolean isFirstResult = jsonObject.has("first_result") && jsonObject.get("first_result").getAsBoolean();
        boolean isInvertedLeftAndRight = jsonObject.has("inverted_left_and_right") && jsonObject.get("inverted_left_and_right").getAsBoolean();

        JsonArray stepsArray = get(jsonObject, "steps").getAsJsonArray();
        List<IModelStep> steps = new ArrayList<>();

        for (var element : stepsArray) {
            JsonObject stepObject = element.getAsJsonObject();

            steps.add(parseStep(stepObject));
        }

        HeadModelEntry entry = new HeadModelEntry(path, steps, textures.orElse(null),
                colors.orElse(null), offsets.orElse(null));

        entry.isPaintable(isPaintableModel);
        entry.isEditingSkinBody(isEditingSkinBody);
        entry.isFirstResult(isFirstResult);
        entry.isInvertedLeftAndRight(isInvertedLeftAndRight);

        return entry;
    }

    /**
     * the default JsonObject does not show the missing key, it gives a generic error,
     * which makes it difficult to find which required key you did not use.
     * <p/>
     * then it is preferable to use this one, to make debugging easier in case you are creating a model
     */
    public static JsonElement get(JsonObject jsonObject, String key) throws IllegalArgumentException {
        if (jsonObject.has(key)) {
            return jsonObject.get(key);
        } else {
            throw new IllegalArgumentException(String.format("[HeadResourcesLoader] Missing key: '%s', JSON object: %s", key, jsonObject));
        }
    }

    public static IModelStep parseStep(JsonObject stepObject) {
        String id = get(stepObject, "type").getAsString();

        return switch (id) {
            case "copy" -> ModelCopyStep.parse(stepObject);
            case "delete" -> ModelDeleteStep.parse(stepObject);
            case "fill_color" -> ModelFillColorStep.parse(stepObject);
            case "select_color" -> ModelSelectColorStep.parse(stepObject);
            case "select_texture" -> ModelSelectTextureStep.parse(stepObject);
            case "toggle_offset" -> ModelToggleOffsetStep.parse(stepObject);
            case "select_destination" -> ModelSelectDestinationStep.parse(stepObject);
            case "function" -> ModelFunctionStep.parse(stepObject);
            case "condition" -> ModelConditionStep.parse(stepObject);
            default -> data -> FzmmClient.LOGGER.warn("[HeadResourcesLoader] Unknown model step type: {}", id);
        };
    }

    public static <T> Optional<ParameterList<T>> getParameterList(JsonObject jsonObject, String key,
                                                                  Function<JsonObject, IParameterEntry<T>> elementParser) {
        ParameterList<T> result = new ParameterList<>();
        if (!jsonObject.has(key))
            return Optional.empty();

        JsonArray texturesArray = jsonObject.get(key).getAsJsonArray();

        for (var textureElement : texturesArray) {
            JsonObject textureObject = textureElement.getAsJsonObject();
            result.put(elementParser.apply(textureObject));
        }

        return Optional.of(result);
    }

    public static IParameterEntry<BufferedImage> textureParser(JsonObject jsonObject) {
        String id = HeadResourcesLoader.get(jsonObject, "id").getAsString();
        boolean requested = !jsonObject.has("requested") || jsonObject.get("requested").getAsBoolean();
        String defaultValue = jsonObject.has("path") ? jsonObject.get("path").getAsString() : null;

        return new ResettableModelParameter<>(id, null, defaultValue, requested);
    }

    public static IParameterEntry<Color> colorParser(JsonObject jsonObject) {
        String id = HeadResourcesLoader.get(jsonObject, "id").getAsString();
        boolean requested = !jsonObject.has("requested") || jsonObject.get("requested").getAsBoolean();

        Color color = Color.WHITE;
        if (jsonObject.has("color_hex")) {
            String colorHex = jsonObject.get("color_hex").getAsString();
            color = parseColor(colorHex);
        }

        return new ModelParameter<>(id, color, requested);
    }

    public static Color parseColor(String colorHex) {
        if (colorHex.startsWith("#")) {
            colorHex = colorHex.substring(1);
        } else {
            throw new IllegalArgumentException(String.format("[HeadResourcesLoader] Missing '#' in color hex: %s", colorHex));
        }

        int color = Integer.parseUnsignedInt(colorHex, 16);

        return colorHex.length() == 8 ? Color.ofArgb(color) : Color.ofRgb(color);
    }

    public static IParameterEntry<OffsetParameter> offsetParser(JsonObject jsonObject) {
        String id = HeadResourcesLoader.get(jsonObject, "id").getAsString();
        boolean requested = !jsonObject.has("requested") || jsonObject.get("requested").getAsBoolean();
        byte value = jsonObject.has("value") ? jsonObject.get("value").getAsByte() : 0;
        byte minValue = jsonObject.has("min_value") ? jsonObject.get("min_value").getAsByte() : 0;
        byte maxValue = jsonObject.has("max_value") ? jsonObject.get("max_value").getAsByte() : 8;
        boolean isXAxis = jsonObject.has("axis") && jsonObject.get("axis").getAsString().equalsIgnoreCase("X");
        boolean enabled = jsonObject.has("enabled") && jsonObject.get("enabled").getAsBoolean();

        return new ModelParameter<>(id, new OffsetParameter(value, minValue, maxValue, isXAxis, enabled), requested);
    }

    private static void addChatMessageError(Exception e, String path) {
        if (MinecraftClient.getInstance().player != null) {
            Text message = Text.translatable("fzmm.gui.headGenerator.model.error.loadingModel", path)
                    .setStyle(Style.EMPTY
                            .withColor(FzmmClient.CHAT_BASE_COLOR)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(e.getMessage())))
                    );

            MinecraftClient.getInstance().player.sendMessage(message);
        }
    }
}
