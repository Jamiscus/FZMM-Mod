package fzmm.zailer.me.compat.symbol_chat.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.replaceitem.symbolchat.gui.SymbolSelectionPanel;
import net.replaceitem.symbolchat.gui.container.ContainerWidgetImpl;
import net.replaceitem.symbolchat.gui.container.ScrollableContainer;
import net.replaceitem.symbolchat.gui.widget.SymbolSearchBar;
import net.replaceitem.symbolchat.gui.widget.SymbolTabWidget;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SymbolComponentAdapter extends VanillaWidgetComponent {
    protected final SymbolSelectionPanel widget;
    @Nullable
    protected ScrollableContainer scrollableContainer = null;

    public SymbolComponentAdapter(SymbolSelectionPanel widget) {
        super(widget);
        this.widget = widget;
        this.widget.visible = true;

        // fix click in the background
        this.mouseDown().subscribe((mouseX, mouseY, button) -> true);

        ClickableWidget clickableWidget = this.findScrollbar(this.widget);
        if (!(clickableWidget instanceof ScrollableContainer scrollContainer)) {
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            FzmmClient.LOGGER.error("[AbstractSymbolChatBaseAdapter] ScrollableContainer not found");
            return;
        }
        this.scrollableContainer = scrollContainer;
    }

    @Nullable
    private ClickableWidget findSearchBar() {
        try {
            Optional<SymbolTabWidget> tabWidgetOptional = this.widget.getCurrentTab();

            if (tabWidgetOptional.isPresent()) {
                for (var child : tabWidgetOptional.get().children()) {
                    if (child instanceof SymbolSearchBar symbolSearchBar) {
                        return symbolSearchBar;
                    }
                }
            }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            FzmmClient.LOGGER.error("[SymbolComponentAdapter] Failed to find search bar", e);
        }
        return null;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        ClickableWidget searchBar = this.findSearchBar();

        return (searchBar != null && searchBar.onKeyPress(keyCode, scanCode, modifiers)) || super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        ClickableWidget searchBar = this.findSearchBar();

        return (searchBar != null && searchBar.onCharTyped(chr, modifiers)) || super.onCharTyped(chr, modifiers);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        // fix scroll with smooth as it depends on the render
        // It is not being called because the custom implementation
        // of ContainerWidget in Symbol Chat is not compatible with owo-lib by default
        if (this.scrollableContainer == null) {
            return;
        }
        boolean visible = this.scrollableContainer.visible;
        this.scrollableContainer.visible = false;
        this.scrollableContainer.renderWidget(context, mouseX, mouseY, delta);
        this.scrollableContainer.visible = visible;
    }

    @Nullable
    private ClickableWidget findScrollbar(ClickableWidget widget) {
        if (widget instanceof ContainerWidgetImpl containerWidget) {
            for (var element : containerWidget.children()) {
                if (element instanceof ClickableWidget widgetChild)  {
                    return this.findScrollbar(widgetChild);
                }
            }
        } else if (widget instanceof ScrollableContainer) {
            return widget;
        }
        return null;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (this.scrollableContainer == null) {
            return super.onMouseScroll(mouseX, mouseY, amount);
        }
        return this.scrollableContainer.mouseScrolled(mouseX, mouseY, 0f, amount);
    }
}
