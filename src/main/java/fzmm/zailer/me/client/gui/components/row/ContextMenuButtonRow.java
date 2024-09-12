package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.w3c.dom.Element;


public class ContextMenuButtonRow extends AbstractRow {
    public ContextMenuButtonRow(String baseTranslationKey, String id, String tooltipId, boolean translate) {
        super(baseTranslationKey, id, tooltipId, false, translate);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        int width = NORMAL_WIDTH + BaseFzmmScreen.COMPONENT_DISTANCE + MinecraftClient.getInstance().textRenderer
                .getWidth(Text.translatable("fzmm.gui.button.reset").getString()) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        Component button = new ContextMenuButton(Text.empty())
                .horizontalSizing(Sizing.fixed(width))
                .id(getButtonId(id));

        return new Component[] {
                button
        };
    }

    public static String getButtonId(String id) {
        return id + "-context-menu-option";
    }

    public static ContextMenuButtonRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);
        String tooltipId = getTooltipId(element, id);

        return new ContextMenuButtonRow(baseTranslationKey, id, tooltipId, true);
    }
}
