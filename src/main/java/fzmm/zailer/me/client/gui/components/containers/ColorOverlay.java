package fzmm.zailer.me.client.gui.components.containers;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.ColorPickerComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ColorOverlay extends OverlayContainer<FlowLayout> {
    private final List<FlowLayout> colorsLayouts;
    @Nullable
    private Color selectedColor;
    private static final int WIDTH = 190;
    private static final int HEIGHT = 170;
    private static final int COLOR_SIZE = 16;

    public ColorOverlay(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        super(Containers.verticalFlow(Sizing.content(), Sizing.content()));
        this.colorsLayouts = new ArrayList<>();
        this.selectedColor = null;

        this.addComponents(color, withAlpha, onConfirm, colorPreview);
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(4);
        this.zIndex(300);
    }

    protected void addComponents(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        FlowLayout colorPickerLayout = this.getColorPickerComponent(color, withAlpha, onConfirm, colorPreview);
        FlowLayout firstRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        firstRow.gap(4);

        ColorPickerComponent picker = colorPickerLayout.childById(ColorPickerComponent.class, "color-picker");
        if (picker == null) {
            FzmmClient.LOGGER.warn("[ColorOverlay] 'color-picker' component not found");
            return;
        }

        firstRow.child(this.getFavoriteColorsLayout(picker));
        firstRow.child(colorPickerLayout);
        firstRow.gap(4);

        this.child.child(firstRow);
        this.child.child(this.getDefaultColorsLayout(picker));

        picker.selectedColor(color);
    }

    public FlowLayout getFavoriteColorsLayout(ColorPickerComponent picker) {
        FzmmConfig.Colors config = FzmmClient.CONFIG.colors;

        FlowLayout layout = Containers.verticalFlow(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
        layout.gap(5);
        layout.padding(Insets.of(5));
        layout.surface(Surface.DARK_PANEL);
        layout.horizontalAlignment(HorizontalAlignment.CENTER);
        layout.mouseDown().subscribe((mouseX, mouseY, button) -> true);

        Component labelComponent = Components.label(Text.translatable("fzmm.gui.colorPicker.title.favorite"))
                .shadow(true)
                .margins(Insets.top(3));

        FlowLayout favoriteColorsComponent = Containers.ltrTextFlow(Sizing.fill(100), Sizing.content());
        favoriteColorsComponent.children(config.favoriteColors().stream()
                .map(color -> (FlowLayout) this.newColorBox(picker, color))
                .collect(Collectors.toList())
        );
        favoriteColorsComponent.horizontalAlignment(HorizontalAlignment.CENTER);

        ScrollContainer<FlowLayout> favoriteColorsScroll = Containers.verticalScroll(Sizing.content(), Sizing.fill(75), favoriteColorsComponent);
        favoriteColorsScroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE));

        ButtonComponent removeColorButton = Components.button(Text.translatable("fzmm.gui.button.remove"), button -> {
            if (this.selectedColor == null)
                return;

            List<Component> favoriteColorsComponentList = List.copyOf(favoriteColorsComponent.children());
            for (var favoriteColor : favoriteColorsComponentList) {
                if (favoriteColor instanceof FlowLayout colorLayout &&
                        colorLayout.children().get(0) instanceof BoxComponent boxComponent &&
                        boxComponent.startColor().get() == this.selectedColor) {

                    favoriteColorsComponent.removeChild(favoriteColor);
                    this.colorsLayouts.remove(favoriteColor);
                }
            }
            this.updateSelected(null, null);

            config.favoriteColors(this.getFavoriteList(favoriteColorsComponent));
            FzmmClient.CONFIG.save();
        });
        removeColorButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .zIndex(300)
                .id("remove-favorite-button");

        ButtonComponent addColorButton = Components.button(Text.translatable("fzmm.gui.button.add"),
                button -> {
                    Color selectedColor = picker.selectedColor();
                    FlowLayout colorLayout = (FlowLayout) this.newColorBox(picker, picker.selectedColor());
                    List<Component> favoriteColorsList = favoriteColorsComponent.children();

                    if (!favoriteColorsList.contains(colorLayout)) {
                        this.colorsLayouts.add(colorLayout);
                        favoriteColorsComponent.child(colorLayout);
                        this.updateSelected(selectedColor, colorLayout);

                        config.favoriteColors(this.getFavoriteList(favoriteColorsComponent));
                        FzmmClient.CONFIG.save();
                    }
                });

        addColorButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .zIndex(300)
                .id("add-favorite-button");

        FlowLayout buttonsLayout = this.getButtonsLayout(removeColorButton, addColorButton);

        layout.children(List.of(labelComponent, favoriteColorsScroll, buttonsLayout));

        return layout;
    }

    private List<Color> getFavoriteList(FlowLayout favoriteColorsComponent) {
        List<Color> colorList = new ArrayList<>();

        for (var component : favoriteColorsComponent.children()) {
            if (component instanceof FlowLayout layout && layout.children().get(0) instanceof BoxComponent boxComponent) {
                colorList.add(boxComponent.startColor().get());
            }
        }

        return colorList;
    }

    public Component newColorBox(ColorPickerComponent picker, Color color) {
        Component boxComponent = Components.box(Sizing.fixed(COLOR_SIZE), Sizing.fixed(COLOR_SIZE))
                .color(color)
                .fill(true)
                .margins(Insets.of(1))
                .cursorStyle(CursorStyle.HAND);

        FlowLayout colorLayout = Containers.horizontalFlow(Sizing.fixed(COLOR_SIZE + 2), Sizing.fixed(COLOR_SIZE + 2));
        colorLayout.padding(Insets.of(1));
        colorLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        colorLayout.child(boxComponent);

        boxComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            picker.selectedColor(color);

            this.updateSelected(color, colorLayout);

            return true;
        });

        this.colorsLayouts.add(colorLayout);

        return colorLayout;
    }

    private void updateSelected(@Nullable Color color, @Nullable FlowLayout colorLayout) {
        for (var layout : this.colorsLayouts) {
            layout.surface(Surface.outline(0x00000000));
        }

        this.selectedColor = color;

        if (color != null &&
                colorLayout != null &&
                colorLayout.children().get(0) instanceof BoxComponent boxComponent &&
                boxComponent.startColor().get().equals(color)) {

            colorLayout.surface(Surface.outline(0xFFFFFFFF));
        }
    }

    public FlowLayout getColorPickerComponent(Color color, boolean withAlpha, Consumer<ColorPickerComponent> onConfirm, BoxComponent colorPreview) {
        FlowLayout layout = Containers.verticalFlow(Sizing.fixed(WIDTH), Sizing.fixed(HEIGHT));
        layout.gap(5)
                .padding(Insets.of(5))
                .surface(Surface.DARK_PANEL)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .mouseDown().subscribe((mouseX, mouseY, button) -> true);

        Component labelComponent = Components.label(Text.translatable("fzmm.gui.colorPicker.title.picker"));

        ColorPickerComponent picker = (ColorPickerComponent) new ColorPickerComponent()
                .selectedColor(color)
                .showAlpha(withAlpha)
                .sizing(Sizing.fixed(160), Sizing.fixed(100))
                .id("color-picker");

        BoxComponent currentColor = (BoxComponent) Components.box(Sizing.fixed(80), Sizing.fixed(15))
                .fill(true)
                .color(color)
                .id("current-color");

        FlowLayout colorsLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                Components.box(Sizing.fixed(80), Sizing.fixed(15))
                        .fill(true)
                        .color(color)
        ).child(currentColor);

        assert this.child.parent() != null;
        ButtonComponent cancelButton = Components.button(Text.translatable("fzmm.gui.colorPicker.cancel"), buttonComponent -> this.child.parent().remove());
        cancelButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .id("cancel-button");

        ButtonComponent confirmButton = Components.button(Text.translatable("fzmm.gui.colorPicker.confirm"), buttonComponent -> {
            onConfirm.accept(picker);
            colorPreview.color(picker.selectedColor());
            this.child.parent().remove();
        });
        confirmButton.sizing(Sizing.fixed(50), Sizing.fixed(15))
                .id("confirm-button");

        FlowLayout buttonsLayout = this.getButtonsLayout(cancelButton, confirmButton);

        picker.onChanged().subscribe(selectedColor -> {
            this.selectedColor = selectedColor;
            currentColor.color(selectedColor);
        });

        List<Component> components = List.of(labelComponent, picker, colorsLayout, buttonsLayout);
        layout.children(components);

        return layout;
    }

    private FlowLayout getButtonsLayout(Component... components) {
        return (FlowLayout) Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                .children(Arrays.asList(components))
                .gap(10)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.relative(0, 98));
    }

    private FlowLayout getDefaultColorsLayout(ColorPickerComponent picker) {
        FlowLayout result = Containers.verticalFlow(Sizing.content(), Sizing.content());
        result.gap(4);

        int width = WIDTH * 2 + 4;
        result.child(this.getDyeColorsLayout(picker, width));
        result.child(this.getFormattingLayout(picker, width));

        for (var child : result.children()) {
            if (child instanceof FlowLayout flowLayout) {
                flowLayout.padding(Insets.of(5)).surface(Surface.DARK_PANEL).horizontalAlignment(HorizontalAlignment.CENTER);
            }
        }
        return result;
    }

    private FlowLayout getDyeColorsLayout(ColorPickerComponent picker, int width) {
        DyeColor[] defaultDyeColorArray = FzmmUtils.getDyeColorsInOrder();
        List<Pair<Color, Component>> dyeColorsComponents = new ArrayList<>();

        for (DyeColor dyeColor : defaultDyeColorArray) {
            Item dyeItem = Items.AIR;

            for (var item : Registries.ITEM.stream().toList()) {
                if (item instanceof DyeItem dyeItem1 && dyeItem1.getColor() == dyeColor) {
                    dyeItem = item;
                    break;
                }
            }

            dyeColorsComponents.add(new Pair<>(Color.ofDye(dyeColor), Components.item(dyeItem.getDefaultStack())));
        }

        List<Component> dyeComponents = this.getColorsComponentsWithIcon(picker, dyeColorsComponents);

        FlowLayout dyeLayout = Containers.ltrTextFlow(Sizing.fixed(width), Sizing.content());
        dyeLayout.children(dyeComponents);

        return dyeLayout;
    }

    private FlowLayout getFormattingLayout(ColorPickerComponent picker, int width) {
        Formatting[] defaultFormattingArray = FzmmUtils.getFormattingColorsInOrder();

        List<Component> formattingComponents = new ArrayList<>();

        for (Formatting formatting : defaultFormattingArray) {
            if (formatting.getColorValue() == null) {
                continue;
            }

            String colorCode = String.valueOf(formatting.getCode());
            FlowLayout colorLayout = (FlowLayout) this.newColorBox(picker, Color.ofFormatting(formatting));
            colorLayout.tooltip(Text.literal("&" + colorCode));
            formattingComponents.add(colorLayout);
        }

        FlowLayout formattingLayout = Containers.ltrTextFlow(Sizing.fixed(width), Sizing.content());
        formattingLayout.children(formattingComponents);

        return formattingLayout;
    }

    private List<Component> getColorsComponentsWithIcon(ColorPickerComponent picker, List<Pair<Color, Component>> colors) {
        List<Component> result = new ArrayList<>();

        for (var entry : colors) {
            StackLayout colorStack = Containers.stack(Sizing.content(), Sizing.content());
            FlowLayout colorLayout = (FlowLayout) this.newColorBox(picker, entry.getLeft());

            Component colorIconComponent = entry.getRight();

            colorStack.child(colorLayout);
            colorStack.child(colorIconComponent);

            colorIconComponent.mouseDown().subscribe(colorLayout::onMouseDown);
            colorIconComponent.cursorStyle(CursorStyle.HAND);

            colorStack.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            result.add(colorStack);
        }

        return result;
    }
}
