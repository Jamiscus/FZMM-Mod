package fzmm.zailer.me.client.gui.item_editor.common.block_list.components;

import fzmm.zailer.me.client.gui.item_editor.common.block_list.AbstractBlockListBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.block_list.BlockListEditor;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockOrTagComponent extends FlowLayout {
    protected List<Block> values;
    protected String identifier;
    protected Component component;
    protected final BlockListEditor editor;
    protected final AbstractBlockListBuilder builder;
    protected final Consumer<BlockOrTagComponent> updateAvailableLayout;
    protected final Consumer<BlockOrTagComponent> updateAppliedLayout;
    protected boolean isAdd;


    public BlockOrTagComponent(List<Block> values, String identifier, BlockListEditor editor,
                               AbstractBlockListBuilder builder, Consumer<BlockOrTagComponent> updateAvailableLayout,
                               Consumer<BlockOrTagComponent> updateAppliedLayout, boolean isAdd) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.values = values;
        this.identifier = identifier.toLowerCase();
        this.editor = editor;
        this.builder = builder;
        this.updateAvailableLayout = updateAvailableLayout;
        this.updateAppliedLayout = updateAppliedLayout;
        this.isAdd = isAdd;
        this.updateBlockLayout();

        if (this.editor != null && this.builder != null && this.updateAvailableLayout != null && this.updateAppliedLayout != null) {
            this.component.mouseDown().subscribe((mouseX, mouseY, button) -> {
                UISounds.playButtonSound();
                return this.onExecute(true);
            });
            this.component.cursorStyle(CursorStyle.HAND);

            this.component.mouseEnter().subscribe(() -> this.surface(Surface.flat(this.isAdd ? 0xFF45BA5C : 0xFFE3281C)));
            this.component.mouseLeave().subscribe(() -> this.surface(Surface.flat(0)));

            this.margins(Insets.of(1));
        }
    }

    public BlockOrTagComponent(List<Block> values, String identifier) {
        this(values, identifier, null, null, null, null, false);
    }

    public boolean filter(String value) {
        return value.isEmpty() || this.identifier.contains(value.toLowerCase());
    }

    public List<Block> values() {
        return this.values;
    }

    public String identifier() {
        return this.identifier;
    }

    public void update(List<Block> values, String identifier) {
        this.values = values;
        this.identifier = identifier.toLowerCase();
        this.updateBlockLayout();
    }

    protected List<Component> getBlockLayout() {
        List<Component> result = new ArrayList<>();
        if (this.identifier.startsWith("#")) {
            this.component = Components.label(Text.translatable("fzmm.gui.itemEditor.block_list.label.tag", this.identifier, this.values.size()))
                    .margins(Insets.bottom(6))
                    .horizontalSizing(Sizing.expand(30));
            result.add(this.component);
        } else {
            Block firstBlock = this.values.get(0);
            ItemStack stack = new ItemStack(firstBlock);
            this.component = (stack.isEmpty() ?
                    Components.block(firstBlock.getDefaultState()).sizing(Sizing.fixed(16)) :
                    Components.item(stack)
            ).tooltip(firstBlock.getName());
            result.add(this.component);
        }

        return result;
    }

    protected void updateBlockLayout() {
        this.clearChildren();
        this.children(this.getBlockLayout());
    }

    private boolean onExecute(boolean needPreviewBeUpdated) {
        ParentComponent parent = this.parent();
        if (parent != null) {
            parent.removeChild(this);

            if (this.isAdd) {
                this.builder.add(this.identifier);
                this.updateAppliedLayout.accept(this);
            } else {
                this.builder.remove(this.identifier);
                this.updateAvailableLayout.accept(this);
            }

            this.isAdd = !this.isAdd;
        }

        if (needPreviewBeUpdated)
            this.editor.updateItemPreview();

        return true;
    }

    public void ignoreUpdatePreviewExecute() {
        this.onExecute(false);
    }

}
