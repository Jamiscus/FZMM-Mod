package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

public class BooleanButton extends ButtonComponent {

    protected boolean enabled = false;
    protected final Text enabledText;
    protected final Text disabledText;

    public BooleanButton(Text text, Color enabledColor) {
        super(Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.enabledText = text.copy().setStyle(Style.EMPTY.withColor(enabledColor.rgb()).withItalic(true));
        this.disabledText = text.copy().setStyle(Style.EMPTY);
        this.updateMessage();
    }

    public BooleanButton(Text enabledText, Text disabledText) {
        super(Text.empty(), button -> {});
        this.verticalSizing(Sizing.fixed(20));
        this.enabledText = enabledText;
        this.disabledText = disabledText;
        this.updateMessage();
    }

    @SuppressWarnings("NoTranslation")
    public BooleanButton() {
        this(Text.translatable("text.owo.config.boolean_toggle.enabled"), Text.translatable("text.owo.config.boolean_toggle.disabled"));
    }

    @Override
    public void onPress() {
        this.enabled = !this.enabled;
        this.updateMessage();
        super.onPress();
    }

    protected void updateMessage() {
        this.setMessage(this.enabled ? this.enabledText : this.disabledText);
    }

    public void enabled(boolean enabled) {
        this.enabledIgnoreCallback(enabled);
        this.onPress.onPress(this);
    }

    public void enabledIgnoreCallback(boolean enabled) {
        this.enabled = enabled;
        this.updateMessage();
    }

    public boolean enabled() {
        return this.enabled;
    }

    public void setContentHorizontalSizing() {
        int maxWidth = FzmmUtils.getMaxWidth(List.of(this.enabledText, this.disabledText)) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        this.horizontalSizing(Sizing.fixed(maxWidth));
    }

    public static BooleanButton parse(Element element) {
        Map<String, Element> children = UIParsing.childElements(element);

        if (children.containsKey("text")) {
            Text text = UIParsing.parseText(children.get("text"));
            Color enabledColor = Color.parse(children.get("enabled-color"));
            return new BooleanButton(text, enabledColor);
        }
        Text enabledText = UIParsing.parseText(children.get("enabled-text"));
        Text disabledText = UIParsing.parseText(children.get("disabled-text"));
        return new BooleanButton(enabledText, disabledText);
    }
    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "text", UIParsing::parseText, this::setMessage);
        UIParsing.apply(children, "text-shadow", UIParsing::parseBool, this::textShadow);
        UIParsing.apply(children, "renderer", Renderer::parse, this::renderer);
    }
}
