package fzmm.zailer.me.client.logic.head_generator.model;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.OffsetParameter;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ParameterList;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public final class ModelData {
    private Graphics2D destinationGraphics;
    private String destinationId;
    private final ParameterList<BufferedImage> textures;
    private final ParameterList<ColorParameter> colors;
    private final ParameterList<OffsetParameter> offsets;
    private final boolean isInvertedLeftAndRight;
    private BufferedImage selectedTexture;
    private ColorParameter selectedColor;

    public ModelData(Graphics2D destinationGraphics, String destinationId, @Nullable ParameterList<BufferedImage> textures,
                     @Nullable ParameterList<ColorParameter> colors, @Nullable ParameterList<OffsetParameter> offsets,
                     BufferedImage selectedTexture, ColorParameter selectedColor, boolean isInvertedLeftAndRight) {
        this.destinationGraphics = destinationGraphics;
        this.destinationId = destinationId;
        this.textures = textures == null ? new ParameterList<>() : textures;
        this.colors = colors == null ? new ParameterList<>() : colors;
        this.offsets = offsets == null ? new ParameterList<>() : offsets;
        this.selectedTexture = selectedTexture;
        this.selectedColor = selectedColor;
        this.isInvertedLeftAndRight = isInvertedLeftAndRight;
    }

    public ColorParameter getColor(String key) {
        return this.colors.get(key).orElse(ColorParameter.getDefault());
    }


    public Optional<BufferedImage> getTexture(String key) {
        return this.textures.get(key);
    }

    public Graphics2D destinationGraphics() {
        return this.destinationGraphics;
    }

    public String destinationId() {
        return this.destinationId;
    }

    public BufferedImage destinationTexture() {
        return this.getTexture(this.destinationId).orElseThrow(() -> {
            String message = String.format("[ModelData] Could not find destination texture '%s'", this.destinationId);
            FzmmClient.LOGGER.error(message);
            return new IllegalArgumentException(message);
        });
    }

    public ParameterList<BufferedImage> textures() {
        return this.textures;
    }

    public ParameterList<ColorParameter> colors() {
        return this.colors;
    }

    public ParameterList<OffsetParameter> offsets() {
        return this.offsets;
    }

    public BufferedImage selectedTexture() {
        return this.selectedTexture;
    }

    public ColorParameter selectedColor() {
        return this.selectedColor;
    }

    public void destinationGraphics(Graphics2D destinationGraphics) {
        this.destinationGraphics = destinationGraphics;
    }

    public void destinationId(String id) {
        this.destinationId = id;
    }

    public void selectedTexture(BufferedImage texture) {
        this.selectedTexture = texture;
    }

    public void selectedColor(ColorParameter color) {
        this.selectedColor = color;
    }

    public boolean isInvertedLeftAndRight() {
        return this.isInvertedLeftAndRight;
    }
}
