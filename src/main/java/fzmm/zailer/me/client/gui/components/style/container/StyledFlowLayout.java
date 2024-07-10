package fzmm.zailer.me.client.gui.components.style.container;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StyledFlowLayout extends FlowLayout {
    @Nullable
    private Surface hoveredSurface = null;

    public StyledFlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
    }

    public Surface styledPanel() {
//        boolean darkMode = FzmmClient.CONFIG.guiStyle.darkMode();
//        return darkMode ? Surface.DARK_PANEL : Surface.PANEL;
        return Surface.DARK_PANEL;
    }

    public Surface styledBackground() {
        boolean useNewVanillaBackground = FzmmClient.CONFIG.guiStyle.optionsBackground();
        return useNewVanillaBackground ? Surface.OPTIONS_BACKGROUND : Surface.VANILLA_TRANSLUCENT;
    }

    /**
     * this is necessary because it is not possible to compare the Surface
     * (because they are anonymous lambdas extending an interface)
     */
    private Optional<Surface> parseStyledSurface(Element surfaceElement) {
        List<Element> children = UIParsing.allChildrenOfType(surfaceElement, Node.ELEMENT_NODE);
        Surface result = Surface.BLANK;
        boolean modified = false;

        for (var child : children) {
            result = switch (child.getNodeName()) {
//                case "panel" -> {
//                    modified = true;
//                    yield result.and(styledPanel());
//                }
                case "options-background", "vanilla-translucent" -> {
                    modified = true;
                    yield result.and(styledBackground());
                }
                default -> result;
            };
        }

        return modified ? Optional.of(result) : Optional.empty();
    }

    public StyledFlowLayout hoveredSurface(@Nullable Surface hoveredSurface) {
        this.hoveredSurface = hoveredSurface;
        return this;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "surface", this::parseStyledSurface, surfaceOptional -> surfaceOptional.ifPresent(this::surface));
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.isInBoundingBox(mouseX, mouseY) && this.hoveredSurface != null) {
            this.hoveredSurface.draw(context, this);
        }

        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public static FlowLayout parse(Element element) {
        UIParsing.expectAttributes(element, "direction");

        return switch (element.getAttribute("direction")) {
            case "horizontal" -> StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
            case "ltr-text-flow" -> StyledContainers.ltrTextFlow(Sizing.content(), Sizing.content());
            default -> StyledContainers.verticalFlow(Sizing.content(), Sizing.content());
        };
    }

}
