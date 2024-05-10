package fzmm.zailer.me.client.gui.item_editor.color_editor;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.item_editor.IItemEditorScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.item_editor.color_editor.algorithm.*;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.item.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnstableApiUsage"})
public class ColorEditor implements IItemEditorScreen {
    private RequestedItem colorableRequested = null;
    private List<RequestedItem> requestedItems = null;
    private List<ButtonComponent> colorButtons;
    private ItemStack colorableStack;
    private IColorAlgorithm currentAlgorithm;
    private ConfigTextBox colorComponent;
    private CheckboxComponent checkboxComponent;

    @Override
    public List<RequestedItem> getRequestedItems() {
        if (this.requestedItems != null)
            return this.requestedItems;

        this.colorableRequested = new RequestedItem(
                itemStack -> AutoDetectColorAlgorithm.algorithm.isApplicable(itemStack),
                this::selectItemAndUpdateParameters,
                null,
                Text.translatable("fzmm.gui.itemEditor.color.title"),
                true
        );

        this.requestedItems = List.of(this.colorableRequested);
        return this.requestedItems;
    }

    @Override
    public ItemStack getExampleItem() {
        return Items.RED_DYE.getDefaultStack();
    }

    @Override
    public FlowLayout getLayout(ItemEditorBaseScreen baseScreen, FlowLayout editorLayout) {
        FlowLayout colorInputLayout = editorLayout.childById(FlowLayout.class, "color-input");
        BaseFzmmScreen.checkNull(colorInputLayout, "flow-layout", "color-input");

        ColorRow colorRow = new ColorRow("", "color", "color", false, false);

        colorInputLayout.child(
                colorRow.childById(BoxComponent.class, ColorRow.getColorPreviewId("color"))
                        .sizing(Sizing.fixed(48))
        );
        colorInputLayout.child(
                colorRow.childById(ConfigTextBox.class, ColorRow.getColorFieldId("color"))
                        .horizontalSizing(Sizing.fixed(50))
                        .margins(Insets.of(0))
        );

        this.checkboxComponent = editorLayout.childById(CheckboxComponent.class, "toggle");
        BaseFzmmScreen.checkNull(this.checkboxComponent, "checkbox", "toggle");
        this.checkboxComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
           this.updateItemPreview();

            return true;
        });

        this.colorComponent = ColorRow.setup(editorLayout, "color", Color.WHITE, false, 500, s -> {
            this.checkboxComponent.checked(true);

            this.updateItemPreview();
        });

        this.colorButtons = new ArrayList<>();
        this.colorableStack = Items.AIR.getDefaultStack();
        this.currentAlgorithm = AutoDetectColorAlgorithm.algorithm;

        this.colorButtons.add(this.setupAndGetButton(editorLayout, AutoDetectColorAlgorithm.algorithm));

        for (var algorithm : AutoDetectColorAlgorithm.colorAlgorithms) {
            this.colorButtons.add(this.setupAndGetButton(editorLayout, algorithm));
        }

        this.colorButtons.get(0).active = false;
        this.currentAlgorithm = AutoDetectColorAlgorithm.algorithm;

        return editorLayout;
    }

    private int getColor() {
        return ((Color) this.colorComponent.parsedValue()).rgb();
    }

    public ButtonComponent setupAndGetButton(FlowLayout rootComponent, IColorAlgorithm algorithm) {
        ButtonComponent button = rootComponent.childById(ButtonComponent.class, algorithm.getId());
        BaseFzmmScreen.checkNull(button, "button", algorithm.getId());

        button.renderer(ButtonComponent.Renderer.flat(0x40000000, 0x60000000, 0x80000000));
        button.onPress(buttonComponent -> {
            for (var colorAlgorithm : AutoDetectColorAlgorithm.colorAlgorithms)
                colorAlgorithm.removeTag(this.colorableStack);

            for (var colorButton : this.colorButtons)
                colorButton.active = true;

            buttonComponent.active = false;

            this.currentAlgorithm = algorithm;
            this.updateItemPreview();
        });

        return button;
    }

    @Override
    public String getId() {
        return "color";
    }

    @Override
    public void updateItemPreview() {
        if (this.checkboxComponent.isChecked())
            this.currentAlgorithm.setColor(this.colorableStack, this.getColor());
        else
            this.currentAlgorithm.removeTag(this.colorableStack);

        this.colorableRequested.setStack(this.colorableStack);
        this.colorableRequested.updatePreview();
    }

    @Override
    public void selectItemAndUpdateParameters(ItemStack stack) {
        this.colorableStack = stack.copy();

        boolean hasTag = AutoDetectColorAlgorithm.algorithm.hasTag(stack);

        if (hasTag) {
            int color = AutoDetectColorAlgorithm.algorithm.getColor(stack);
            this.colorComponent.setText(Color.ofRgb(color).asHexString(false));
        }

        this.checkboxComponent.checked(hasTag);
    }

}
