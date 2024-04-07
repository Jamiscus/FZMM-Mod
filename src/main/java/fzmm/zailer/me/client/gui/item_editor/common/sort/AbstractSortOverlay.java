package fzmm.zailer.me.client.gui.item_editor.common.sort;

import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractSortOverlay<V, D, B extends ISortBuilder<D>> extends OverlayContainer<FlowLayout> {
    protected final List<D> valueList;
    protected final List<EnumWidget> sorterList;
    protected final ISortEditor editor;
    protected final B builder;
    protected final List<ISortComponent<V, D>> components;

    public AbstractSortOverlay(ISortEditor editor, B builder) {
        super(Containers.verticalFlow(Sizing.fill(70), Sizing.fill(90)));

        this.editor = editor;
        this.builder = builder;
        this.valueList = builder.values();
        this.components = new ArrayList<>();

        FlowLayout layout = Containers.verticalFlow(Sizing.content(0), Sizing.content(0));
        layout.children(this.getValues());

        FlowLayout sorters = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        sorters.gap(4);
        this.sorterList = this.getSorters(layout);
        sorters.children(this.sorterList);

        for (var sorter : this.sorterList) {
            //noinspection UnstableApiUsage
            sorter.select(0);
        }

        ScrollContainer<FlowLayout> scrollContainer = Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), layout);
        scrollContainer.margins(Insets.vertical(24));
        scrollContainer.positioning(Positioning.relative(0, 0));

        ButtonComponent backButton = Components.button(Text.translatable("fzmm.gui.button.back"), buttonComponent -> this.remove());
        backButton.margins(Insets.bottom(10).withRight(10));
        backButton.positioning(Positioning.relative(100, 100));

        this.child.surface(Surface.DARK_PANEL);
        this.child.gap(8);
        this.child.padding(Insets.of(10));
        this.child.child(sorters);
        this.child.child(scrollContainer);
        this.child.child(backButton);

        // otherwise owo-lib closes the overlay
        this.child.mouseDown().subscribe((mouseX, mouseY, button) -> true);

    }

    private List<? extends Component> getValues() {
        List<Component> result = new ArrayList<>();

        for (var value : this.valueList) {
            ISortComponent<V, D> valueComponent = this.getComponent(value, this.editor, this.builder);
            ButtonComponent upButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.arrow.up"),
                            button -> ListUtils.upEntry(this.components, valueComponent, this::updateComponentList))
                    .horizontalSizing(Sizing.fixed(20));

            ButtonComponent downButton = (ButtonComponent) Components.button(Text.translatable("fzmm.gui.button.arrow.down"),
                            button -> ListUtils.downEntry(this.components, valueComponent, this::updateComponentList))
                    .horizontalSizing(Sizing.fixed(20));

            this.components.add(valueComponent);

            FlowLayout valueLayout = valueComponent.getLayout(valueComponent, upButton, downButton);
            result.add(valueLayout);
        }

        return result;
    }

    protected abstract ISortComponent<V, D> getComponent(D value, ISortEditor editor, B builder);

    private void updateComponentList() {
        this.valueList.clear();

        for (var component : this.components)
            this.valueList.add(component.value());

        this.builder.values(this.valueList);
    }

    protected abstract List<EnumWidget> getSorters(FlowLayout layout);

    protected void sorterExecute(EnumWidget sorterComponent, Consumer<SortOption> sortConsumer) {
        SortOption sortOption = (SortOption) sorterComponent.getValue();
        this.disableSorters(sorterComponent, sortOption);
        sortConsumer.accept(sortOption);
        this.updateValuesLayout();
    }

    private void updateValuesLayout() {
        this.builder.values(this.valueList);

        for (int i = 0; i != this.components.size(); i++) {
            ISortComponent<V, D> entry = this.components.get(i);
            D sortedValue = this.builder.getValue(i);
            ISortComponent<V, D> valueCopy = this.getComponent(sortedValue, this.editor, this.builder);
            entry.setValue(valueCopy);
        }
    }

    private void disableSorters(EnumWidget button, SortOption sortOption) {
        if (sortOption == SortOption.DISABLE)
            return;

        for (var option : this.sorterList) {
            if (option != button && option.getValue() != SortOption.DISABLE)
                option.setValue(SortOption.DISABLE);
        }
    }

    protected EnumWidget getEnumWidget(String translationKey) {
        return new EnumWidget() {
            @Override
            public void setMessage(Text message) {
                message = Text.translatable(translationKey, message);
                super.setMessage(message);
            }
        };
    }

    @Override
    public void remove() {
        super.remove();

        this.editor.updateItemPreview();
        this.editor.updateAppliedValues();
    }

    public enum SortOption implements IMode {
        DISABLE("fzmm.gui.button.remove"),
        ASCENDING("fzmm.gui.button.arrow.up"),
        DESCENDING("fzmm.gui.button.arrow.down");

        private final String symbol;

        SortOption(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String getTranslationKey() {
            return this.symbol;
        }
    }
}
