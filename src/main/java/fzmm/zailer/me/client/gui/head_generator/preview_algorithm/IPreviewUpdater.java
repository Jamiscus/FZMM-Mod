package fzmm.zailer.me.client.gui.head_generator.preview_algorithm;

import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentEntry;

import java.awt.image.BufferedImage;

public interface IPreviewUpdater {

    BufferedImage getPreEdit(BufferedImage baseSkin, BufferedImage selectedPreEdit, boolean isSlim,
                             boolean compoundEntriesEditingSkinBody, HeadGeneratorScreen parent);

    BufferedImage getHead(HeadComponentEntry entry, HeadGeneratorScreen parent,
                          BufferedImage algorithmPreEdit, BufferedImage selectedPreEdit);
}
