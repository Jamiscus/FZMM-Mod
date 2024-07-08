package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;
import io.wispforest.owo.ui.core.Color;

public class FillColorMultiply implements IFillColorAlgorithm {
    @Override
    public int getColor(ColorParameter colorParameter, int pixelColor) {
        Color color = colorParameter.color();
        float alpha = colorParameter.hasAlpha() ? color.alpha() : ((pixelColor >> 24) & 0xFF) / 255f;
        int origRed = (pixelColor >> 16) & 0xFF;
        int origGreen = (pixelColor >> 8) & 0xFF;
        int origBlue = pixelColor & 0xFF;

        int newRed = (int) (origRed * color.red());
        int newGreen = (int) (origGreen * color.green());
        int newBlue = (int) (origBlue * color.blue());

        return new Color(newRed / 255f, newGreen / 255f, newBlue / 255f, alpha).argb();
    }

    @Override
    public boolean acceptTransparentPixel() {
        return false;
    }
}
