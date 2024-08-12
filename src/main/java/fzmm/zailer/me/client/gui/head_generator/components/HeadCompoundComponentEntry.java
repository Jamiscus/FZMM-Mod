package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;

public class HeadCompoundComponentEntry extends AbstractHeadComponentEntry {
    private static final Text REMOVE_LAYER_BUTTON_TEXT = Text.translatable("fzmm.gui.button.remove");
    private boolean modifiedPreview;
    private BufferedImage previousCompoundSkin;

    public HeadCompoundComponentEntry(AbstractHeadEntry entry, FlowLayout parentLayout, HeadGeneratorScreen parentScreen, BufferedImage initialPreview) {
        super(entry, Sizing.fixed(50), Sizing.fixed(45), parentScreen);
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.previousCompoundSkin = parentScreen.getGridBaseSkin(entry.isEditingSkinBody());

        FlowLayout moveButtons = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        moveButtons.positioning(Positioning.relative(50, 100));
        moveButtons.gap(15);

        ButtonComponent moveUpButton = Components.button(Text.translatable("fzmm.gui.button.arrow.up"),
                buttonComponent -> parentScreen.upCompoundEntry(this));
        moveUpButton.verticalSizing(Sizing.fixed(14));
        moveUpButton.renderer(FzmmStyles.DEFAULT_FLAT_BUTTON);
        
        ButtonComponent moveDownButton = Components.button(Text.translatable("fzmm.gui.button.arrow.down"),
                buttonComponent -> parentScreen.downCompoundEntry(this));
        moveDownButton.verticalSizing(Sizing.fixed(14));
        moveDownButton.renderer(FzmmStyles.DEFAULT_FLAT_BUTTON);

        moveButtons.child(moveUpButton);
        moveButtons.child(moveDownButton);

        this.child(moveButtons);

        for (var button : moveButtons.children()) {
            button.mouseEnter().subscribe(() -> this.mouseEnterEvents.sink().onMouseEnter());
            button.mouseLeave().subscribe(() -> this.mouseLeaveEvents.sink().onMouseLeave());
        }
        this.parent = parentLayout;
        this.updatePreview(initialPreview, ImageUtils.isSlimSimpleCheck(initialPreview));
    }

    @Override
    public BufferedImage getBaseSkin() {
        return this.previousCompoundSkin;
    }

    @Override
    public void update(BufferedImage previousCompoundSkin, boolean isSlim) {
        this.previousCompoundSkin = previousCompoundSkin;
        if (!this.modifiedPreview) {
            super.update(previousCompoundSkin, isSlim);
        }

        this.modifiedPreview = false;
    }

    @Override
    public void updatePreview(BufferedImage previewSkin, boolean isSlim) {
        super.updatePreview(previewSkin, isSlim);
        this.modifiedPreview = true;
    }

    @Override
    protected void addOverlay(HeadGeneratorScreen parent) {
        super.addOverlay(parent);
        this.modifiedPreview = true;
    }

    @Override
    protected void onCloseOverlay() {
        this.parentScreen.updatePreviews();
    }

    private void removeCompoundEntry(ButtonComponent button) {
        assert this.parent != null;

        this.close();
        if (this.parent.children().isEmpty()) {
            Animation<Sizing> layoutAnimation = this.parent.horizontalSizing().animation();
            if (layoutAnimation != null)
                layoutAnimation.backwards();
        }
        this.parentScreen.removeCompound(this);
        this.overlayContainer.remove();
    }

    @Override
    protected void addTopRightButtons(FlowLayout panel, FlowLayout layout) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int addLayerButtonWidth = textRenderer.getWidth(REMOVE_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        ButtonComponent removeButton = Components.button(REMOVE_LAYER_BUTTON_TEXT, this::removeCompoundEntry);
        removeButton.horizontalSizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)));

        layout.child(removeButton);

        LabelComponent categoryLabel = panel.childById(LabelComponent.class, "category-label");
        BaseFzmmScreen.checkNull(categoryLabel, "label", "category-label");
        categoryLabel.text(Text.translatable(IHeadCategory.COMPOUND_CATEGORY.getTranslationKey() + ".label", categoryLabel.text(), IHeadCategory.COMPOUND_CATEGORY.getText()));
    }

}