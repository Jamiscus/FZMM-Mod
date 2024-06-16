package fzmm.zailer.me.client.gui.head_generator.preview_algorithm;

import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.logic.head_generator.TextureOverlap;

import java.awt.image.BufferedImage;

public class DefaultPreviewUpdater implements IPreviewUpdater {
    @Override
    public BufferedImage getPreEdit(BufferedImage baseSkin, BufferedImage selectedPreEdit, boolean isSlim,
                                    boolean compoundEntriesEditingSkinBody, HeadGeneratorScreen parent) {
        for (var headEntry : parent.getHeadComponentEntries()) {
            headEntry.update(selectedPreEdit, isSlim);
            selectedPreEdit = new TextureOverlap(headEntry.getPreview())
                    .overlap(compoundEntriesEditingSkinBody)
                    .getHeadTexture();
        }

        SkinPreEditOption skinPreEditOption = parent.skinPreEdit();
        parent.setGridBaseSkinOriginalBody(parent.skinPreEdit(selectedPreEdit, skinPreEditOption, false));
        parent.setGridBaseSkinEditedBody(parent.skinPreEdit(selectedPreEdit, skinPreEditOption, true));

        return selectedPreEdit;
    }

    @Override
    public BufferedImage getHead(HeadComponentEntry entry, HeadGeneratorScreen parent,
                                 BufferedImage algorithmPreEdit, BufferedImage selectedPreEdit) {
        return parent.getGridBaseSkin(entry.getValue().isEditingSkinBody());
    }
}
