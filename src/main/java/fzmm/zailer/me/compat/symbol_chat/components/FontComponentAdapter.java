package fzmm.zailer.me.compat.symbol_chat.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.replaceitem.symbolchat.gui.widget.DropDownWidget;
import net.replaceitem.symbolchat.resource.FontProcessor;

import java.util.function.Consumer;

public class FontComponentAdapter extends VanillaWidgetComponent {
    protected final DropDownWidget<FontProcessor> widget;
    protected final PositionedRectangle widgetScrollRect;

    public FontComponentAdapter(DropDownWidget<FontProcessor> widget) {
        super(widget);

        this.widget = widget;
        this.widget.visible = true;
        this.widget.expanded = true;

        this.widgetScrollRect = new PositionedRectangle() {
            @Override
            public int x() {
                return FontComponentAdapter.this.x();
            }

            @Override
            public int y() {
                return FontComponentAdapter.this.y();
            }

            @Override
            public int width() {
                return FontComponentAdapter.this.width();
            }

            @Override
            public int height() {
                // add widget.scrollableGridWidget height
                return FontComponentAdapter.this.height() + 200;
            }
        };

        // fix click in the background
        this.mouseDown().subscribe((mouseX, mouseY, button) -> true);
    }

    public void processFont(TextFieldWidget widget, String text, Consumer<String> writeConsumer) {
        try {
            FontProcessor selectedFont = this.widget.getSelection();

            text = selectedFont.convertString(text);
            writeConsumer.accept(text);

            if (selectedFont.isReverseDirection()) {
                int pos = widget.getCursor() - text.length();
                widget.setSelectionStart(pos);
                widget.setSelectionEnd(pos);
            }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            FzmmClient.LOGGER.error("[FontComponentAdapter] Failed to process font", e);
        }
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        return super.isInBoundingBox(x, y) || (this.widget.expanded && this.widgetScrollRect.isInBoundingBox(x, y));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        //FIXME: hovered in DropDownElementWidget
        //
        // The hovered state is false whenever scrolling occurs, since
        // DrawContext#scissorContains in the condition to detect the hovered
        // state includes the rectangle of the widget.scrollableGridWidget,
        // but the Y offset of the element widget is never changed.
        // Therefore, elements outside the initial area of the scroll
        // never have hovered set to true
        //
        // Workaround: do not render any hovered
        super.draw(context, 0, 0, partialTicks, delta);
    }
}
