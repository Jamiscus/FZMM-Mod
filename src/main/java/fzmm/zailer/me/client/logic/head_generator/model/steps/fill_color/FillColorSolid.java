package fzmm.zailer.me.client.logic.head_generator.model.steps.fill_color;

import io.wispforest.owo.ui.core.Color;

public class FillColorSolid implements IFillColorAlgorithm {
    @Override
    public int getColor(Color selectedColor, int pixelColor) {
        return selectedColor.rgb();
    }

    @Override
    public boolean acceptTransparency() {
        return true;
    }
}
