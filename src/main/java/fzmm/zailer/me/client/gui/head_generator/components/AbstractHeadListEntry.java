package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.IParametersEntry;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.OffsetParameter;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.entity.custom_skin.CustomPlayerSkinEntity;
import fzmm.zailer.me.client.entity.custom_skin.ISkinMutable;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.Entity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractHeadListEntry extends FlowLayout implements IListEntry<AbstractHeadEntry> {
    public static final int HEAD_PREVIEW_SIZE = 24;
    public static final int BODY_PREVIEW_SIZE = 12;
    public static final int BODY_PREVIEW_OVERLAY_SIZE = 24;
    private static final int OVERLAY_WIDGETS_WIDTH = 75;
    public static final Text GIVE_BUTTON_TEXT = Text.translatable("fzmm.gui.button.giveHead");
    public static final Text GIVE_WAITING_UNDEFINED_TEXT = Text.translatable("fzmm.gui.headGenerator.wait");
    public static final String GIVE_WAITING_SECONDS_KEY = "fzmm.gui.headGenerator.wait_seconds";
    protected AbstractHeadEntry entry;
    private EntityComponent<Entity> previewComponent;
    private NativeImageBackedTexture previewTexture;
    protected final HeadGeneratorScreen parentScreen;
    protected OverlayContainer<FlowLayout> overlayContainer;
    private boolean isBodyPreview;

    public AbstractHeadListEntry(AbstractHeadEntry entry, Sizing horizontalSizing, Sizing verticalSizing, HeadGeneratorScreen parent) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);

        this.setBodyPreview(entry.isEditingSkinBody());
        this.setValue(entry);

        this.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        this.gap(BaseFzmmScreen.COMPONENT_DISTANCE);
        this.cursorStyle(CursorStyle.HAND);

        this.parentScreen = parent;

        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.addOverlay(parent);
            UISounds.playInteractionSound();
            return true;
        });

        this.mouseEnter().subscribe(() -> this.surface(Surface.flat(0x40000000)));
        this.mouseLeave().subscribe(() -> {
            if (!this.hovered)
                this.surface(Surface.flat(0));
        });
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
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

    public Text getDisplayName() {
        return this.entry.getDisplayName();
    }

    public String getFilterValue() {
        return this.entry.getFilterValue();
    }

    public String getCategoryId() {
        return this.entry.getCategoryId();
    }

    public void update(BufferedImage baseSkin, boolean isSlim) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();
        BufferedImage previewSkin = this.entry.getHeadSkin(baseSkin);

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

    protected EntityComponent<Entity> cloneCustomHeadEntity() {
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
        Map<String, String> parameters = Map.of("name", this.getDisplayName().getString());
        int giveButtonWidth = FzmmUtils.getMaxWidth(List.of(GIVE_BUTTON_TEXT,
                GIVE_WAITING_UNDEFINED_TEXT,
                Text.translatable(GIVE_WAITING_SECONDS_KEY, 1))
        ) + BaseFzmmScreen.BUTTON_TEXT_PADDING;

        FlowLayout headOverlay = parent.getModel().expandTemplate(FlowLayout.class, "head-overlay", parameters).configure(panel -> {
            panel.mouseDown().subscribe((mouseX1, mouseY1, button1) -> true);

            FlowLayout previewLayout = panel.childById(FlowLayout.class, "preview");
            BaseFzmmScreen.checkNull(previewLayout, "flow-layout", "preview");
            previewLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            EntityComponent<Entity> previewEntity = this.cloneCustomHeadEntity().allowMouseRotation(true);
            previewEntity.cursorStyle(CursorStyle.MOVE);
            if (this.isBodyPreview) {
                previewEntity.sizing(Sizing.fixed(BODY_PREVIEW_OVERLAY_SIZE));
                previewLayout.sizing(Sizing.content(8), Sizing.fixed((int) (BODY_PREVIEW_OVERLAY_SIZE * 2.5)));
            }
            previewLayout.child(previewEntity);

            LabelComponent categoryLabel = panel.childById(LabelComponent.class, "category-label");
            BaseFzmmScreen.checkNull(categoryLabel, "label", "category-label");
            categoryLabel.text(IHeadCategory.getCategory(this.entry, this.getCategoryId()).getText());

            ButtonComponent giveButton = panel.childById(ButtonComponent.class, "give-button");
            BaseFzmmScreen.checkNull(giveButton, "button", "give-button");
            giveButton.onPress((button) -> this.giveButtonExecute());
            giveButton.horizontalSizing(Sizing.fixed(giveButtonWidth));
            parent.setCurrentGiveButton(giveButton);

            ButtonComponent saveButton = panel.childById(ButtonComponent.class, "save-button");
            BaseFzmmScreen.checkNull(saveButton, "button", "save-button");
            saveButton.onPress(buttonComponent -> this.saveSkinExecute(this.getPreview()));

            this.addParameters(panel, parent);

            FlowLayout topRightButtonsLayout = panel.childById(FlowLayout.class, "top-right-buttons");
            BaseFzmmScreen.checkNull(topRightButtonsLayout, "flow-layout", "top-right-buttons");
            this.addTopRightButtons(panel, topRightButtonsLayout);
        });

        this.overlayContainer = Containers.overlay(headOverlay);
        this.overlayContainer.zIndex(500);
        ((FlowLayout) this.root()).child(this.overlayContainer);
    }

    private void addParameters(FlowLayout panel, BaseFzmmScreen parent) {
        if (!(this.entry instanceof IParametersEntry parametersEntry))
            return;

        FlowLayout parametersLayout = panel.childById(FlowLayout.class, "parameters");
        BaseFzmmScreen.checkNull(parametersLayout, "flow-layout", "parameters");
        if (parametersEntry.hasParameters()) {
            LabelComponent parametersLabel = Components.label(Text.translatable("fzmm.gui.headGenerator.label.parameters"));
            parametersLayout.child(parametersLabel);
        }

        this.addTextureParameters(parametersLayout, parametersEntry, parent);
        this.addColorParameters(parametersLayout, parametersEntry, parent);
        this.addOffsetsParameters(parametersLayout, parametersEntry, parent);
    }

    private void addTextureParameters(FlowLayout parametersLayout, IParametersEntry parametersEntry, BaseFzmmScreen parent) {
        for (var texture : parametersEntry.getTextures()) {
            if (!texture.isRequested())
                continue;
            String buttonId = texture.id() + "-texture";
            String enumButtonId = texture.id() + "-texture-mode";
            ImageRows imageRows = new ImageRows(parent.getBaseScreenTranslationKey(), buttonId, buttonId, enumButtonId, enumButtonId, false);
            parametersLayout.child(imageRows);

            ImageRowsElements elements = ImageRows.setup(parametersLayout, buttonId, enumButtonId, ImageMode.NAME);
            elements.imageButton().setButtonCallback(bufferedImage -> {
                parametersEntry.putTexture(texture.id(), bufferedImage);
                this.update();
            });

            elements.suggestionTextBox().horizontalSizing(Sizing.fixed(OVERLAY_WIDGETS_WIDTH));
        }
    }

    private void addColorParameters(FlowLayout parametersLayout, IParametersEntry parametersEntry, BaseFzmmScreen parent) {
        for (var colorParameter : parametersEntry.getColors()) {
            if (!colorParameter.isRequested())
                continue;
            String id = colorParameter.id() + "-color";
            ColorRow colorRow = new ColorRow(parent.getBaseScreenTranslationKey(), id, id, false, false);
            parametersLayout.child(colorRow);

            ColorRow.setup(parametersLayout, id, colorParameter.value().orElse(Color.WHITE), false, 300, s -> {
                parametersEntry.putColor(colorParameter.id(), colorRow.getValue());
                this.update();
            });

            colorRow.getWidget().horizontalSizing(Sizing.fixed(OVERLAY_WIDGETS_WIDTH));
        }
    }

    private void addOffsetsParameters(FlowLayout parametersLayout, IParametersEntry parametersEntry, BaseFzmmScreen parent) {
        for (var offset : parametersEntry.getOffsets()) {
            if (!offset.isRequested() && offset.value().isEmpty())
                continue;
            OffsetParameter offsetParameter = offset.value().get();
            String id = offset.id() + "-offset";
            SliderRow sliderRow = new SliderRow(parent.getBaseScreenTranslationKey(), id, id, false);
            parametersLayout.child(sliderRow);

            SliderRow.setup(parametersLayout, id, offsetParameter.value(), offsetParameter.minValue(), offsetParameter.maxValue(), Byte.class, 0, 1, d -> {
                offsetParameter.setValue(d.byteValue());
                this.update();
            });

            sliderRow.getWidget().horizontalSizing(Sizing.fixed(OVERLAY_WIDGETS_WIDTH));
        }
    }

    protected abstract void addTopRightButtons(FlowLayout panel, FlowLayout layout);

    private void update() {
        BufferedImage baseSkin = this.parentScreen.getGridBaseSkin(this.getValue().isEditingSkinBody());
        this.update(baseSkin, ImageUtils.isAlexModel(1, baseSkin));
    }

    public void saveSkinExecute(@Nullable BufferedImage skin) {
        ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
        if (skin == null) {
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.thereIsNoSkin")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
            return;
        }

        File skinFolder = HeadGeneratorScreen.SKIN_SAVE_FOLDER_PATH.toFile();
        if (skinFolder.mkdirs())
            FzmmClient.LOGGER.info("Skin save folder created");

        File file = ScreenshotRecorder.getScreenshotFilename(skinFolder);
        try {
            ImageIO.write(skin, "png", file);
            MutableText fileMessage = Text.literal(file.getName())
                    .setStyle(Style.EMPTY.withUnderline(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.saved", fileMessage)
                    .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR)));
        } catch (IOException e) {
            FzmmClient.LOGGER.error("Unexpected error saving the skin", e);
            chatHud.addMessage(Text.translatable("fzmm.gui.headGenerator.saveSkin.saveError")
                    .setStyle(Style.EMPTY.withColor(Formatting.RED)));
        }
    }

    private void giveButtonExecute() {
        this.parentScreen.giveHead(this.getPreview(), this.getDisplayName().getString());
    }

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