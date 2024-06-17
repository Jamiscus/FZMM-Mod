package fzmm.zailer.me.client.logic.head_generator.model;

import fzmm.zailer.me.client.gui.head_generator.category.HeadModelCategory;
import fzmm.zailer.me.client.gui.head_generator.category.HeadPaintableCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.INestedParameters;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.OffsetParameter;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ParameterList;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ResettableModelParameter;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class HeadModelEntry extends AbstractHeadEntry implements INestedParameters {

    public static final String DESTINATION_ID = "destination_skin";
    public static final String BASE_SKIN_ID = "base_skin";
    private final List<IModelStep> steps = new ArrayList<>();
    @Nullable
    private ParameterList<BufferedImage> textures = null;
    @Nullable
    private ParameterList<Color> colors = null;
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
                          @Nullable ParameterList<Color> colors,
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
        BufferedImage result = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

        Graphics2D destinationGraphics = result.createGraphics();

        ModelData data = new ModelData(destinationGraphics, DESTINATION_ID, this.textures,
                this.colors, this.offsets, baseSkin, Color.WHITE, this.isInvertedLeftAndRight());

        this.apply(data, baseSkin, result);
        this.resetOffset(data.offsets());

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
    public boolean validate() throws IllegalArgumentException{
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
    public ParameterList<Color> getColorParameters() {
        return this.colors == null ? new ParameterList<>() : this.colors;
    }

    public void loadDefaultTexture() {
        for (var textureParameter : this.getNestedTextureParameters().parameterList()) {
            BufferedImage texture;
            String defaultValue = textureParameter instanceof ResettableModelParameter<BufferedImage> resettableParam ?
                    resettableParam.getDefaultValue() : null;

            if (defaultValue != null) {
                //TODO: HeadResourcesLoader
                Identifier textureIdentifier = Identifier.of(defaultValue);
                texture = ImageUtils.getBufferedImgFromIdentifier(textureIdentifier).orElseThrow(() -> new IllegalArgumentException(defaultValue));
            } else {
                texture = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }

            textureParameter.setValue(texture);
        }
    }

    public void resetOffset(ParameterList<OffsetParameter> offsetParameters) {
        for (var offset : offsetParameters.parameterList()) {
            offset.value().ifPresent(OffsetParameter::reset);
        }
    }
}
