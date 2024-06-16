package fzmm.zailer.me.client.gui.head_generator.preview_algorithm;

import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;

import java.awt.image.BufferedImage;

public class ForceNonePreEditPreviewUpdater implements IPreviewUpdater {
    @Override
    public BufferedImage getPreEdit(BufferedImage baseSkin, BufferedImage selectedPreEdit, boolean isSlim,
                                    boolean compoundEntriesEditingSkinBody, HeadGeneratorScreen parent) {
        return parent.skinPreEdit(baseSkin, SkinPreEditOption.NONE, false);
    }

    @Override
    public BufferedImage getHead(HeadComponentEntry entry, HeadGeneratorScreen parent,
                                 BufferedImage algorithmPreEdit, BufferedImage selectedPreEdit) {
        return entry.getValue() instanceof HeadModelEntry ? algorithmPreEdit : selectedPreEdit;
    }
}
