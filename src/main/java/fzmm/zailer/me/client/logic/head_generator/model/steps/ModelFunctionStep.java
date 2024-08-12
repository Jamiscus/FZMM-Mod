package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.ModelPoint;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.*;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ModelFunctionStep implements IModelStep, INestedParameters {
    private static final String FUNCTION_OFFSET_ID = "function_offset_";
    private final String functionPath;
    @Nullable
    private final ParameterList<BufferedImage> textures;
    @Nullable
    private final ParameterList<ColorParameter> colors;
    @Nullable
    private final ParameterList<OffsetParameter> offsets;
    @Nullable
    private final ParameterList<OffsetParameter> functionOffsets;
    private final boolean isInvertedLeftAndRight;

    public ModelFunctionStep(String functionPath, @Nullable ParameterList<BufferedImage> textures,
                             @Nullable ParameterList<ColorParameter> colors, @Nullable ParameterList<OffsetParameter> offsets,
                             ModelPoint pos, boolean isInvertedLeftAndRight) {
        this.functionPath = functionPath;
        this.textures = textures;
        this.colors = colors;
        this.offsets = offsets;
        this.functionOffsets = this.createFunctionOffsets(pos).orElse(null);
        this.isInvertedLeftAndRight = isInvertedLeftAndRight;
    }

    @Override
    public void apply(ModelData data) {
        Optional<HeadModelEntry> functionOptional = getFunction(this.functionPath);
        if (functionOptional.isEmpty())
            return;
        HeadModelEntry function = functionOptional.get();

        BufferedImage destinationTexture = data.destinationTexture();
        BufferedImage baseSkin = data.getTexture(HeadModelEntry.BASE_SKIN_ID).orElseThrow(() -> {
            String message = "[ModelFunctionStep] Could not find base skin";
            FzmmClient.LOGGER.error(message);
            return new NullPointerException(message);
        });

        ParameterList<BufferedImage> textures = this.textures != null ? this.textures.copy() : null;
        ParameterList<OffsetParameter> offsets = this.offsets != null ? this.offsets.copy() : new ParameterList<>();
        ParameterList<ColorParameter> colors = this.colors != null ? this.colors.copy() : null;

        if (this.functionOffsets != null) {
            offsets.put(this.functionOffsets);
        }

        // It's necessary to add the offsets from the previous functions;
        // otherwise, they will be ignored. For example, if a function is
        // called and x and y are passed, but there are more functions inside
        // that function, they will ignore the x and y of the first function.
        for (var offset : data.offsets().parameterList()) {
            if (offset.id().startsWith(FUNCTION_OFFSET_ID)) {
                offsets.put(offset);
            }
        }

        ModelData functionData = new ModelData(data.destinationGraphics(), data.destinationId(), textures,
                colors, offsets, data.selectedTexture(), data.selectedColor(), this.isInvertedLeftAndRight);

        function.apply(functionData, baseSkin, destinationTexture);

        // if a function has an offset, it is possible that the previous model needs them,
        // but we only remove the offset of this function ("x" and "y" parameters of function)
        if (this.functionOffsets != null) {
            for (var offset : this.functionOffsets.parameterList()) {
                offsets.remove(offset.id());
            }
        }

        data.offsets().put(functionData.offsets());
    }

    /**
     * @throws IllegalArgumentException if the function is not found
     */
    public static Optional<HeadModelEntry> getFunction(String functionPath) throws IllegalArgumentException {
        AbstractHeadEntry functionEntry = HeadResourcesLoader.getByPath(functionPath).orElseThrow(() -> {
            String message = String.format("[ModelFunctionStep] Could not find function step '%s'", functionPath);
            FzmmClient.LOGGER.error(message);
            return new IllegalArgumentException(message);
        });

        if (!(functionEntry instanceof HeadModelEntry function)) {
            FzmmClient.LOGGER.warn("[ModelFunctionStep] '{}' is not a HeadModelEntry", functionPath);
            return Optional.empty();
        }

        return Optional.of(function);
    }

    private IParameterEntry<OffsetParameter> createOffset(int value, boolean isXAxis) {
        String key = FUNCTION_OFFSET_ID + UUID.randomUUID().toString().substring(0, 16);
        return new ModelParameter<>(key, new OffsetParameter((byte) value, (byte) 0, (byte) 64, isXAxis, true), false);
    }

    private Optional<ParameterList<OffsetParameter>> createFunctionOffsets(ModelPoint pos) {
        ParameterList<OffsetParameter> functionOffsets = new ParameterList<>();

        int x = pos.getXWithOffset();
        int y = pos.getYWithOffset();

        if (x != 0) {
            functionOffsets.put(this.createOffset(x, true));
        }

        if (y != 0) {
            functionOffsets.put(this.createOffset(y, false));
        }

        return functionOffsets.isEmpty() ? Optional.empty() : Optional.of(functionOffsets);
    }

    @Override
    public ParameterList<OffsetParameter> getOffsetParameters() {
        return this.offsets == null ? new ParameterList<>() : this.offsets;
    }

    @Override
    public ParameterList<BufferedImage> getTextureParameters() {
        return this.textures == null ? new ParameterList<>() : this.textures;
    }

    @Override
    public ParameterList<ColorParameter> getColorParameters() {
        return this.colors == null ? new ParameterList<>() : this.colors;
    }

    /**
     * @throws IllegalArgumentException if the function is not found
     */
    @Override
    public List<IModelStep> getSteps() {
        Optional<HeadModelEntry> functionOptional = getFunction(this.functionPath);
        return functionOptional.isPresent() ? functionOptional.get().getSteps() : new ArrayList<>();
    }

    @Override
    public boolean validate() throws IllegalArgumentException {
        return getFunction(this.functionPath).isPresent();
    }

    public static ModelFunctionStep parse(JsonObject jsonObject) {
        String functionPath = HeadResourcesLoader.get(jsonObject, "function_path").getAsString();

        Optional<ParameterList<BufferedImage>> textures = HeadResourcesLoader.getParameterList(jsonObject,
                "textures", HeadResourcesLoader::textureParser);

        Optional<ParameterList<ColorParameter>> colors = HeadResourcesLoader.getParameterList(jsonObject,
                "colors", HeadResourcesLoader::colorParser);

        Optional<ParameterList<OffsetParameter>> offsets = HeadResourcesLoader.getParameterList(jsonObject,
                "offsets", HeadResourcesLoader::offsetParser);

        ModelPoint point = jsonObject.has("pos") ?
                ModelPoint.parse(jsonObject.getAsJsonObject("pos")) : ModelPoint.ZERO;

        boolean isInvertedLeftAndRight = jsonObject.has("inverted_left_and_right") && jsonObject.get("inverted_left_and_right").getAsBoolean();

        return new ModelFunctionStep(functionPath, textures.orElse(null), colors.orElse(null),
                offsets.orElse(null), point, isInvertedLeftAndRight);
    }
}