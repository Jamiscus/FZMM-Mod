package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import io.wispforest.owo.ui.core.Color;

public class FillColorDesaturate implements IFillColorAlgorithm {
    @Override
    public int getColor(Color selectedColor, int pixelColor) {
        int alpha = (pixelColor >> 24) & 0xFF;
        int red = (pixelColor >> 16) & 0xFF;
        int green = (pixelColor >> 8) & 0xFF;
        int blue = pixelColor & 0xFF;

        int average = (red + green + blue) / 3;

        return (alpha << 24) | (average << 16) | (average << 8) | average;
    }

    @Override
    public boolean acceptTransparency() {
        return false;
    }
}
