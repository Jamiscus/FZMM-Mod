package fzmm.zailer.me.client.logic.head_generator.model;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.head_generator.category.HeadModelCategory;
import fzmm.zailer.me.client.gui.head_generator.category.HeadPaintableCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.*;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;
import fzmm.zailer.me.utils.SkinPart;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeadModelEntry extends AbstractHeadEntry implements INestedParameters {

    public static final String DESTINATION_ID = "destination_skin";
    public static final String BASE_SKIN_ID = "base_skin";
    private final List<IModelStep> steps = new ArrayList<>();
    @Nullable
    private ParameterList<BufferedImage> textures = null;
    @Nullable
    private ParameterList<ColorParameter> colors = null;
    @Nullable
    private ParameterList<OffsetParameter> offsets = null;
    private boolean isPaintable = false;
    private boolean isEditingSkinBody = false;
    private boolean isFirstResult = false;
    private boolean isInternal = false;
    private boolean isInvertedLeftAndRight = false;

    public HeadModelEntry() {
        super("");
    }

    public HeadModelEntry(String key, List<IModelStep> steps,
                          @Nullable ParameterList<BufferedImage> textures,
                          @Nullable ParameterList<ColorParameter> colors,
                          @Nullable ParameterList<OffsetParameter> offsets) {
        super(key);
        this.steps.addAll(steps);
        this.textures = textures;
        this.colors = colors;
        this.offsets = offsets;
    }

    public HeadModelEntry copy(String newPath) {
        HeadModelEntry result = new HeadModelEntry(newPath, this.steps, this.textures, this.colors, this.offsets);

        result.isPaintable(this.isPaintable);
        result.isEditingSkinBody(this.isEditingSkinBody);
        result.isFirstResult(this.isFirstResult);
        result.isInternal(this.isInternal);
        result.isInvertedLeftAndRight(this.isInvertedLeftAndRight);

        return result;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin) {
        BufferedImage result = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.loadDefaultTexture();

        Graphics2D destinationGraphics = result.createGraphics();

        ModelData data = new ModelData(destinationGraphics, DESTINATION_ID, this.textures,
                this.colors, this.offsets, baseSkin, ColorParameter.getDefault(), this.isInvertedLeftAndRight());

        this.apply(data, baseSkin, result);
        this.resetOffset(data.offsets());
        this.resetTexture(data.textures());

        destinationGraphics.dispose();

        return result;
    }

    public void apply(ModelData data, BufferedImage baseSkin, BufferedImage destinationSkin) {
        ParameterList<BufferedImage> textures = data.textures();
        textures.put(new ResettableModelParameter<>(BASE_SKIN_ID, baseSkin, null, false));
        textures.put(new ResettableModelParameter<>(DESTINATION_ID, destinationSkin, null, false));

        for (var step : this.steps)
            step.apply(data);
    }

    @Override
    public String getCategoryId() {
        return this.isPaintable ? HeadPaintableCategory.CATEGORY_ID : HeadModelCategory.CATEGORY_ID;
    }

    @Override
    public boolean isEditingSkinBody() {
        return this.isEditingSkinBody;
    }

    public void isEditingSkinBody(boolean value) {
        this.isEditingSkinBody = value;
    }

    @Override
    public boolean isFirstResult() {
        return this.isFirstResult;
    }

    public void isFirstResult(boolean value) {
        this.isFirstResult = value;
    }

    public void isPaintable(boolean value) {
        this.isPaintable = value;
    }

    public boolean isPaintable() {
        return this.isPaintable;
    }

    public boolean isInternal() {
        return this.isInternal;
    }

    public void isInternal(boolean value) {
        this.isInternal = value;
    }

    public void isInvertedLeftAndRight(boolean value) {
        this.isInvertedLeftAndRight = value;
    }

    public boolean isInvertedLeftAndRight() {
        return this.isInvertedLeftAndRight;
    }

    public List<IModelStep> getSteps() {
        return this.steps;
    }

    public void setSteps(List<IModelStep> steps) {
        this.steps.clear();
        this.steps.addAll(steps);
    }

    /**
     * It validates that the step is correct; it runs after loading all resources
     * from HeadResourceLoader so that, in case one is required, it can be checked
     * for existence. If it's false, a message will be displayed in the chat, and
     * it will be removed from the loaded resources.
     */
    public boolean validate() throws IllegalArgumentException {
        for (var step : this.getSteps()) {
            if (!step.validate()) {
                return false;
            }
        }
        return true;
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

    private void loadDefaultTexture() {
        for (var textureParameter : this.getNestedTextureParameters().parameterList()) {
            if (textureParameter.isRequested()) {
                continue;
            }

            BufferedImage texture = null;
            String defaultValue = textureParameter instanceof ResettableModelParameter<BufferedImage> resettableParam ?
                    resettableParam.getDefaultValue() : null;

            if (defaultValue != null) {
                Optional<BufferedImage> modelTexture = HeadResourcesLoader.getModelTexture(defaultValue);
                texture = modelTexture.orElseGet(() -> {
                    FzmmClient.LOGGER.warn("[HeadModelEntry] Could not find model texture '{}'", defaultValue);
                    return null;
                });
            }

            if (texture == null) {
                texture = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }

            textureParameter.setValue(texture);
        }
    }

    public void resetOffset(ParameterList<OffsetParameter> offsetParameters) {
        for (var offset : offsetParameters.parameterList()) {
            offset.value().ifPresent(OffsetParameter::reset);
        }
    }

    public void resetTexture(ParameterList<BufferedImage> textureParameters) {
        for (var parameter : textureParameters.parameterList()) {

            if (parameter instanceof ResettableModelParameter<BufferedImage> resettableParam &&
                    (!resettableParam.isRequested() && resettableParam.getDefaultValue() == null)
            ) {
                resettableParam.value().ifPresent(BufferedImage::flush);
            }
        }
    }
}
