package fzmm.zailer.me.client.gui.head_generator.options;

import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.itemgroup.Icon;

import java.awt.*;

public enum SkinPreEditOption implements IMode {
    NONE("none", Icon.of(FzmmIcons.TEXTURE, 48, 0, 256, 256), (graphics, skin, skinPart) ->  {
        ImageUtils.drawUsedPixels(skin, false, graphics, false, skinPart);
        ImageUtils.drawUsedPixels(skin, true, graphics, true, skinPart);
    }),
    OVERLAP("overlap", Icon.of(FzmmIcons.TEXTURE, 48, 16, 256, 256), (graphics, skin, skinPart) -> {
        ImageUtils.drawUsedPixels(skin, false, graphics, false, skinPart);
        ImageUtils.drawUsedPixels(skin, true, graphics, false, skinPart);

        byte[][] usedAreas = skinPart.usedAreas();
        graphics.setBackground(new Color(0, 0, 0, 0));
        ImageUtils.clearRect(graphics, new byte[][]{usedAreas[2], usedAreas[3]});
    }),
    REMOVE("remove", Icon.of(FzmmIcons.TEXTURE, 48, 32, 256, 256), (graphics, skin, skinPart) -> {
        ImageUtils.drawUsedPixels(skin, false, graphics, false, skinPart);

        byte[][] usedAreas = skinPart.usedAreas();
        graphics.setBackground(new Color(0, 0, 0, 0));
        ImageUtils.clearRect(graphics, new byte[][]{usedAreas[2], usedAreas[3]});
    });

    private final String id;
    private final Icon icon;
    private final ISkinPreEdit preEdit;

    SkinPreEditOption(String id, Icon icon, ISkinPreEdit preEdit) {
        this.id = id;
        this.icon = icon;
        this.preEdit = preEdit;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.headGenerator.option.skinPreEdit." + this.id;
    }

    public ISkinPreEdit getPreEdit() {
        return this.preEdit;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public String getId() {
        return "skin-pre-edit-" + this.id;
    }
}