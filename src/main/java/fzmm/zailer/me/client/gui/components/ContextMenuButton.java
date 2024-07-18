package fzmm.zailer.me.client.gui.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ContextMenuButton extends ButtonComponent {
    @Nullable
    private DropdownComponent contextMenu = null;
    protected int additionalZIndex = 200;
    private Consumer<DropdownComponent> contextMenuOptionsConsumer = dropdownComponent -> {};

    public ContextMenuButton(Text text) {
        super(text, button -> {
        });
        this.verticalSizing(Sizing.fixed(20));
    }

    @Override
    public void onPress() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (!(screen instanceof BaseFzmmScreen baseScreen)) {
            return;
        }

        //TODO: close contextMenu if it's already open
        if (this.contextMenu == null) {
            DropdownComponent.openContextMenu(baseScreen, baseScreen.getRoot(), FlowLayout::child,
                    this.x(), this.y() + this.height(), contextMenu -> {
                        this.contextMenu = contextMenu;
                        this.contextMenuOptionsConsumer.accept(contextMenu) ;

                        List<Component> dropdownChildren = contextMenu.children();
                        if (!dropdownChildren.isEmpty()) {
                            dropdownChildren.get(0).horizontalSizing(Sizing.fixed(this.width()));
                        }

                        contextMenu.zIndex(this.zIndex() + this.additionalZIndex);
                        // fixes that if you click on the margins zone it clicks on the component behind the dropdown
                        contextMenu.mouseDown().subscribe((mouseX1, mouseY1, button1) -> true);
                    });
        } else {
            this.removeContextMenu();
        }
    }

    public void setContextMenuOptions(Consumer<DropdownComponent> contextMenuOptionsConsumer) {
        this.contextMenuOptionsConsumer = contextMenuOptionsConsumer;
    }

    public void additionalZIndex(int additionalZIndex) {
        this.additionalZIndex = additionalZIndex;
    }

    public void removeContextMenu() {
        if (this.contextMenu != null) {
            this.contextMenu.remove();
            this.contextMenu = null;
        }
    }
}
