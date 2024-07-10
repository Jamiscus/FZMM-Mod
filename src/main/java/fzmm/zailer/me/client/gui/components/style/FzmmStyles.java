package fzmm.zailer.me.client.gui.components.style;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;

public class FzmmStyles {

    public static final Color ERROR_TEXT_COLOR = Color.ofRgb(0xD83F27);

    public static final Surface DEFAULT_HOVERED = (context, component) -> context.fill(component.x(), component.y(),
            component.x() + component.width(),
            component.y() + component.height(),
            component.zIndex(), 0x40000000
    );

    public static final ButtonComponent.Renderer DEFAULT_FLAT_BUTTON = ButtonComponent.Renderer
            .flat(0x00000000, 0x40000000, 0x00000000);
}
