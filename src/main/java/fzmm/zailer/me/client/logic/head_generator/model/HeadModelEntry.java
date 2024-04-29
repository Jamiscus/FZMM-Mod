package fzmm.zailer.me.client.logic.head_generator.model;

import fzmm.zailer.me.client.gui.head_generator.category.HeadModelCategory;
import fzmm.zailer.me.client.gui.head_generator.category.HeadPaintableCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.*;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class HeadModelEntry extends AbstractHeadEntry implements IParametersEntry {

    public static final String DESTINATION_ID = "destination_skin";
    private final List<IModelStep> steps = new ArrayList<>();
    private final List<ResettableModelParameter<BufferedImage, String>> textures = new ArrayList<>();
    private final List<IModelParameter<Color>> colors = new ArrayList<>();
    private final List<IModelParameter<OffsetParameter>> offsets = new ArrayList<>();
    private boolean isPaintable = false;
    private boolean isEditingSkinBody = false;
    private boolean isFirstResult = false;
    private boolean isInternal = false;

    public HeadModelEntry() {
        super("");
    }

    public HeadModelEntry(String key, List<IModelStep> steps,
                          List<ResettableModelParameter<BufferedImage, String>> textures,
                          List<? extends IModelParameter<Color>> colors,
                          List<? extends IModelParameter<OffsetParameter>> offsets) {
        super(key);
        this.steps.addAll(steps);
        this.textures.addAll(textures);
        this.colors.addAll(colors);
        this.offsets.addAll(offsets);
    }

    public HeadModelEntry copy(String newPath) {
        HeadModelEntry result = new HeadModelEntry(newPath, this.steps, this.textures, this.colors, this.offsets);

        result.isPaintable(this.isPaintable);
        result.isEditingSkinBody(this.isEditingSkinBody);
        result.isFirstResult(this.isFirstResult);
        result.isInternal(this.isInternal);

        return result;
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin) {
        BufferedImage result = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

        Graphics2D destinationGraphics = result.createGraphics();
        Color selectedColor = Color.WHITE;
        List<ResettableModelParameter<BufferedImage, String>> texturesCopy = new ArrayList<>(this.textures);
        List<IModelParameter<Color>> colorsCopy = new ArrayList<>(this.colors);
        List<IModelParameter<OffsetParameter>> offsetsCopy = new ArrayList<>(this.offsets);

        texturesCopy.add(new ResettableModelParameter<>("base_skin", baseSkin, null, false));
        texturesCopy.add(new ResettableModelParameter<>(DESTINATION_ID, result, null, false));

        ModelData modelData = new ModelData(destinationGraphics, DESTINATION_ID, texturesCopy, colorsCopy, offsetsCopy, baseSkin, selectedColor);

        for (var step : this.steps)
            step.apply(modelData);

        for (var offset : this.offsets)
            offset.value().ifPresent(OffsetParameter::reset);

        destinationGraphics.dispose();

        return result;
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

    @Override
    public List<? extends IModelParameter<Color>> getColors() {
        return this.colors;
    }

    @Override
    public void putColor(String key, Color color) {
        for (var colorEntry : this.colors) {
            if (colorEntry.id().equals(key)) {
                colorEntry.setValue(color);
                return;
            }
        }
    }

    @Override
    public List<ResettableModelParameter<BufferedImage, String>> getTextures() {
        return this.textures;
    }

    public void putTexture(String key, BufferedImage texture) {
        for (var textureEntry : this.textures) {
            if (textureEntry.id().equals(key)) {
                textureEntry.setValue(texture);
                return;
            }
        }
    }

    @Override
    public List<? extends IModelParameter<OffsetParameter>> getOffsets() {
        return this.offsets;
    }

    public List<IModelStep> getSteps() {
        return this.steps;
    }

    public void setSteps(List<IModelStep> steps) {
        this.steps.clear();
        this.steps.addAll(steps);
    }

    @Override
    public boolean hasParameters() {
        return this.getColors().stream().anyMatch(IModelParameter::isRequested)
                || this.getTextures().stream().anyMatch(IModelParameter::isRequested)
                || this.getOffsets().stream().anyMatch(IModelParameter::isRequested);
    }

    public void reset() {
        for (var textureParameter : this.textures) {
            BufferedImage texture;
            String defaultValue = textureParameter.getDefaultValue();
            if (defaultValue != null) {
                Identifier textureIdentifier = new Identifier(defaultValue);
                texture = ImageUtils.getBufferedImgFromIdentifier(textureIdentifier).orElseThrow(() -> new NoSuchElementException(defaultValue));
            } else {
                texture = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }

            textureParameter.setValue(texture);
        }
    }
}
