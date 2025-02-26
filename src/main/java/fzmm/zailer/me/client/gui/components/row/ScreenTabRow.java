package fzmm.zailer.me.client.gui.components.row;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.components.style.container.StyledScrollContainer;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTabIdentifier;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class ScreenTabRow extends AbstractRow {
    private FlowLayout tabsLayout;
    private ScrollContainer<?> scrollContainer;

    public ScreenTabRow(String baseTranslationKey, String id) {
        super(baseTranslationKey);
        this.id(id);
        this.sizing(Sizing.content(), Sizing.fixed(26));
        this.surface(Surface.flat(0x80000000));
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.gap(4);
        this.hoveredSurface(null);
    }

    @Override
    public Component[] getComponents(String id, String tooltipId) {
        return new Component[0];
    }

    public static String getScreenTabButtonId(String id) {
        return id + "-screen-tab-button";
    }

    public static String getScreenTabButtonId(IScreenTabIdentifier tab) {
        return getScreenTabButtonId(tab.getId());
    }

    public static void setup(FlowLayout rootComponent, String id, Enum<? extends IScreenTabIdentifier> defaultTab) {
        ScreenTabRow screenTabRow = rootComponent.childById(ScreenTabRow.class, id);
        if (screenTabRow == null)
            return;

        screenTabRow.setup(defaultTab);
    }

    public void setup(Enum<? extends IScreenTabIdentifier> defaultTab) {
        List<Component> componentList = new ArrayList<>();
        for (var tab : defaultTab.getClass().getEnumConstants()) {
            IScreenTabIdentifier screenTab = (IScreenTabIdentifier) tab;
            boolean active = tab != defaultTab;
            String translationKey = BaseFzmmScreen.getTabTranslationKey(this.baseTranslationKey) + screenTab.getId();
            Text text = Text.translatable(translationKey);
            ButtonWidget button = Components.button(text, buttonComponent -> {});

            button.id(getScreenTabButtonId(screenTab.getId()));
            button.active = active;
            button.verticalSizing(Sizing.fixed(16));

            componentList.add(button);
        }

        this.tabsLayout = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .children(componentList)
                .gap(this.gap());
        this.scrollContainer = (ScrollContainer<?>) StyledContainers.horizontalScroll(Sizing.fill(100), Sizing.fill(100), this.tabsLayout)
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.vertical(StyledScrollContainer.SCROLLBAR_THICCNESS))
                .margins(Insets.horizontal(20));

        this.child(this.scrollContainer);

    }

    public static ScreenTabRow parse(Element element) {
        String baseTranslationKey = BaseFzmmScreen.getBaseTranslationKey(element);
        String id = getId(element);

        return new ScreenTabRow(baseTranslationKey, id);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (this.scrollContainer.width() > this.tabsLayout.width())
            return false;


        return super.onMouseScroll(mouseX, mouseY, amount);
    }
}
