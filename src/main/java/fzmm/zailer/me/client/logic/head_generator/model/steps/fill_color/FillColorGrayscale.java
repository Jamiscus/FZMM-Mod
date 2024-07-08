package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;

public class FillColorGrayscale implements IFillColorAlgorithm {
    @Override
    public int getColor(ColorParameter colorParameter, int pixelColor) {
        int alpha = (pixelColor >> 24) & 0xFF;
        int red = (pixelColor >> 16) & 0xFF;
        int green = (pixelColor >> 8) & 0xFF;
        int blue = pixelColor & 0xFF;

        // The weighted average is applied to calculate the grayscale.
        int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);

        return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    }

    @Override
    public boolean acceptTransparentPixel() {
        return false;
    }
}
