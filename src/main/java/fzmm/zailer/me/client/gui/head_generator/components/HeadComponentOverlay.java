package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.entity.custom_skin.ISkinMutable;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.ColorRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ColorParameter;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.INestedParameters;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.OffsetParameter;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ParameterList;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HeadComponentOverlay extends StyledFlowLayout {
    private static final int OVERLAY_WIDGETS_WIDTH = 75;
    public static final Text GIVE_BUTTON_TEXT = Text.translatable("fzmm.gui.button.giveHead");
    public static final Text GIVE_WAITING_UNDEFINED_TEXT = Text.translatable("fzmm.gui.headGenerator.wait");
    public static final String GIVE_WAITING_SECONDS_KEY = "fzmm.gui.headGenerator.wait_seconds";
    private final HeadGeneratorScreen parentScreen;
    private final EntityComponent<Entity> previewEntity;
    private boolean isSlimFormat;
    private ButtonComponent selectedSkinFormat;
    private SkinPreEditOption selectedSkinPreEdit;

    public HeadComponentOverlay(HeadGeneratorScreen parentScreen, EntityComponent<Entity> previewEntity,
                                AbstractHeadComponentEntry headComponentEntry) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.parentScreen = parentScreen;
        this.previewEntity = previewEntity;
        this.isSlimFormat = false;
        this.selectedSkinFormat = null;
        AbstractHeadEntry entry = headComponentEntry.getValue();

        Map<String, String> parameters = Map.of("name", entry.getDisplayName().getString());

        FlowLayout headOverlay = this.parentScreen.getModel().expandTemplate(FlowLayout.class, "head-overlay", parameters).configure(panel -> {
            panel.mouseDown().subscribe((mouseX1, mouseY1, button1) -> true);
            int giveButtonWidth = FzmmUtils.getMaxWidth(List.of(GIVE_BUTTON_TEXT,
                    GIVE_WAITING_UNDEFINED_TEXT,
                    Text.translatable(GIVE_WAITING_SECONDS_KEY, 1))
            ) + BaseFzmmScreen.BUTTON_TEXT_PADDING;

            FlowLayout previewLayout = panel.childById(FlowLayout.class, "preview");
            BaseFzmmScreen.checkNull(previewLayout, "flow-layout", "preview");
            previewLayout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            this.previewEntity.cursorStyle(CursorStyle.MOVE);
            int previewEntitySize = this.previewEntity.horizontalSizing().get().value * 2;
            this.previewEntity.sizing(Sizing.fixed(previewEntitySize));
            if (headComponentEntry.isBodyPreview()) {
                previewLayout.sizing(Sizing.content(8), Sizing.fixed((int) (previewEntitySize * 2.5)));
            }
            previewLayout.child(this.previewEntity);

            LabelComponent categoryLabel = panel.childById(LabelComponent.class, "category-label");
            BaseFzmmScreen.checkNull(categoryLabel, "label", "category-label");
            categoryLabel.text(IHeadCategory.getCategory(entry, headComponentEntry.getCategoryId()).getText());

            ButtonComponent giveButton = panel.childById(ButtonComponent.class, "give-button");
            BaseFzmmScreen.checkNull(giveButton, "button", "give-button");
            giveButton.onPress((button) -> this.giveButtonExecute(headComponentEntry));
            giveButton.horizontalSizing(Sizing.fixed(giveButtonWidth));
            parentScreen.setCurrentGiveButton(giveButton);

            ButtonComponent saveButton = panel.childById(ButtonComponent.class, "save-button");
            BaseFzmmScreen.checkNull(saveButton, "button", "save-button");
            saveButton.onPress(buttonComponent -> SnackBarManager.getInstance()
                    .add(this.saveSkinExecute(headComponentEntry.getPreview())));

            this.addDefaultOptions(panel, headComponentEntry);
            if (entry instanceof INestedParameters parametersEntry) {
                this.addParameters(panel, parentScreen, parametersEntry, headComponentEntry);
            }

            FlowLayout topRightButtonsLayout = panel.childById(FlowLayout.class, "top-right-buttons");
            BaseFzmmScreen.checkNull(topRightButtonsLayout, "flow-layout", "top-right-buttons");
            headComponentEntry.addTopRightButtons(panel, topRightButtonsLayout);
        });

        this.child(headOverlay);
    }

    private void giveButtonExecute(AbstractHeadComponentEntry headComponentEntry) {
        this.parentScreen.giveHead(headComponentEntry.getPreview(), headComponentEntry.getValue().getDisplayName().getString());
    }

    public ISnackBarComponent saveSkinExecute(@Nullable BufferedImage skin) {
        File skinFolder = HeadGeneratorScreen.SKIN_SAVE_FOLDER_PATH.toFile();
        if (skinFolder.mkdirs()) {
            FzmmClient.LOGGER.info("[HeadComponentOverlay] Skin save folder created");
        }

        return saveSkin(skin, ScreenshotRecorder.getScreenshotFilename(skinFolder));
    }

    public static ISnackBarComponent saveSkin(@Nullable BufferedImage skin, File file) {
        SnackBarBuilder builder = BaseSnackBarComponent.builder(SnackBarManager.HEAD_GENERATOR_SAVE_ID)
                .highTimer()
                .closeButton();
        if (skin == null) {
            return builder
                    .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .title(Text.translatable("fzmm.gui.headGenerator.snack_bar.saveSkin.thereIsNoSkin"))
                    .keepOnLimit()
                    .build();
        }

        try {
            ImageIO.write(skin, "png", file);
            FzmmClient.LOGGER.info("[HeadComponentOverlay] Saved skin to file: {}", file.toPath());
            builder.backgroundColor(FzmmStyles.ALERT_SUCCESS_COLOR)
                    .title(Text.translatable("fzmm.gui.headGenerator.snack_bar.saveSkin.saved"))
                    .button(iSnackBarComponent -> Components.button(Text.translatable("fzmm.gui.headGenerator.snack_bar.saveSkin.button.openFolder"), buttonComponent ->
                            Util.getOperatingSystem().open(HeadGeneratorScreen.SKIN_SAVE_FOLDER_PATH.toFile())))
                    .button(iSnackBarComponent -> Components.button(Text.translatable("fzmm.gui.headGenerator.snack_bar.saveSkin.button.openSkin"), buttonComponent ->
                            Util.getOperatingSystem().open(file)))
                    .sizing(Sizing.fixed(180), Sizing.content())
                    .mediumTimer();
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[HeadComponentOverlay] Unexpected error saving the skin", e);
            builder.backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                    .title(Text.translatable("fzmm.gui.headGenerator.snack_bar.saveSkin.saveError"))
                    .keepOnLimit();
        }
        return builder.startTimer().build();
    }

    private void addParameters(FlowLayout panel, BaseFzmmScreen parent, INestedParameters parametersEntry, AbstractHeadComponentEntry headComponentEntry) {
        FlowLayout parametersLayout = panel.childById(FlowLayout.class, "parameters");
        BaseFzmmScreen.checkNull(parametersLayout, "flow-layout", "parameters");
        if (parametersEntry.hasRequestedParameters()) {
            LabelComponent parametersLabel = StyledComponents.label(Text.translatable("fzmm.gui.headGenerator.label.parameters"));
            parametersLayout.child(parametersLabel);

            String baseTranslation = parent.getBaseScreenTranslationKey();
            this.addTextureParameters(parametersLayout, parametersEntry, baseTranslation, headComponentEntry);
            this.addColorParameters(parametersLayout, parametersEntry, baseTranslation, headComponentEntry);
            this.addOffsetsParameters(parametersLayout, parametersEntry, baseTranslation, headComponentEntry);
        }
    }

    private void addTextureParameters(FlowLayout parametersLayout, INestedParameters parametersEntry, String baseTranslation,
                                      AbstractHeadComponentEntry headComponentEntry) {
        ParameterList<BufferedImage> textureParameters = parametersEntry.getNestedTextureParameters();
        for (var texture : textureParameters.parameterList()) {
            if (!texture.isRequested())
                continue;
            String buttonId = texture.id() + "-texture";
            String enumButtonId = texture.id() + "-texture-mode";
            ImageRows imageRows = new ImageRows(baseTranslation, buttonId, buttonId, enumButtonId, enumButtonId, false);
            parametersLayout.child(imageRows);

            ImageRowsElements elements = ImageRows.setup(parametersLayout, buttonId, enumButtonId, ImageMode.NAME);
            elements.imageButton().setButtonCallback(bufferedImage -> {
                textureParameters.update(texture.id(), bufferedImage);
                this.updatePreview(headComponentEntry);
            });

            elements.valueField().suggestionsZIndex(350)
                    .horizontalSizing(Sizing.fixed(OVERLAY_WIDGETS_WIDTH));
        }
    }

    private void addColorParameters(FlowLayout parametersLayout, INestedParameters parametersEntry, String baseTranslation,
                                    AbstractHeadComponentEntry headComponentEntry) {
        ParameterList<ColorParameter> colorParameters = parametersEntry.getNestedColorParameters();
        for (var colorParameter : colorParameters.parameterList()) {
            if (!colorParameter.isRequested())
                continue;
            String id = colorParameter.id() + "-color";
            ColorRow colorRow = new ColorRow(baseTranslation, id, id, false, false);
            parametersLayout.child(colorRow);

            ColorParameter color = colorParameter.value().orElse(ColorParameter.getDefault());
            boolean hasAlpha = color.hasAlpha();

            ColorRow.setup(parametersLayout, id, color.color(), hasAlpha, 300, s -> {
                colorParameters.update(colorParameter.id(), new ColorParameter(colorRow.getValue(), hasAlpha));
                this.updatePreview(headComponentEntry);
            });

            colorRow.getWidget().horizontalSizing(Sizing.fixed(OVERLAY_WIDGETS_WIDTH));
        }
    }

    private void addOffsetsParameters(FlowLayout parametersLayout, INestedParameters parametersEntry, String baseTranslation,
                                      AbstractHeadComponentEntry headComponentEntry) {
        ParameterList<OffsetParameter> offsetParameters = parametersEntry.getNestedOffsetParameters();
        for (var offset : offsetParameters.parameterList()) {
            if (!offset.isRequested() && offset.value().isEmpty())
                continue;
            OffsetParameter offsetParameter = offset.value().get();
            String id = offset.id() + "-offset";
            SliderRow sliderRow = new SliderRow(baseTranslation, id, id, false);
            parametersLayout.child(sliderRow);

            SliderRow.setup(parametersLayout, id, offsetParameter.value(), offsetParameter.minValue(),
                    offsetParameter.maxValue(), Byte.class, 0, 1, d -> {
                        offsetParameter.setValue(d.byteValue());
                        this.updatePreview(headComponentEntry);
                    });

            sliderRow.getWidget().horizontalSizing(Sizing.fixed(OVERLAY_WIDGETS_WIDTH));
        }
    }

    private void addDefaultOptions(FlowLayout panel, AbstractHeadComponentEntry headComponentEntry) {
        FlowLayout defaultOptionsLayout = panel.childById(FlowLayout.class, "default-options");
        BaseFzmmScreen.checkNull(defaultOptionsLayout, "flow-layout", "default-options");

        defaultOptionsLayout.child(this.getRotateOptions(headComponentEntry));
        defaultOptionsLayout.child(this.getPreEditOptions(headComponentEntry));
        defaultOptionsLayout.child(this.getSkinFormatOptions(headComponentEntry));
    }

    private ButtonComponent getModelButton(AbstractHeadComponentEntry headComponentEntry, HeadModelEntry modelEntry,
                                           int amount, int iconV, @Nullable Consumer<ButtonComponent> callback) {
        Icon icon = Icon.of(FzmmIcons.TEXTURE, 64, iconV, 256, 256);
        ButtonComponent result = Components.button(Text.empty(), button -> {
            BufferedImage preview = headComponentEntry.getPreview();
            for (int i = 0; i < amount; i++) {
                BufferedImage updatedSkin = modelEntry.getHeadSkin(preview, this.parentScreen.hasUnusedPixels());
                preview.flush();
                preview = updatedSkin;
            }
            headComponentEntry.updatePreview(preview);

            if (callback != null) {
                callback.accept(button);
            }
        });

        result.sizing(Sizing.fixed(20), Sizing.fixed(20));
        result.renderer((context, button, delta) -> {
            ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
            icon.render(context, button.x() + 2, button.y() + 2, 0, 0, delta);
        });

        return result;
    }

    private FlowLayout getOptionLayout(String id) {
        FlowLayout result = StyledContainers.verticalFlow(Sizing.content(), Sizing.content());
        result.horizontalAlignment(HorizontalAlignment.CENTER);
        result.gap(4);
        LabelComponent label = StyledComponents.label(Text.translatable("fzmm.gui.headGenerator.option.overlayDefault." + id));

        result.child(label);

        return result;
    }

    private FlowLayout getRotateOptions(AbstractHeadComponentEntry headComponentEntry) {
        FlowLayout rotateLayout = this.getOptionLayout("rotate");
        FlowLayout rotateFirstRow = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        rotateFirstRow.gap(4);
        FlowLayout rotateSecondRow = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        rotateSecondRow.gap(4);

        int iconV = 0;
        rotateFirstRow.children(List.of(
                this.getModelButton(headComponentEntry, InternalModels.ROTATE_IN_X_AXIS, 1, iconV, null),
                this.getModelButton(headComponentEntry, InternalModels.ROTATE_IN_Y_AXIS, 1, iconV += 16, null),
                this.getModelButton(headComponentEntry, InternalModels.ROTATE_IN_Z_AXIS, 1, iconV += 16, null)
        ));

        rotateSecondRow.children(List.of(
                this.getModelButton(headComponentEntry, InternalModels.ROTATE_IN_X_AXIS, 3, iconV += 16, null),
                this.getModelButton(headComponentEntry, InternalModels.ROTATE_IN_Y_AXIS, 3, iconV += 16, null),
                this.getModelButton(headComponentEntry, InternalModels.ROTATE_IN_Z_AXIS, 3, iconV + 16, null)
        ));

        rotateLayout.children(List.of(rotateFirstRow, rotateSecondRow));

        return rotateLayout;
    }

    private FlowLayout getPreEditOptions(AbstractHeadComponentEntry headComponentEntry) {
        FlowLayout preEditLayout = this.getOptionLayout("preEdit");
        FlowLayout preEditRow = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        preEditRow.gap(4);

        HashMap<SkinPreEditOption, ButtonComponent> preEditHashMap = new HashMap<>();
        for (var preEdit : SkinPreEditOption.values()) {
            FlowLayout layout = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
            this.parentScreen.setupPreEditButton(layout, preEdit, preEditHashMap, skinPreEditOption -> {
                this.selectedSkinPreEdit = skinPreEditOption;
                BufferedImage baseSkin = this.updatePreview(headComponentEntry);

                // Updates the skin format to the selected one, as pre-edit skin uses
                // the base skin and not the preview because it needs the 2nd layer,
                // which can be removed depending on the selected pre-edit,
                // resulting in the loss of this option
                //
                // TODO: rotate is also lost, but I haven't implemented a way to save it and then replicate it
                if (this.selectedSkinFormat != null && ImageUtils.isSlimSimpleCheck(baseSkin) != this.isSlimFormat) {
                    this.selectedSkinFormat.onPress();
                }
                baseSkin.flush();
            });
            preEditRow.child(layout);
        }
        boolean forcePreEditInNone = FzmmClient.CONFIG.headGenerator.forcePreEditNoneInModels() &&
                headComponentEntry.getValue() instanceof HeadModelEntry;
        this.selectedSkinPreEdit = forcePreEditInNone ? SkinPreEditOption.NONE : this.parentScreen.skinPreEdit();
        preEditHashMap.get(this.selectedSkinPreEdit).active = false;

        preEditLayout.child(preEditRow);

        return preEditLayout;
    }

    private BufferedImage getBaseSkin(SkinPreEditOption skinPreEditOption, boolean isBodyPreview) {
        return this.parentScreen.skinPreEdit(skinPreEditOption, isBodyPreview);
    }

    private FlowLayout getSkinFormatOptions(AbstractHeadComponentEntry headComponentEntry) {
        FlowLayout skinFormatLayout = this.getOptionLayout("skinFormat");
        FlowLayout skinFormatRow = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        skinFormatRow.gap(4);

        List<ButtonComponent> buttons = new ArrayList<>();
        List<Component> optionsList = new ArrayList<>();

        ButtonComponent slim = this.getModelButton(headComponentEntry, InternalModels.WIDE_TO_SLIM, 1, 96,
                modelButton -> this.skinFormatCallback(buttons, modelButton, true));
        ButtonComponent wide = this.getModelButton(headComponentEntry, InternalModels.SLIM_TO_WIDE, 1, 112,
                modelButton -> this.skinFormatCallback(buttons, modelButton, false));

        optionsList.add(StyledContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(wide)
                .tooltip(Text.translatable("fzmm.gui.headGenerator.option.overlayDefault.skinFormat.wide"))
        );
        optionsList.add(StyledContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(slim)
                .tooltip(Text.translatable("fzmm.gui.headGenerator.option.overlayDefault.skinFormat.slim"))
        );

        buttons.add(wide);
        buttons.add(slim);

        if (ImageUtils.isSlimSimpleCheck(headComponentEntry.getPreview())) {
            slim.active = false;
        } else {
            wide.active = false;
        }

        skinFormatRow.children(optionsList);

        skinFormatLayout.child(skinFormatRow);

        return skinFormatLayout;
    }

    private void skinFormatCallback(List<ButtonComponent> buttons, ButtonComponent modelButton, boolean isSlim) {
        for (var button : buttons) {
            button.active = button != modelButton;
        }
        this.isSlimFormat = isSlim;
        this.selectedSkinFormat = modelButton;

        if (this.previewEntity.entity() instanceof ISkinMutable skinMutable) {
            skinMutable.model(isSlim);
        }
    }

    private BufferedImage updatePreview(AbstractHeadComponentEntry headComponentEntry) {
        BufferedImage baseSkin = this.getBaseSkin(this.selectedSkinPreEdit, headComponentEntry.isBodyPreview());
        headComponentEntry.basePreview(baseSkin, this.parentScreen.hasUnusedPixels());
        headComponentEntry.updateModel(ImageUtils.isSlimSimpleCheck(baseSkin));
        return baseSkin;
    }
}
