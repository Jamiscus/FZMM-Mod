package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.ModelArea;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;
import fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color.IFillColorAlgorithm;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Optional;

public class ModelFillColorStep implements IModelStep {

    private final ModelArea area;
    private final IFillColorAlgorithm algorithm;

    public ModelFillColorStep(ModelArea area, IFillColorAlgorithm algorithm) {
        this.area = area;
        this.algorithm = algorithm;
    }

    @Override
    public void apply(ModelData data) {
        Optional<BufferedImage> optionalTexture = data.getTexture(data.destinationId());
        if (optionalTexture.isEmpty())
            return;

        if (data.isInvertedLeftAndRight()) {
            this.area.swapLeftAndRight();
        }

        byte[][] area = this.area.copyWithOffset(data.offsets().parameterList()).optimize();
        BufferedImage texture = optionalTexture.get();
        ColorParameter selectedColor = data.selectedColor();

        if (data.isInvertedLeftAndRight()) {
            this.area.swapLeftAndRight();
        }

        for (var rect : area) {
            if (rect[2] > texture.getWidth() || rect[3] > texture.getHeight()) {
                FzmmClient.LOGGER.error("[ModelFillColorStep] Pixel outside of texture: {} {}",  rect[2],  rect[3]);
                continue;
            }

            this.apply(rect, texture, selectedColor);
        }
    }

    private void apply(byte[] rect, BufferedImage texture, ColorParameter selectedColor) {
        WritableRaster raster = texture.getRaster();
        ColorModel colorModel = texture.getColorModel();
        for (int y = rect[1]; y < rect[3]; y++) {
            for (int x = rect[0]; x < rect[2]; x++) {
                Object elements = raster.getDataElements(x, y, null);

                int alpha = colorModel.getAlpha(elements);
                if (!this.algorithm.acceptTransparentPixel() && alpha== 0) {
                    continue;
                }

                int colorArgb  = this.algorithm.getColor(selectedColor,
                        colorModel.getRed(elements),
                        colorModel.getGreen(elements),
                        colorModel.getBlue(elements),
                        colorModel.getAlpha(elements)
                );

                texture.setRGB(x, y, colorArgb);
            }
        }
    }

    public static ModelFillColorStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(HeadResourcesLoader.get(jsonObject, "area").getAsJsonObject());
        String algorithmString = HeadResourcesLoader.get(jsonObject, "algorithm").getAsString();
        IFillColorAlgorithm algorithm = switch (algorithmString) {
            case "desaturate" -> IFillColorAlgorithm.DESATURATE;
            case "grayscale" -> IFillColorAlgorithm.GRAYSCALE;
            case "inverse" -> IFillColorAlgorithm.INVERSE;
            case "solid" -> IFillColorAlgorithm.SOLID;
            case "multiply" -> IFillColorAlgorithm.MULTIPLY;
            default -> IFillColorAlgorithm.SOLID;
        };

        return new ModelFillColorStep(area, algorithm);
    }
}
