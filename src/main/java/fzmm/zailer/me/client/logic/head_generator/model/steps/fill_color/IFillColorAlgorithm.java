package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import io.wispforest.owo.ui.core.Color;

public interface IFillColorAlgorithm {
    IFillColorAlgorithm SOLID = new FillColorSolid();
    IFillColorAlgorithm MULTIPLY = new FillColorMultiply();

    /**
     * @return ARGB color
     */
    int getColor(Color selectedColor, int pixelColor);
}
