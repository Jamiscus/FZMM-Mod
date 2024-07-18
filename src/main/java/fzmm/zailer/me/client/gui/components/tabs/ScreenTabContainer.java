package fzmm.zailer.me.client.gui.components.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabContainer extends StyledFlowLayout {
    protected boolean selected;
    protected List<Component> componentList;
    private final FlowLayout labelLayout;

    public ScreenTabContainer(String baseTranslationKey, Sizing horizontalSizing, Sizing verticalSizing, String id) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.selected = false;
        this.componentList = new ArrayList<>();
        this.id(getScreenTabId(id));

        String translationKey = "fzmm.gui." + baseTranslationKey + ".tab." + id;

        this.labelLayout = (FlowLayout) StyledContainers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .child(
                        StyledComponents.label(Text.translatable(translationKey))
                                .tooltip(Text.translatable(translationKey + ".tooltip"))
                ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .margins(Insets.vertical(4));
    }

    public void setSelected(boolean selected, boolean addLabel) {
        if (this.selected && selected) {
            return;
        }
        this.selected = selected;

        if (this.selected) {
            if (addLabel) {
                this.child(this.labelLayout);
            }
            this.children(this.componentList);
            this.componentList.clear();
        } else {
            this.removeChild(this.labelLayout);
            this.componentList.addAll(this.children());
            this.clearChildren();
        }
    }

    public static String getScreenTabId(String id) {
        return id + "-screen-tab";
    }

    public static ScreenTabContainer parse(Element element) {
        String id = UIParsing.parseText(UIParsing.childElements(element).get("id")).getString();
        return new ScreenTabContainer(BaseFzmmScreen.getBaseTranslationKey(element), Sizing.content(), Sizing.content(), id);
    }
}
