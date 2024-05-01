package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.entity.custom_skin.CustomPlayerSkinEntity;
import fzmm.zailer.me.client.entity.custom_skin.ISkinMutable;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UIErrorToast;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;

import java.awt.image.BufferedImage;

public abstract class AbstractHeadComponentEntry extends FlowLayout implements IListEntry<AbstractHeadEntry> {
    public static final int HEAD_PREVIEW_SIZE = 24;
    public static final int BODY_PREVIEW_SIZE = 12;
    protected AbstractHeadEntry entry;
    private EntityComponent<Entity> previewComponent;
    private NativeImageBackedTexture previewTexture;
    protected final HeadGeneratorScreen parentScreen;
    protected OverlayContainer<FlowLayout> overlayContainer;
    private boolean isBodyPreview;

    public AbstractHeadComponentEntry(AbstractHeadEntry entry, Sizing horizontalSizing, Sizing verticalSizing, HeadGeneratorScreen parent) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);

        this.setBodyPreview(entry.isEditingSkinBody());
        this.setValue(entry);

        this.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        this.gap(BaseFzmmScreen.COMPONENT_DISTANCE);
        this.cursorStyle(CursorStyle.HAND);

        this.parentScreen = parent;

        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            try {
                this.addOverlay(parent);
            } catch (Exception e) {
                //noinspection UnstableApiUsage
                UIErrorToast.report(e);
            }
            UISounds.playInteractionSound();
            return true;
        });

        this.mouseEnter().subscribe(() -> this.surface(Surface.flat(0x40000000)));
        this.mouseLeave().subscribe(() -> {
            if (!this.hovered)
                this.surface(Surface.flat(0));
        });
    }

    public boolean isBodyPreview() {
        return this.isBodyPreview;
    }

    private void setBodyPreview(boolean isBody) {
        this.isBodyPreview = isBody;
        Entity previewEntity;
        int size;
        int margins;
        if (isBody) {
            previewEntity = new CustomPlayerSkinEntity(MinecraftClient.getInstance().world);
            size = BODY_PREVIEW_SIZE;
            margins = 4;
        } else {
            previewEntity = new CustomHeadEntity(MinecraftClient.getInstance().world);
            size = HEAD_PREVIEW_SIZE;
            margins = 0;
        }

        this.removeChild(this.previewComponent);
        this.previewComponent = Components.entity(Sizing.fixed(size), previewEntity);
        this.previewComponent.cursorStyle(CursorStyle.HAND);
        this.previewComponent.margins(Insets.left(margins));
        this.child(this.previewComponent);
    }

    public String getFilterValue() {
        return this.entry.getFilterValue();
    }

    public String getCategoryId() {
        return this.entry.getCategoryId();
    }

    public abstract BufferedImage getBaseSkin();

    public void update() {
        this.update(this.getBaseSkin());
    }

    public void update(BufferedImage baseSkin) {
        this.update(baseSkin, ImageUtils.isAlexModel(1, baseSkin));
    }

    public void update(BufferedImage baseSkin, boolean isSlim) {
        BufferedImage previewSkin;

        this.close();
        try {
            previewSkin = this.entry.getHeadSkin(baseSkin);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("Failed to update preview skin of '{}'", this.entry.getKey(), e);
            return;
        }

        this.updatePreview(previewSkin, isSlim);
    }

    public void updatePreview(BufferedImage previewSkin, boolean isSlim) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        this.close();
        if (!(this.previewComponent.entity() instanceof ISkinMutable previewEntity)) {
            FzmmClient.LOGGER.error("[AbstractHeadListEntry] Failed to update preview entity");
            return;
        }

        NativeImage nativeImage = ImageUtils.toNativeImage(previewSkin);
        nativeImage.untrack();
        this.previewTexture = new NativeImageBackedTexture(nativeImage);
        previewEntity.setSkin(textureManager.registerDynamicTexture("fzmm_head", this.previewTexture), isSlim);

        textureManager.bindTexture(previewEntity.getTextures());
    }

    protected EntityComponent<Entity> copyCustomHeadEntity() {
        return Components.entity(this.previewComponent.horizontalSizing().get(), this.previewComponent.entity());
    }

    public void close() {
        if (this.previewTexture == null)
            return;

        this.previewTexture.close();
        this.previewTexture = null;
    }

    public BufferedImage getPreview() {
        NativeImage nativeImage = this.previewTexture.getImage();
        if (nativeImage == null) {
            FzmmClient.LOGGER.warn("[AbstractHeadListEntry] Failed to get preview image for {}", this.entry.getDisplayName().getString());
            return new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        }

        return ImageUtils.getBufferedImgFromNativeImg(nativeImage);
    }


    protected void addOverlay(HeadGeneratorScreen parent) {
        parent.collapseCategories();

        EntityComponent<Entity> previewEntity = this.copyCustomHeadEntity().allowMouseRotation(true);
        FlowLayout overlayLayout = new HeadComponentOverlay(parent, previewEntity, this);

        this.overlayContainer = new OverlayContainer<>(overlayLayout) {
            @Override
            public void remove() {
                super.remove();
                onCloseOverlay();
            }
        };
        this.overlayContainer.zIndex(300);

        ((FlowLayout) this.root()).child(this.overlayContainer);
    }

    protected void onCloseOverlay() {

    }

    protected abstract void addTopRightButtons(FlowLayout panel, FlowLayout layout);


    @Override
    public AbstractHeadEntry getValue() {
        return this.entry;
    }

    @Override
    public void setValue(AbstractHeadEntry value) {
        this.entry = value;
        this.previewComponent.tooltip(value.getDisplayName());
    }
}