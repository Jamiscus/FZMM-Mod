package fzmm.zailer.me.client.gui.item_editor.common.levelable.components.levelable;

import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelable;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.ILevelableBuilder;
import fzmm.zailer.me.client.gui.item_editor.common.levelable.LevelableEditor;
import fzmm.zailer.me.client.gui.item_editor.common.sort.ISortComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class SortLevelableComponent<V, D extends ILevelable<V>, B extends ILevelableBuilder<V, D>> extends BaseLevelableComponent<V, D, B>
        implements ISortComponent<V, D> {

    private LabelComponent levelLabel;

    public SortLevelableComponent(D levelable, LevelableEditor<V, D, B> editor, B builder) {
        super(levelable, editor, builder);
    }

    @Override
    protected List<? extends Component> getOptions() {
        String level = String.valueOf(this.getLevelable().getLevel());
        this.levelLabel = Components.label(Text.literal(level));
        return List.of(this.levelLabel);
    }

    @Override
    protected List<ButtonComponent> getButtons(LevelableEditor<V, D, B> editor, B builder) {
        return new ArrayList<>();
    }

    @Override
    public D value() {
        return this.getLevelable();
    }

    @Override
    public SortLevelableComponent<V, D, B> getValue() {
        return new SortLevelableComponent<>(this.getLevelable(), this.editor, this.builder);
    }

    @Override
    public void setValue(ISortComponent<V, D> value) {
        SortLevelableComponent<V, D, B> valueCast = (SortLevelableComponent<V, D, B>) value;

        this.setDisabled(valueCast.isDisabled);
        this.setButtonList(valueCast.buttonList);
        this.setLevelable(valueCast.getLevelable());
        this.levelLabel.text(valueCast.levelLabel.text());

        if (this.spriteLayout != null) {
            this.spriteLayout.clearChildren();
            this.getSpriteComponent().ifPresent(spriteComponent -> this.spriteLayout.child(spriteComponent));
        }
    }
}
