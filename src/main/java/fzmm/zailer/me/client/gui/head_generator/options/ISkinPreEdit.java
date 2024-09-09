package fzmm.zailer.me.client.gui.head_generator.options;

import fzmm.zailer.me.utils.SkinPart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public interface ISkinPreEdit {

    default void apply(Graphics2D graphics, BufferedImage skin, List<SkinPart> skinParts) {
        for (SkinPart skinPart : skinParts) {
            this.apply(graphics, skin, skinPart);
        }
    }

    default void apply(Graphics2D graphics, BufferedImage skin) {
        this.apply(graphics, skin, true);
    }

    default void apply(Graphics2D graphics, BufferedImage skin, boolean editBody) {
        this.apply(graphics, skin, SkinPart.HEAD);
        if (editBody) {
            this.apply(graphics, skin, SkinPart.BODY_PARTS);
        }
    }

    void apply(Graphics2D graphics, BufferedImage skin, SkinPart skinPart);
}
