package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;

public interface IFillColorAlgorithm {
    IFillColorAlgorithm DESATURATE = new FillColorDesaturate();
    IFillColorAlgorithm GRAYSCALE = new FillColorGrayscale();
    IFillColorAlgorithm INVERSE = new FillColorInverse();
    IFillColorAlgorithm SOLID = new FillColorSolid();
    IFillColorAlgorithm MULTIPLY = new FillColorMultiply();

    /**
     * @return ARGB color
     */
    int getColor(ColorParameter colorParameter, int pixelColor);

    /**
     * @return true if the algorithm supports alpha 0
     */
    boolean acceptTransparentPixel();
}
