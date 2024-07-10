package fzmm.zailer.me.client.gui.head_generator;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.gui.head_generator.components.AbstractHeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentOverlay;
import fzmm.zailer.me.client.gui.head_generator.components.HeadCompoundComponentEntry;
import fzmm.zailer.me.client.gui.head_generator.options.SkinPreEditOption;
import fzmm.zailer.me.client.gui.head_generator.preview_algorithm.DefaultPreviewUpdater;
import fzmm.zailer.me.client.gui.head_generator.preview_algorithm.ForceNonePreEditPreviewUpdater;
import fzmm.zailer.me.client.gui.head_generator.preview_algorithm.IPreviewUpdater;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.utils.*;
import fzmm.zailer.me.utils.list.IListEntry;
import fzmm.zailer.me.utils.list.ListUtils;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HeadGeneratorScreen extends BaseFzmmScreen implements IMementoScreen {
    private static final int COMPOUND_HEAD_LAYOUT_WIDTH = 60;
    private static final int HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS = 1;
    public static final Path SKIN_SAVE_FOLDER_PATH = Path.of(FabricLoader.getInstance().getGameDir().toString(), FzmmClient.MOD_ID, "skins");
    private static final String SKIN_ID = "skin";
    private static final String SKIN_SOURCE_TYPE_ID = "skinSourceType";
    private static final String HEAD_NAME_ID = "headName";
    private static final String SEARCH_ID = "search";
    private static final String CONTENT_ID = "content";
    private static final String HEADS_LAYOUT_ID = "heads-layout";
    private static final String CONTENT_PARENT_LAYOUT_ID = "content-parent-layout";
    private static final String COMPOUND_HEADS_LAYOUT_ID = "compound-heads-layout";
    private static final String OPEN_SKIN_FOLDER_ID = "open-folder";
    private static final String TOGGLE_FAVORITE_LIST_ID = "toggle-favorite-list";
    private static final String HEAD_CATEGORY_ID = "head-category-collapsible";
    private static final String WIKI_BUTTON_ID = "wiki-button";
    private static HeadGeneratorMemento memento = null;
    private final Set<String> favoritesHeadsOnOpenScreen;
    private ImageRowsElements skinElements;
    private TextBoxComponent headNameField;
    private HashMap<SkinPreEditOption, ButtonComponent> skinPreEditButtons;
    private SkinPreEditOption selectedSkinPreEdit;
    private TextBoxComponent searchField;
    private List<HeadComponentEntry> headComponentEntries;
    private List<HeadCompoundComponentEntry> headCompoundComponentEntries;
    private FlowLayout contentLayout;
    private StyledFlowLayout compoundHeadsLayout;
    private ButtonWidget toggleFavoriteList;
    private boolean showFavorites;
    private BufferedImage baseSkin;
    private BufferedImage gridBaseSkinOriginalBody;
    private BufferedImage gridBaseSkinEditedBody;
    private String previousSkinName;
    private IHeadCategory selectedCategory;
    private ButtonComponent giveButton;
    private Animation.Composed compoundExpandAnimation;
    private CollapsibleContainer headCategoryCollapsible;


    public HeadGeneratorScreen(@Nullable Screen parent) {
        super("head_generator", "headGenerator", parent);
        this.favoritesHeadsOnOpenScreen = Set.copyOf(FzmmClient.CONFIG.headGenerator.favoriteSkins());
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.headComponentEntries = new ArrayList<>();
        this.headCompoundComponentEntries = new ArrayList<>();
        this.baseSkin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        this.gridBaseSkinOriginalBody = this.baseSkin;
        this.gridBaseSkinEditedBody = this.baseSkin;
        //general
        this.skinElements = ImageRows.setup(rootComponent, SKIN_ID, SKIN_SOURCE_TYPE_ID, ImageMode.NAME);
        this.skinElements.imageButton().setButtonCallback(this::imageCallback);
        this.previousSkinName = "";
        this.headNameField = TextBoxRow.setup(rootComponent, HEAD_NAME_ID, "", 512);
        this.skinElements.valueField().onChanged().subscribe(this::onChangeSkinField);
        this.contentLayout = rootComponent.childById(FlowLayout.class, CONTENT_ID);
        checkNull(this.contentLayout, "flow-layout", CONTENT_ID);
        this.compoundHeadsLayout = rootComponent.childById(StyledFlowLayout.class, COMPOUND_HEADS_LAYOUT_ID);
        checkNull(this.compoundHeadsLayout, "flow-layout", COMPOUND_HEADS_LAYOUT_ID);

        FlowLayout contentParentLayout = rootComponent.childById(FlowLayout.class, CONTENT_PARENT_LAYOUT_ID);
        checkNull(contentParentLayout, "flow-layout", CONTENT_PARENT_LAYOUT_ID);
        FlowLayout headsLayout = rootComponent.childById(FlowLayout.class, HEADS_LAYOUT_ID);
        checkNull(headsLayout, "flow-layout", HEADS_LAYOUT_ID);

        int animationDuration = 800;
        Animation<Insets> headsLayoutMarginAnimation = this.compoundHeadsLayout.margins()
                .animate(animationDuration, Easing.CUBIC, Insets.of(0, 0, 0, 6));
        Animation<Sizing> compoundHeadsLayoutAnimation = this.compoundHeadsLayout.horizontalSizing()
                .animate(animationDuration, Easing.CUBIC, Sizing.fixed(COMPOUND_HEAD_LAYOUT_WIDTH));
        Animation<Insets> compoundHeadsLayoutPaddingAnimation = this.compoundHeadsLayout.padding()
                .animate(animationDuration, Easing.CUBIC, Insets.of(3));
        this.compoundExpandAnimation = Animation.compose(compoundHeadsLayoutAnimation, headsLayoutMarginAnimation, compoundHeadsLayoutPaddingAnimation);

        //bottom buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(OPEN_SKIN_FOLDER_ID), true, button -> Util.getOperatingSystem().open(SKIN_SAVE_FOLDER_PATH.toFile()));

        // nav var
        this.searchField = TextBoxRow.setup(rootComponent, SEARCH_ID, "", 128, s -> this.applyFilters());

        this.skinPreEditButtons = new HashMap<>();
        for (SkinPreEditOption preEditOption : SkinPreEditOption.values()) {
            FlowLayout skinPreEditButtonLayout = rootComponent.childById(FlowLayout.class, preEditOption.getId());
            checkNull(skinPreEditButtonLayout, "flow-layout", preEditOption.getId());
            this.setupPreEditButton(skinPreEditButtonLayout, preEditOption, this.skinPreEditButtons, skinPreEditOption -> {
                this.selectedSkinPreEdit = skinPreEditOption;

                if (this.skinElements.imageButton().hasImage()) {
                    this.updatePreviews();
                }
            });
        }
        this.skinPreEditButtons.get(SkinPreEditOption.OVERLAP).onPress();

        this.headCategoryCollapsible = rootComponent.childById(CollapsibleContainer.class, HEAD_CATEGORY_ID);
        checkNull(this.headCategoryCollapsible, "collapsible", HEAD_CATEGORY_ID);
        DropdownComponent headCategoryDropdown = Components.dropdown(Sizing.content());

        for (var category : IHeadCategory.NATURAL_CATEGORIES) {
            headCategoryDropdown.button(Text.translatable(category.getTranslationKey()),
                    dropdownComponent -> this.updateCategory(category));
        }

        this.selectedCategory = IHeadCategory.NATURAL_CATEGORIES[0];
        this.updateCategoryTitle(this.selectedCategory);
        this.headCategoryCollapsible.child(headCategoryDropdown);
        int maxCategoryHorizontalSizing = FzmmUtils.getMaxWidth(Arrays.asList(IHeadCategory.NATURAL_CATEGORIES),
                this::getCategoryText) + 20;
        this.headCategoryCollapsible.horizontalSizing(Sizing.fixed(maxCategoryHorizontalSizing));

        headCategoryDropdown.zIndex(300);
        List<Component> dropdownChildren = headCategoryDropdown.children();
        if (!dropdownChildren.isEmpty() && dropdownChildren.get(0) instanceof ParentComponent parentComponent) {

            // fixes that if you click on the margins zone it clicks on the component behind the dropdown
            parentComponent.mouseDown().subscribe((mouseX, mouseY, button) -> true);
            for (var child : parentComponent.children()) {
                child.margins(Insets.of(3));
            }
        }

        this.toggleFavoriteList = ButtonRow.setup(rootComponent, TOGGLE_FAVORITE_LIST_ID, true, buttonComponent -> this.toggleFavoriteListExecute());
        checkNull(this.toggleFavoriteList, "button", TOGGLE_FAVORITE_LIST_ID);
        this.showFavorites = false;
        int toggleFavoriteListWidth = FzmmUtils.getMaxWidth(List.of(HeadComponentEntry.FAVORITE_DISABLED_TEXT, HeadComponentEntry.FAVORITE_ENABLED_TEXT)) + BUTTON_TEXT_PADDING;
        this.toggleFavoriteList.horizontalSizing(Sizing.fixed(Math.max(20, toggleFavoriteListWidth)));
        this.updateToggleFavoriteText();

        ButtonRow.setup(rootComponent, WIKI_BUTTON_ID, true, buttonComponent -> this.wikiExecute());

        this.tryLoadHeadEntries(rootComponent);
        this.updatePreviews();
    }

    private void updateCategory(IHeadCategory category) {
        this.selectedCategory = category;
        this.applyFilters();
        this.updateCategoryTitle(category);
        this.updateTogglePreEdit();
    }

    private void updateCategoryTitle(IHeadCategory category) {
        List<Component> children = this.headCategoryCollapsible.titleLayout().children();
        if (!children.isEmpty() && children.get(0) instanceof LabelComponent titleComponent) {
            titleComponent.text(this.getCategoryText(category).formatted(Formatting.UNDERLINE));
        }
    }

    private void updateTogglePreEdit() {
        if (!FzmmClient.CONFIG.headGenerator.forcePreEditNoneInModels()) {
            return;
        }

        for (var preEditOption : this.skinPreEditButtons.keySet()) {
            this.skinPreEditButtons.get(preEditOption).active = !this.selectedCategory.isModel() && preEditOption != this.selectedSkinPreEdit;
        }
    }

    @SuppressWarnings("All")
    private MutableText getCategoryText(IHeadCategory category) {
        return Text.translatable("fzmm.gui.headGenerator.label.category", Text.translatable(category.getTranslationKey()));
    }

    private void imageCallback(BufferedImage skinBase) {
        assert this.client != null;

        if (skinBase == null) {
            return;
        }

        if (ImageUtils.isEquals(skinBase, this.baseSkin)) {
            return;
        }

        if (skinBase.getWidth() == 64 && skinBase.getHeight() == 32) {
            skinBase = InternalModels.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skinBase);
            this.skinElements.imageButton().setImage(skinBase);
        }

        this.baseSkin = skinBase;
        this.gridBaseSkinEditedBody = this.baseSkin;
        this.gridBaseSkinOriginalBody = this.baseSkin;

        this.updatePreviews();
    }

    private void tryLoadHeadEntries(FlowLayout rootComponent) {
        if (!this.contentLayout.children().isEmpty()) {
            return;
        }

        List<HeadComponentEntry> headComponentList = this.getHeadComponents(HeadResourcesLoader.getPreloaded());

        if (headComponentList.isEmpty()) {
            this.addNoResultsMessage(rootComponent);
            return;
        }

        this.headComponentEntries.addAll(headComponentList);
        this.applyFilters();
    }

    private List<HeadComponentEntry> getHeadComponents(List<AbstractHeadEntry> headEntriesList) {
        List<HeadComponentEntry> headEntries = new ArrayList<>();

        for (AbstractHeadEntry entry : headEntriesList) {
            HeadComponentEntry headComponentEntry = new HeadComponentEntry(entry, this);

            if (entry instanceof HeadModelEntry modelEntry) {
                modelEntry.loadDefaultTexture();
                if (!modelEntry.isInternal()) {
                    headEntries.add(headComponentEntry);
                }
            } else {
                headEntries.add(headComponentEntry);
            }
        }

        return headEntries;
    }

    private void addNoResultsMessage(FlowLayout parent) {
        FzmmClient.LOGGER.warn("[HeadGeneratorScreen] No head entries found");
        Component label = StyledComponents.label(Text.translatable("fzmm.gui.headGenerator.label.noResults")
                        .setStyle(Style.EMPTY.withColor(FzmmStyles.ERROR_TEXT_COLOR.rgb())))
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .sizing(Sizing.expand(100), Sizing.content())
                .margins(Insets.top(4));
        FlowLayout layout = parent.childById(FlowLayout.class, "no-results-label-layout");
        checkNull(layout, "flow-layout", "no-results-label-layout");
        layout.child(label);
    }


    public void updatePreviews() {
        assert this.client != null;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        boolean compoundEntriesEditingSkinBody = this.headCompoundComponentEntries.stream()
                .anyMatch(entry -> entry.getValue().isEditingSkinBody());

        SkinPreEditOption skinPreEditOption = this.skinPreEdit();
        BufferedImage selectedPreEdit = this.skinPreEdit(this.baseSkin, skinPreEditOption, compoundEntriesEditingSkinBody);
        boolean isSlim = ImageUtils.isAlexModel(1, this.baseSkin);

        IPreviewUpdater updateAlgorithm;
        if (this.headCompoundComponentEntries.isEmpty() && FzmmClient.CONFIG.headGenerator.forcePreEditNoneInModels()) {
            updateAlgorithm = new ForceNonePreEditPreviewUpdater();
        } else {
            updateAlgorithm = new DefaultPreviewUpdater();
        }

        BufferedImage algorithmPreEdit = updateAlgorithm.getPreEdit(this.baseSkin, selectedPreEdit, isSlim,
                compoundEntriesEditingSkinBody, this);

        scheduler.schedule(() -> {
            for (int i = 0; i != this.headComponentEntries.size(); i++) {
                HeadComponentEntry entry = this.headComponentEntries.get(i);
                // Update head BufferedImage, it does not need to use MinecraftClient#execute as it does not update the GUI
                entry.updateHead(updateAlgorithm.getHead(entry, this, algorithmPreEdit, selectedPreEdit));
            }
        }, 0, TimeUnit.MILLISECONDS);

        AtomicInteger index = new AtomicInteger(1);
        for (int i = 0; i != this.headComponentEntries.size(); i++) {
            HeadComponentEntry entry = this.headComponentEntries.get(i);

            scheduler.schedule(() -> {
                // components must be updated in the client thread otherwise it may cause a crash
                this.client.execute(() -> entry.updatePreview(isSlim));
            }, (long) HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS * index.getAndIncrement(), TimeUnit.MILLISECONDS);
        }

        scheduler.schedule(() -> {
            scheduler.shutdownNow();
        }, (this.headComponentEntries.size() + 2) * HEAD_PREVIEW_SCHEDULE_DELAY_MILLIS, TimeUnit.MILLISECONDS);
    }

    public BufferedImage skinPreEdit(SkinPreEditOption skinPreEditOption, boolean editBody) {
        return this.skinPreEdit(this.baseSkin, skinPreEditOption, editBody);
    }

    public BufferedImage skinPreEdit(BufferedImage preview, SkinPreEditOption skinPreEditOption, boolean editBody) {
        BufferedImage result = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        result = skinPreEditOption.getPreEdit().execute(result, preview, List.of(SkinPart.HEAD));
        result = (editBody ? skinPreEditOption : SkinPreEditOption.NONE).getPreEdit().execute(result, preview, SkinPart.BODY_PARTS);

        return result;
    }

    public void setupPreEditButton(FlowLayout preEditLayout, SkinPreEditOption preEditOption,
                                   HashMap<SkinPreEditOption, ButtonComponent> skinPreEditButtons,
                                   Consumer<SkinPreEditOption> selectPreEditCallback) {
        preEditLayout.tooltip(Text.translatable(preEditOption.getTranslationKey() + ".tooltip"));

        ButtonComponent preEditButton = Components.button(Text.empty(), button -> {
            selectPreEditCallback.accept(preEditOption);

            for (var option : skinPreEditButtons.keySet()) {
                if (option != preEditOption)
                    skinPreEditButtons.get(option).active = true;
            }
            button.active = false;
        });
        preEditButton.horizontalSizing(Sizing.fixed(20));
        preEditButton.renderer((context, button, delta) -> {
            ButtonComponent.Renderer.VANILLA.draw(context, button, delta);
            preEditOption.getIcon().render(context, button.x() + 2, button.y() + 2, 0, 0, delta);
        });

        skinPreEditButtons.put(preEditOption, preEditButton);
        preEditLayout.child(preEditButton);
    }

    public BufferedImage getGridBaseSkin(boolean editBody) {
        return editBody ? this.gridBaseSkinEditedBody : this.gridBaseSkinOriginalBody;
    }

    public List<HeadCompoundComponentEntry> getHeadComponentEntries() {
        return this.headCompoundComponentEntries;
    }

    public void setGridBaseSkinOriginalBody(BufferedImage gridBaseSkinOriginalBody) {
        this.gridBaseSkinOriginalBody = gridBaseSkinOriginalBody;
    }

    public void setGridBaseSkinEditedBody(BufferedImage gridBaseSkinEditedBody) {
        this.gridBaseSkinEditedBody = gridBaseSkinEditedBody;
    }

    private void closeTextures() {
        if (this.contentLayout == null)
            return;

        assert this.client != null;
        this.client.execute(() -> {
            this.closeTextures(this.headComponentEntries);
            this.closeTextures(this.headCompoundComponentEntries);
        });
    }

    private void closeTextures(List<? extends AbstractHeadComponentEntry> entries) {
        for (var entry : entries) {
            entry.close();
        }
    }

    private void applyFilters() {
        if (this.searchField == null)
            return;
        String searchValue = this.searchField.getText().toLowerCase();

        for (var entry : this.headComponentEntries) {
            entry.filter(searchValue, this.showFavorites, this.selectedCategory);
        }

        List<Component> newResults = new ArrayList<>(this.headComponentEntries);
        newResults.removeIf(component -> component instanceof HeadComponentEntry entry && entry.isHide());
        this.contentLayout.clearChildren();
        this.contentLayout.children(newResults);
    }

    public void giveHead(BufferedImage image, String textureName) {
        assert this.client != null;
        this.client.execute(() -> {
            this.setUndefinedDelay();
            String headName = this.getHeadName();

            new HeadUtils().uploadHead(image, headName + " + " + textureName).thenAccept(headUtils -> {
                int delay = (int) TimeUnit.MILLISECONDS.toSeconds(headUtils.getDelayForNextInMillis());
                HeadBuilder builder = headUtils.getBuilder();
                if (!headName.isBlank())
                    builder.headName(headName);

                FzmmUtils.giveItem(builder.get());
                this.client.execute(() -> this.setDelay(delay));
            });
        });
    }

    public void setUndefinedDelay() {
        Text waitMessage = Text.translatable("fzmm.gui.headGenerator.wait");
        this.updateButton(waitMessage, false);
    }

    public void setDelay(int seconds) {
        for (int i = 0; i != seconds; i++) {
            Text message = Text.translatable("fzmm.gui.headGenerator.wait_seconds", seconds - i);
            CompletableFuture.delayedExecutor(i, TimeUnit.SECONDS).execute(() -> this.updateButton(message, false));
        }

        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS)
                .execute(() -> this.updateButton(HeadComponentOverlay.GIVE_BUTTON_TEXT, true));
    }

    public void updateButton(Text message, boolean active) {
        if (this.giveButton != null) {
            this.giveButton.setMessage(message);
            this.giveButton.active = active;
        }
    }

    public void setCurrentGiveButton(ButtonComponent currentGiveButton) {
        if (this.giveButton != null) {
            Text message = this.giveButton.getMessage();
            boolean active = this.giveButton.active;
            this.giveButton = currentGiveButton;
            this.updateButton(message, active);
        } else {
            this.giveButton = currentGiveButton;
        }
    }

    public String getHeadName() {
        return this.headNameField.getText();
    }

    public void addCompound(AbstractHeadEntry headData, BufferedImage currentPreview) {
        assert this.client != null;

        List<Component> compoundHeads = this.compoundHeadsLayout.children();
        if (compoundHeads.isEmpty()) {
            this.compoundExpandAnimation.forwards();
            this.compoundHeadsLayout.surface(this.compoundHeadsLayout.styledPanel());
        }

        HeadCompoundComponentEntry entry = new HeadCompoundComponentEntry(headData, this.compoundHeadsLayout, this, currentPreview);

        this.headCompoundComponentEntries.add(entry);
        this.compoundHeadsLayout.child(entry);
        this.updatePreviews();
    }

    public void removeCompound(HeadCompoundComponentEntry entry) {
        assert this.parent != null;
        entry.close();
        this.compoundHeadsLayout.removeChild(entry);
        this.headCompoundComponentEntries.remove(entry);

        if (this.headCompoundComponentEntries.isEmpty()) {
            this.compoundExpandAnimation.backwards();
            this.compoundHeadsLayout.surface(Surface.BLANK);
        }

        this.updatePreviews();
    }

    private void toggleFavoriteListExecute() {
        this.showFavorites = !this.showFavorites;
        this.updateToggleFavoriteText();
        this.applyFilters();
    }

    private void updateToggleFavoriteText() {
        this.toggleFavoriteList.setMessage(this.showFavorites ? HeadComponentEntry.FAVORITE_ENABLED_TEXT : HeadComponentEntry.FAVORITE_DISABLED_TEXT);
    }

    private void wikiExecute() {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK);

            this.client.setScreen(this);
        }, FzmmWikiConstants.HEAD_GENERATOR_WIKI_LINK, true));
    }

    public SkinPreEditOption skinPreEdit() {
        return this.selectedSkinPreEdit;
    }

    public void upCompoundEntry(AbstractHeadComponentEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadComponentEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.upEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    public void downCompoundEntry(AbstractHeadComponentEntry entry) {
        List<IListEntry<AbstractHeadEntry>> list = new ArrayList<>();
        for (var component : this.compoundHeadsLayout.children()) {
            if (component instanceof AbstractHeadComponentEntry headEntry) {
                list.add(headEntry);
            }
        }
        ListUtils.downEntry(list, entry, () -> {
        });
        this.updatePreviews();
    }

    @Override
    public void close() {
        super.close();
        this.closeTextures();

        if (!this.favoritesHeadsOnOpenScreen.equals(FzmmClient.CONFIG.headGenerator.favoriteSkins()))
            FzmmClient.CONFIG.save();
    }

    private void onChangeSkinField(String value) {
        AtomicReference<ImageMode> mode = this.skinElements.mode();

        if (mode.get().isHeadName() && this.headNameField.getText().equals(this.previousSkinName)) {
            this.headNameField.text(value);
        }

        this.previousSkinName = value;
    }

    public void collapseCategories() {
        if (this.headCategoryCollapsible.expanded()) {
            this.headCategoryCollapsible.toggleExpansion();
        }
    }

    @Override
    public void setMemento(IMementoObject memento) {
        HeadGeneratorScreen.memento = (HeadGeneratorMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new HeadGeneratorMemento(
                this.headNameField.getText(),
                this.skinElements.mode().get(),
                this.skinElements.valueField().getText(),
                this.showFavorites,
                this.skinPreEdit(),
                this.selectedCategory,
                this.searchField.getText()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        HeadGeneratorMemento memento = (HeadGeneratorMemento) mementoObject;
        this.skinElements.imageModeButtons().get(memento.skinMode).onPress();
        this.skinElements.valueField().text(memento.skinRowValue);
        this.headNameField.text(memento.headName);
        if (memento.showFavorites)
            this.toggleFavoriteListExecute();
        this.skinPreEditButtons.get(memento.skinPreEditOption).onPress();
        this.updateCategory(memento.category);
        this.searchField.text(memento.search);
    }

    private record HeadGeneratorMemento(String headName, ImageMode skinMode, String skinRowValue, boolean showFavorites,
                                        SkinPreEditOption skinPreEditOption, IHeadCategory category,
                                        String search) implements IMementoObject {
    }
}