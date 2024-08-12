package fzmm.zailer.me.client.logic.head_generator.texture;

import fzmm.zailer.me.client.gui.head_generator.category.HeadTextureCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.TextureOverlap;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SkinPart;

import java.awt.image.BufferedImage;

public class HeadTextureEntry extends AbstractHeadEntry {

    private final BufferedImage headSkin;
    private final boolean isEditingSkinBody;

    /**
     * @param headSkin the skin of the head, this is where the hat, glasses, beard, hair or whatever is,
     *                 should not be confused with the base skin (the one to which this skin is applied on top)
     */
    public HeadTextureEntry(BufferedImage headSkin, String path) {
        super(path);
        this.headSkin = headSkin;
        this.isEditingSkinBody = this.calculateIsEditingSkinBody();
    }

    @Override
    public BufferedImage getHeadSkin(BufferedImage baseSkin) {
        TextureOverlap textureOverlap = new TextureOverlap(baseSkin);

        // fixes skin formatting inconsistencies
        if (this.isEditingSkinBody() &&
                (ImageUtils.isSlimSimpleCheck(baseSkin) != ImageUtils.isSlimFullCheck(this.headSkin))) {
            HeadModelEntry formatUpdater = ImageUtils.isSlimSimpleCheck(baseSkin) ?
                    InternalModels.WIDE_TO_SLIM : InternalModels.SLIM_TO_WIDE;

            BufferedImage adaptedHeadSkin = formatUpdater.getHeadSkin(this.headSkin);
            textureOverlap.addTexture(adaptedHeadSkin);
            adaptedHeadSkin.flush();
        } else {
            textureOverlap.addTexture(this.headSkin);
        }

        return textureOverlap.getHeadTexture();
    }

    @Override
    public String getCategoryId() {
        return HeadTextureCategory.CATEGORY_ID;
    }

    @Override
    public boolean isEditingSkinBody() {
        return this.isEditingSkinBody;
    }

    @Override
    public boolean isFirstResult() {
        return false;
    }

    private boolean calculateIsEditingSkinBody() {
        if (this.headSkin.getWidth() != SkinPart.MAX_WIDTH || this.headSkin.getHeight() != SkinPart.MAX_HEIGHT)
            return false;


        for (SkinPart part : SkinPart.BODY_PARTS) {
            if (this.calculateIsEditingSkinBody(part.width(), part.height(), part.x(), part.y())
                    || this.calculateIsEditingSkinBody(part.width(), part.height(), part.hatX(), part.hatY()))
                return true;
        }

        return false;
    }

    private boolean calculateIsEditingSkinBody(int width, int height, int x, int y) {
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                if (ImageUtils.hasPixel(1, x + xOffset, y + yOffset, this.headSkin))
                    return true;
            }
        }
        return false;
    }
}
