package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.*;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import fzmm.zailer.me.client.gui.components.style.component.StyledLabelComponent;
import fzmm.zailer.me.client.gui.components.style.container.StyledScrollContainer;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTabIdentifier;
import fzmm.zailer.me.client.gui.components.tabs.ITabsEnum;
import fzmm.zailer.me.client.gui.components.tabs.ScreenTabContainer;
import fzmm.zailer.me.client.gui.main.components.MainButtonComponent;
import fzmm.zailer.me.client.gui.text_format.components.ColorListContainer;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarScreen;
import fzmm.zailer.me.client.gui.utils.memento.IMemento;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.compat.symbol_chat.SymbolChatCompat;
import fzmm.zailer.me.compat.symbol_chat.components.FontTextBoxComponent;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.FocusHandler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class BaseFzmmScreen extends BaseUIModelScreen<StyledFlowLayout> implements ISnackBarScreen {
    @Nullable
    protected Screen parent;
    protected final String baseScreenTranslationKey;
    public static final int BUTTON_TEXT_PADDING = 8;
    public static final int COMPONENT_DISTANCE = 8;
    private final SymbolChatCompat symbolChatCompat;
    protected final HashMap<String, IScreenTab> tabs;
    protected final FlowLayout snackBarLayout;

    public BaseFzmmScreen(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(StyledFlowLayout.class, DataSource.asset(Identifier.of(FzmmClient.MOD_ID, screenPath)));
        this.baseScreenTranslationKey = baseScreenTranslationKey;
        this.parent = parent;
        this.tabs = new HashMap<>();
        this.symbolChatCompat = new SymbolChatCompat();
        this.snackBarLayout = new SnackBarLayout(Sizing.content(), Sizing.content());
    }

    @Override
    protected void build(StyledFlowLayout rootComponent) {
        assert this.client != null;
        ButtonComponent backButton = rootComponent.childById(ButtonComponent.class, "back-button");
        if (backButton != null) {
            backButton.onPress(button -> this.close());
        }

        this.setup(rootComponent);
        rootComponent.child(this.snackBarLayout);
    }

    @Override
    protected void init() {
        super.init();
        if (FzmmClient.CONFIG.history.automaticallyRecoverScreens() && this instanceof IMementoScreen mementoScreen) {
            mementoScreen.getMemento().ifPresent(mementoScreen::restoreMemento);
        }

        if (this.getRoot().focusHandler() != null) {
            this.initFocus(this.getRoot().focusHandler());
        }

        // FIXME: https://github.com/wisp-forest/owo-lib/issues/340
        if (!this.invalid && this.uiAdapter != null) {
            ScreenEvents.afterRender(this).register((screen, drawContext, mouseX, mouseY, tickDelta) -> {
                if (this.uiAdapter != null) {
                    this.uiAdapter.drawTooltip(drawContext, mouseX, mouseY, tickDelta);
                }
            });
        }
    }

    protected void initFocus(FocusHandler focusHandler) {

    }

    protected abstract void setup(FlowLayout rootComponent);

    @Override
    public void removed() {
        this.clearSnackBars();

        if (FzmmClient.CONFIG.history.automaticallyRecoverScreens() && this instanceof IMementoScreen mementoScreen) {
            try {
                mementoScreen.setMemento(mementoScreen.createMemento());
            } catch (NullPointerException e) {
                FzmmClient.LOGGER.error("[BaseFzmmScreen] Failed to create memento", e);
            }
        }

        super.removed();
    }

    @Override
    public void close() {
        this.setScreen(this.parent);
    }

    protected void setTabs(Enum<? extends ITabsEnum> tabs) {
        this.setTabs(this.tabs, tabs);
    }

    protected void setTabs(HashMap<String, IScreenTab> hashMap, Enum<? extends ITabsEnum> tabs) {
        for (var tab : tabs.getDeclaringClass().getEnumConstants())
            hashMap.put(tab.getId(), tab.createTab());
    }

    protected HashMap<String, IMementoObject> createMementoTabs() {
        return this.createMementoTabs(this.tabs);
    }

    protected HashMap<String, IMementoObject> createMementoTabs(HashMap<String, IScreenTab> tabsHashMap) {
        HashMap<String, IMementoObject> tabs = new HashMap<>();
        for (var tab : tabsHashMap.values()) {
            if (tab instanceof IMemento mementoTab)
                tabs.put(tab.getId(), mementoTab.createMemento());

        }
        return tabs;
    }

    protected void restoreMementoTabs(HashMap<String, IMementoObject> mementoTabs) {
        this.restoreMementoTabs(mementoTabs, this.tabs);
    }

    protected void restoreMementoTabs(HashMap<String, IMementoObject> mementoTabs, HashMap<String, IScreenTab> tabsHashMap) {
        for (var tab : tabsHashMap.values()) {
            if (tab instanceof IMemento mementoTab)
                mementoTab.restoreMemento(mementoTabs.get(tab.getId()));
        }
    }

    public <T extends Enum<? extends IScreenTabIdentifier>> T selectScreenTab(FlowLayout rootComponent, IScreenTabIdentifier selectedTab, T tabs) {
        return this.selectScreenTab(rootComponent, selectedTab, tabs, this.tabs, true);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<? extends IScreenTabIdentifier>> T selectScreenTab(FlowLayout rootComponent, IScreenTabIdentifier selectedTab,
                                                                              T tabs, HashMap<String, IScreenTab> tabsHashMap, boolean addLabel) {
        for (var tabId : tabsHashMap.keySet()) {
            ScreenTabContainer screenTabContainer = rootComponent.childById(ScreenTabContainer.class, ScreenTabContainer.getScreenTabId(tabId));
            ButtonWidget screenTabButton = rootComponent.childById(ButtonWidget.class, ScreenTabRow.getScreenTabButtonId(tabId));
            boolean isSelectedTab = selectedTab.getId().equals(tabId);

            if (screenTabContainer != null) {
                screenTabContainer.setSelected(isSelectedTab, addLabel);
            }

            if (screenTabButton != null) {
                screenTabButton.active = !isSelectedTab;
            }
        }


        Optional<T> result = (Optional<T>) Arrays.stream(tabs.getDeclaringClass().getEnumConstants())
                .filter(tab -> tab.getId().equals(selectedTab.getId()))
                .findFirst();

        assert result.isPresent();

        return result.get();
    }

    public <T extends IScreenTab> T getTab(IScreenTabIdentifier tab, Class<T> ignored) {
        return this.getTab(tab, ignored, this.tabs);
    }

    @SuppressWarnings("unchecked")
    public <T extends IScreenTab> T getTab(IScreenTabIdentifier tab, Class<T> ignored, HashMap<String, IScreenTab> tabsHashMap) {
        return (T) tabsHashMap.get(tab.getId());
    }

    public String getBaseScreenTranslationKey() {
        return this.baseScreenTranslationKey;
    }


    public static String getBaseTranslationKey(Element element) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        return currentScreen instanceof BaseFzmmScreen baseFzmmScreen ? baseFzmmScreen.getBaseScreenTranslationKey() : element.getAttribute("baseScreenTranslationKey");
    }

    public static String getBaseTranslationKey(String baseTranslationKey) {
        return "fzmm.gui." + baseTranslationKey;
    }

    public static String getTabTranslationKey(String baseScreenTranslationKey) {
        return getBaseTranslationKey(baseScreenTranslationKey) + ".tab.";
    }

    public static String getOptionBaseTranslationKey(String baseScreenTranslationKey) {
        return getBaseTranslationKey(baseScreenTranslationKey) + ".option.";
    }

    public void child(Component child) {
        this.uiAdapter.rootComponent.child(child);
    }

    @Override
    public FlowLayout getSnackBarLayout() {
        return this.snackBarLayout;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.symbolChatCompat.symbol().isMounted()) {
                this.symbolChatCompat.symbol().remove();
                this.symbolChatCompat.selectedComponent(null);
                return true;
            }

            if (this.symbolChatCompat.font().isMounted()) {
                this.symbolChatCompat.font().remove();
                this.symbolChatCompat.selectedComponent(null);
                return true;
            }
        }

        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        return this.symbolChatCompat.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (super.charTyped(chr, modifiers))
            return true;

        return this.symbolChatCompat.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public SymbolChatCompat getSymbolChatCompat() {
        return this.symbolChatCompat;
    }

    static {
        // rows
        //TODO: replace rows with better UI components
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "boolean-row"), BooleanRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "button-row"), ButtonRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "color-row"), ColorRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "predicate-text-box-row"), ConfigTextBoxRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "context-menu-button-row"), ContextMenuButtonRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "image-rows"), ImageRows::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "number-row"), NumberRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "screen-tab-row"), ScreenTabRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "slider-row"), SliderRow::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "text-box-row"), TextBoxRow::parse);

        // styled components
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "styled-label"), element -> new StyledLabelComponent(Text.empty()));
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "styled-flow-layout"), StyledFlowLayout::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "styled-scroll"), StyledScrollContainer::parse);

        // these are necessary in case you want to create the fields manually with XML
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "book"), element -> new BookComponent());
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "boolean-button"), BooleanButton::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "context-menu-button"), element -> new ContextMenuButton(Text.empty()));
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "number-slider"), element -> new SliderWidget());
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "text-option"), element -> new ConfigTextBox());
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "suggest-text-option"), element -> new SuggestionTextBox());
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "image-option"), element -> new ImageButtonComponent());
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "screen-tab"), ScreenTabContainer::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "main-button"), element -> new MainButtonComponent(Text.empty(), buttonComponent -> {}));
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "screenshot-zone"), element -> new ScreenshotZoneComponent());
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "color-list"), ColorListContainer::parse);
        UIParsing.registerFactory(Identifier.of(FzmmClient.MOD_ID, "font-text-box"), element -> new FontTextBoxComponent(Sizing.fixed(100)));

    }

    @Contract(value = "null, _, _ -> fail;", pure = true)
    public static void checkNull(Component component, String componentTagName, String id) {
        Objects.requireNonNull(component, String.format("No '%s' found with component titleId '%s'", componentTagName, id));
    }

    public UIModel getModel() {
        return this.model;
    }

    public FlowLayout getRoot() {
        return this.uiAdapter.rootComponent;
    }
}
