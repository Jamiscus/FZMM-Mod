package fzmm.zailer.me.client.logic.head_generator.model.parameters;

import io.wispforest.owo.ui.core.Color;

public record ColorParameter(Color color, boolean hasAlpha) {
    public static ColorParameter getDefault() {
        return new ColorParameter(Color.WHITE, false);
    }
}
