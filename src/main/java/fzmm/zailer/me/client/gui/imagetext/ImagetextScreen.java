package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.imagetext.algorithms.ImagetextAlgorithms;
import fzmm.zailer.me.client.gui.imagetext.tabs.IImagetextTab;
import fzmm.zailer.me.client.gui.imagetext.tabs.IImagetextTooltip;
import fzmm.zailer.me.client.gui.imagetext.tabs.ImagetextMode;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class ImagetextScreen extends BaseFzmmScreen implements IMementoScreen {

    private static final double DEFAULT_SIZE_VALUE = 32;
    public static final double MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS = 10d;
    private static ImagetextMode selectedMode = ImagetextMode.LORE;
    private static ImagetextAlgorithms selectedAlgorithm = ImagetextAlgorithms.CHARACTERS;
    private static ImagetextMemento memento = null;
    private final ImagetextLogic imagetextLogic;
    private final HashMap<String, IScreenTab> algorithmsTabs;
    private ImageRowsElements imageElements;
    private BooleanButton preserveImageAspectRatioToggle;
    private SmallCheckboxComponent showResolutionCheckbox;
    private SmallCheckboxComponent smoothImageCheckbox;
    private SliderWidget widthSlider;
    private SliderWidget heightSlider;
    private SliderWidget percentageOfSimilarityToCompress;
    private LabelComponent previewLabel;
    private Animation.Composed smallGuiAnimation;
    private CompletableFuture<Void> scheduledUpdatePreview = CompletableFuture.completedFuture(null);


    public ImagetextScreen(@Nullable Screen parent) {
        super("imagetext", "imagetext", parent);
        this.imagetextLogic = new ImagetextLogic();
        this.algorithmsTabs = new HashMap<>();
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        FzmmConfig.Imagetext config = FzmmClient.CONFIG.imagetext;

        // image options
        ImageRows imageRows = new ImageRows(this.getBaseScreenTranslationKey(), "image", "imageSourceType", true);
        this.imageElements = ImageRows.setup(imageRows, "image", "imageSourceType", ImageMode.URL);

        FlowLayout imageTextBoxLayout = rootComponent.childById(FlowLayout.class, "image-textbox");
        BaseFzmmScreen.checkNull(imageTextBoxLayout, "flow-layout", "image-textbox");
        imageTextBoxLayout.child(this.imageElements.valueField().sizing(Sizing.expand(100), Sizing.fixed(16)));

        FlowLayout imageButtonLayout = rootComponent.childById(FlowLayout.class, "image-buttons");
        BaseFzmmScreen.checkNull(imageButtonLayout, "flow-layout", "image-buttons");
        List<Component> imageButtonList = new ArrayList<>();

        for (var value : ImageMode.values()) {
            FlowLayout buttonLayout = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
            buttonLayout.tooltip(Text.translatable(value.getTranslationKey() + ".tooltip"));
            ButtonComponent button = this.imageElements.imageModeButtons().get(value);
            button.sizing(Sizing.fixed(16));

            buttonLayout.child(button);
            imageButtonList.add(buttonLayout);
        }
        imageButtonList.add(Components.spacer().verticalSizing(Sizing.fixed(1)));
        imageButtonList.add(this.imageElements.imageButton().verticalSizing(Sizing.fixed(16)).margins(Insets.none()));

        ImageButtonComponent imageButton = this.imageElements.imageButton();

        this.preserveImageAspectRatioToggle = rootComponent.childById(BooleanButton.class, "preserveImageAspectRatio");
        BaseFzmmScreen.checkNull(this.preserveImageAspectRatioToggle, "boolean-button", "preserveImageAspectRatio");
        this.preserveImageAspectRatioToggle.enabled(config.defaultPreserveImageAspectRatio());
        this.showResolutionCheckbox = rootComponent.childById(SmallCheckboxComponent.class, "showResolution");
        BaseFzmmScreen.checkNull(this.showResolutionCheckbox, "small-checkbox", "showResolution");
        this.showResolutionCheckbox.checked(false);
        this.smoothImageCheckbox = rootComponent.childById(SmallCheckboxComponent.class, "smoothImage");
        BaseFzmmScreen.checkNull(this.smoothImageCheckbox, "small-checkbox", "smoothImage");
        this.smoothImageCheckbox.checked(true);

        this.widthSlider = SliderRow.setup(rootComponent, "width", DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0, 3,
                aDouble -> this.onResolutionChanged(imageButton, this.preserveImageAspectRatioToggle, widthSlider, heightSlider, true)
        );
        this.heightSlider = SliderRow.setup(rootComponent, "height", DEFAULT_SIZE_VALUE, 2, config.maxResolution(), Integer.class, 0, 3,
                aDouble -> this.onResolutionChanged(imageButton, this.preserveImageAspectRatioToggle, heightSlider, widthSlider, false)
        );
        this.percentageOfSimilarityToCompress = SliderRow.setup(rootComponent, "percentageOfSimilarityToCompress",
                config.defaultPercentageOfSimilarityToCompress(), 0d, MAX_PERCENTAGE_OF_SIMILARITY_TO_COMPRESS, Double.class,
                1, 0.1d, null
        );
        this.percentageOfSimilarityToCompress.message(s -> Text.literal(s + "%"));

        imageButtonLayout.children(imageButtonList);

        // algorithm options
        ContextMenuButton algorithmButton = rootComponent.childById(ContextMenuButton.class, "algorithm-button");
        BaseFzmmScreen.checkNull(algorithmButton, "button", "algorithm-button");
        algorithmButton.setContextMenuOptions(contextMenu -> {
            for (var algorithm : ImagetextAlgorithms.values()) {
                contextMenu.button(algorithm.getText(this.getBaseScreenTranslationKey()), dropdown -> {
                            algorithmButton.removeContextMenu();
                            this.getTab(selectedAlgorithm, IImagetextAlgorithm.class, this.algorithmsTabs).clearCache();
                            selectedAlgorithm = algorithm;
                            algorithmButton.setMessage(this.getAlgorithmText());
                            this.selectTab(rootComponent, algorithm, this.algorithmsTabs);
                            this.scheduleUpdatePreview();
                        }
                );
            }
        });
        algorithmButton.setMessage(this.getAlgorithmText());
        this.setTabs(rootComponent, selectedAlgorithm, ImagetextAlgorithms.values(), this.algorithmsTabs);

        // image mode
        ContextMenuButton modeButton = rootComponent.childById(ContextMenuButton.class, "mode-button");
        BaseFzmmScreen.checkNull(modeButton, "button", "mode-button");
        modeButton.setContextMenuOptions(contextMenu -> {
            for (var mode : ImagetextMode.values()) {
                contextMenu.button(mode.getText(this.getBaseScreenTranslationKey()), dropdown -> {
                            modeButton.removeContextMenu();
                            selectedMode = mode;
                            modeButton.setMessage(this.getModeText());
                            this.selectTab(rootComponent, mode, this.tabs);
                            this.scheduleUpdatePreview();
                        }
                );
            }
        });

        modeButton.setMessage(this.getModeText());
        this.setTabs(rootComponent, selectedMode, ImagetextMode.values(), this.tabs);

        // preview
        this.previewLabel = rootComponent.childById(LabelComponent.class, "preview-label");
        BaseFzmmScreen.checkNull(this.previewLabel, "label", "preview-label");

        this.widthSlider.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.heightSlider.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.percentageOfSimilarityToCompress.onChanged().subscribe(value -> this.scheduleUpdatePreview());
        this.showResolutionCheckbox.onChanged().subscribe(buttonComponent -> this.scheduleUpdatePreview());
        this.smoothImageCheckbox.onChanged().subscribe(buttonComponent -> {
            this.getTab(selectedAlgorithm, IImagetextAlgorithm.class, this.algorithmsTabs).clearCache();
            this.scheduleUpdatePreview();
        });

        for (var imagetextTab : ImagetextAlgorithms.values()) {
            IImagetextAlgorithm tab = this.getTab(imagetextTab, IImagetextAlgorithm.class, this.algorithmsTabs);
            tab.setUpdatePreviewCallback(this::scheduleUpdatePreview);
        }

        //bottom buttons
        ButtonComponent executeButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId("execute"), false, button -> this.execute());
        imageButton.setButtonCallback(image -> {
            boolean hasImage = image != null;
            executeButton.active = hasImage;
            if (hasImage) {
                this.getTab(selectedAlgorithm, IImagetextAlgorithm.class, this.algorithmsTabs).clearCache();
                this.scheduleUpdatePreview();
                this.updateAspectRatio(image);
            }
        });

        // animation
        FlowLayout imageOptionsLayout = rootComponent.childById(FlowLayout.class, "image-options-layout");
        BaseFzmmScreen.checkNull(imageOptionsLayout, "layout", "image-options-layout");

        FlowLayout algorithmOptionsLayout = rootComponent.childById(FlowLayout.class, "algorithm-options-layout");
        BaseFzmmScreen.checkNull(algorithmOptionsLayout, "layout", "algorithm-options-layout");

        Animation<Sizing> imageLayoutAnimation = imageOptionsLayout.horizontalSizing().animate(100, Easing.LINEAR, Sizing.expand(100));
        Animation<Sizing> algorithmLayoutAnimationHorizontal = algorithmOptionsLayout.horizontalSizing().animate(100, Easing.LINEAR, Sizing.expand(100));
        Animation<Sizing> algorithmLayoutAnimationVertical = algorithmOptionsLayout.verticalSizing().animate(100, Easing.LINEAR, Sizing.content());
        this.smallGuiAnimation = Animation.compose(imageLayoutAnimation, algorithmLayoutAnimationHorizontal, algorithmLayoutAnimationVertical);

        this.setSmallGuiAnimation(this.width);
    }

    @SuppressWarnings("unchecked")
    private void setTabs(FlowLayout rootComponent, ITabsEnum selectedTab, ITabsEnum[] enumValues, HashMap<String, IScreenTab> tabsHashMap) {
        Enum<? extends ITabsEnum> selectedTabEnum = (Enum<? extends ITabsEnum>) selectedTab;
        this.setTabs(tabsHashMap, selectedTabEnum);
        for (var imagetextTab : enumValues) {
            IScreenTab tab = this.getTab(imagetextTab, IImagetextTab.class, tabsHashMap);
            tab.setupComponents(rootComponent);
        }
        this.selectTab(rootComponent, selectedTab, tabsHashMap);
    }

    @SuppressWarnings("unchecked")
    private void selectTab(FlowLayout rootComponent, ITabsEnum selectedTab, HashMap<String, IScreenTab> tabsHashMap) {
        Enum<? extends ITabsEnum> selectedTabEnum = (Enum<? extends ITabsEnum>) selectedTab;
        this.selectScreenTab(rootComponent, selectedTab, selectedTabEnum, tabsHashMap, false);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.setSmallGuiAnimation(width);
    }

    private boolean isSmallGuiDesign(int width) {
        // 60% is left options width and 30% is image options + algorithm options width
        return (width * 0.3f) < 200;
    }

    private void setSmallGuiAnimation(int width) {
        if (this.isSmallGuiDesign(width)) {
            this.smallGuiAnimation.forwards();
        } else {
            this.smallGuiAnimation.backwards();
        }
    }

    private Text getAlgorithmText() {
        return Text.translatable("fzmm.gui.imagetext.tab.algorithm", selectedAlgorithm.getText(this.getBaseScreenTranslationKey()));
    }

    private Text getModeText() {
        return Text.translatable("fzmm.gui.imagetext.tab.mode", selectedMode.getText(this.getBaseScreenTranslationKey()));
    }

    private void onResolutionChanged(ImageButtonComponent imageWidget, BooleanButton preserveImageAspectRatioButton,
                                     SliderWidget config, SliderWidget configToChange, boolean isWidth) {
        if (!imageWidget.hasImage() || !preserveImageAspectRatioButton.enabled()) {
            return;
        }

        Optional<BufferedImage> imageOptional = imageWidget.getImage();
        if (imageOptional.isEmpty()) {
            return;
        }
        BufferedImage image = imageOptional.get();

        int configValue = (int) config.parsedValue();
        Pair<Integer, Integer> rescaledSize = ImagetextLogic.changeResolutionKeepingAspectRatio(image.getWidth(), image.getHeight(), configValue, isWidth);

        int newValue = isWidth ? rescaledSize.getLeft() : rescaledSize.getRight();

        if (newValue > configToChange.max()) {
            newValue = (int) configToChange.max();
        } else if (newValue < configToChange.min()) {
            newValue = (int) configToChange.min();
        }

        configToChange.setDiscreteValueWithoutCallback(newValue);
    }

    public void execute() {
        CompletableFuture.runAsync(() -> {
            Optional<BufferedImage> image = this.imageElements.imageButton().getImage();
            if (image.isEmpty()) {
                return;
            }

            this.generateImagetext(image.get(), true);
            this.getTab(selectedMode, IImagetextTab.class).execute(this.imagetextLogic);
        });
    }

    public void scheduleUpdatePreview() {
        if (this.scheduledUpdatePreview != null && !this.scheduledUpdatePreview.isDone()) {
            this.scheduledUpdatePreview.cancel(true);
            this.scheduledUpdatePreview = null;
        }

        this.scheduledUpdatePreview = CompletableFuture.runAsync(() -> this.updatePreview(false),
                CompletableFuture.delayedExecutor(5, TimeUnit.MILLISECONDS)
        );
    }

    public void updatePreview(boolean isExecute) {
        Optional<BufferedImage> image = this.imageElements.imageButton().getImage();
        if (image.isEmpty()) {
            return;
        }

        this.generateImagetext(image.get(), isExecute);
        Text text = this.imagetextLogic.getText();

        ItemStack placeholderStack = DisplayBuilder.builder().addLore(text).get();
        String nbtSize = FzmmUtils.getLengthInKB(FzmmUtils.getLengthInBytes(placeholderStack));
        String textSize = FzmmUtils.getLengthInKB(Text.Serialization.toJsonString(text).length());

        MutableText tooltipText = Text.empty().setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        tooltipText.append(Text.translatable("fzmm.gui.imagetext.label.imagetextSize", nbtSize, textSize));

        if (this.getTab(selectedMode, IImagetextTab.class) instanceof IImagetextTooltip metadata) {
            tooltipText.append("\n");
            tooltipText.append(metadata.getTooltip(this.imagetextLogic));
        }

        assert this.client != null;
        this.client.execute(() -> {
            this.previewLabel.text(text);
            this.previewLabel.tooltip(tooltipText);
        });
    }

    private void generateImagetext(BufferedImage image, boolean isExecute) {
        int width = (int) this.widthSlider.parsedValue();
        int height = (int) this.heightSlider.parsedValue();
        boolean smoothScaling = this.smoothImageCheckbox.checked();
        boolean showResolution = this.showResolutionCheckbox.checked();
        double percentageOfSimilarityToCompress = (double) this.percentageOfSimilarityToCompress.parsedValue();

        IImagetextAlgorithm algorithm = (IImagetextAlgorithm) this.algorithmsTabs.get(selectedAlgorithm.getId());
        ImagetextData data = new ImagetextData(image, width, height, smoothScaling, percentageOfSimilarityToCompress);
        this.getTab(selectedMode, IImagetextTab.class).generate(algorithm, this.imagetextLogic, data, isExecute);

        if (showResolution) {
            this.imagetextLogic.addResolution();
        }

        Text text = this.imagetextLogic.getText();

        ItemStack placeholderStack = DisplayBuilder.builder().addLore(this.imagetextLogic.getText()).get();
        String nbtSize = FzmmUtils.getLengthInKB(FzmmUtils.getLengthInBytes(placeholderStack));
        String textSize = FzmmUtils.getLengthInKB(Text.Serialization.toJsonString(text).length());

        this.previewLabel.text(text);
        this.previewLabel.tooltip(Text.translatable("fzmm.gui.imagetext.label.imagetextSize", nbtSize, textSize));
    }

    private void updateAspectRatio(BufferedImage image) {
        if (!this.preserveImageAspectRatioToggle.enabled()) {
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        if (height > width) {
            this.onResolutionChanged(this.imageElements.imageButton(), this.preserveImageAspectRatioToggle, this.heightSlider, this.widthSlider, false);
        } else {
            this.onResolutionChanged(this.imageElements.imageButton(), this.preserveImageAspectRatioToggle, this.widthSlider, this.heightSlider, true);
        }
    }

    @Override
    public void setMemento(IMementoObject memento) {
        ImagetextScreen.memento = (ImagetextMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new ImagetextMemento(this.imageElements.valueField().getText(),
                this.imageElements.mode().get(),
                (int) this.widthSlider.parsedValue(),
                (int) this.heightSlider.parsedValue(),
                this.smoothImageCheckbox.checked(),
                this.showResolutionCheckbox.checked(),
                this.preserveImageAspectRatioToggle.enabled(),
                (double) this.percentageOfSimilarityToCompress.parsedValue(),
                this.createMementoTabs(this.tabs),
                this.createMementoTabs(this.algorithmsTabs)
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        ImagetextMemento memento = (ImagetextMemento) mementoObject;
        this.imageElements.valueField().text(memento.imageRowValue);
        this.imageElements.imageModeButtons().get(memento.imageMode).onPress();
        this.widthSlider.setFromDiscreteValue(memento.width);
        this.heightSlider.setFromDiscreteValue(memento.height);
        this.smoothImageCheckbox.checked(memento.smoothScaling);
        this.showResolutionCheckbox.checked(memento.showResolution);
        this.preserveImageAspectRatioToggle.enabled(memento.preserveImageAspectRatio);
        this.percentageOfSimilarityToCompress.setFromDiscreteValue(memento.percentageOfSimilarityToCompress);
        this.restoreMementoTabs(memento.mementoTabHashMap, this.tabs);
        this.restoreMementoTabs(memento.mementoAlgorithmTabHashMap, this.algorithmsTabs);
    }

    private record ImagetextMemento(String imageRowValue, ImageMode imageMode, int width, int height,
                                    boolean smoothScaling, boolean showResolution, boolean preserveImageAspectRatio,
                                    double percentageOfSimilarityToCompress,
                                    HashMap<String, IMementoObject> mementoTabHashMap,
                                    HashMap<String, IMementoObject> mementoAlgorithmTabHashMap) implements IMementoObject {
    }
}
