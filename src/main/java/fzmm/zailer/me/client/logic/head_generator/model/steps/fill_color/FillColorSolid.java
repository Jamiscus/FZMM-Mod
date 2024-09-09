package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;

public class FillColorSolid implements IFillColorAlgorithm {
    @Override
    public int getColor(ColorParameter colorParameter, int red, int green, int blue, int alpha) {
        return colorParameter.color().argb();
    }

    @Override
    public boolean acceptTransparentPixel() {
        return true;
    }
}
