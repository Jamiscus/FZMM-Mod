package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;

public class FillColorDesaturate implements IFillColorAlgorithm {
    @Override
    public int getColor(ColorParameter colorParameter, int red, int green, int blue, int alpha) {
        int average = (red + green + blue) / 3;

        return (alpha << 24) | (average << 16) | (average << 8) | average;
    }

    @Override
    public boolean acceptTransparentPixel() {
        return false;
    }
}
