package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;

public class FillColorGrayscale implements IFillColorAlgorithm {
    @Override
    public int getColor(ColorParameter colorParameter, int red, int green, int blue, int alpha) {
        // The weighted average is applied to calculate the grayscale.
        int gray = (int) (0.2989 * red + 0.5870 * green + 0.1140 * blue);

        return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    }

    @Override
    public boolean acceptTransparentPixel() {
        return false;
    }
}
