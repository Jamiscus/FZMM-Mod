package fzmm.zailer.me.client.gui.components.style.container;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.util.math.MathHelper;
import org.w3c.dom.Element;

public class StyledScrollContainer<C extends Component> extends ScrollContainer<C> {
    public static final int SCROLLBAR_THICCNESS = 5;

    public StyledScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(direction, horizontalSizing, verticalSizing, child);

        this.scrollbar(this.styledScrollbar());
        this.scrollbarThiccness(SCROLLBAR_THICCNESS);

        // this is a workaround to solve that some components draw on top of the scroll,
        // causing that part not to be displayed correctly
        Insets oldPadding = this.padding.get();

        if (this.direction == ScrollDirection.VERTICAL && oldPadding.right() == 0) {
            this.padding(Insets.right(SCROLLBAR_THICCNESS));
        } else if (oldPadding.bottom() == 0) {
            this.padding(Insets.bottom(SCROLLBAR_THICCNESS));
        }
    }

    public ScrollContainer.Scrollbar styledScrollbar() {
        boolean isOverrideScrollbar = FzmmClient.CONFIG.guiStyle.persistentScrollbar();
        return isOverrideScrollbar ? vanillaFlat() : flat(Color.WHITE);
    }

    public static StyledScrollContainer<?> parse(Element element) {
        return element.getAttribute("direction").equals("vertical")
                ? StyledContainers.verticalScroll(Sizing.content(), Sizing.content(), null)
                : StyledContainers.horizontalScroll(Sizing.content(), Sizing.content(), null);
    }

    /**
     * Identical to ScrollbarContainer#flat but prevents the scrollbar
     * from being overlapped by another surface
     */
    public static Scrollbar flat(Color color) {
        int scrollbarColor = color.argb();

        return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            if (!active) return;

            if (direction == ScrollDirection.HORIZONTAL) {
                y += SCROLLBAR_THICCNESS;
            } else {
                x += SCROLLBAR_THICCNESS;
            }

            final var progress = Easing.SINE.apply(MathHelper.clamp(lastInteractTime - System.currentTimeMillis(), 0, 750) / 750f);
            int alpha = (int) (progress * (scrollbarColor >>> 24));

            context.fill(
                    x, y, x + width, y + height,
                    alpha << 24 | (scrollbarColor & 0xFFFFFF)
            );
        };
    }

    /**
     * Identical to ScrollbarContainer#vanillaFlat but hides if it can't scroll because the child is not exceeded
     * and prevents the scrollbar from being overlapped by another surface
     */
    public static Scrollbar vanillaFlat() {
        return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            if ((direction == ScrollDirection.HORIZONTAL && width < trackWidth) ||
                    (direction == ScrollDirection.VERTICAL && height < trackHeight)) {

                if (direction == ScrollDirection.HORIZONTAL) {
                    y += SCROLLBAR_THICCNESS;
                    trackY += SCROLLBAR_THICCNESS;
                } else {
                    x += SCROLLBAR_THICCNESS;
                    trackX += SCROLLBAR_THICCNESS;
                }

                context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, Color.BLACK.argb());
                NinePatchTexture.draw(FLAT_VANILLA_SCROLLBAR_TEXTURE, context, x, y, width, height);
            }
        };
    }

}
