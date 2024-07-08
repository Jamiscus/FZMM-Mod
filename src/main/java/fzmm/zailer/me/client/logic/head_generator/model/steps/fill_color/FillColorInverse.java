package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;

public class FillColorInverse implements IFillColorAlgorithm {
    @Override
    public int getColor(ColorParameter colorParameter, int pixelColor) {
        int alpha = (pixelColor >> 24);
        int red = (pixelColor >> 16) & 0xFF;
        int green = (pixelColor >> 8) & 0xFF;
        int blue = pixelColor & 0xFF;

        red = 255 - red;
        green = 255 - green;
        blue = 255 - blue;

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public boolean acceptTransparentPixel() {
        return false;
    }
}
