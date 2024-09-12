package fzmm.zailer.me.client.gui.player_statue;


import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.row.*;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.player_statue.tabs.IPlayerStatueTab;
import fzmm.zailer.me.client.gui.player_statue.tabs.PlayerStatueTabs;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.utils.FzmmWikiConstants;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class PlayerStatueScreen extends BaseFzmmScreen implements IMementoScreen {
    private static final String HORIZONTAL_DIRECTION_ID = "horizontal-direction";
    private static final String POS_X_ID = "posX";
    private static final String POS_Y_ID = "posY";
    private static final String POS_Z_ID = "posZ";
    private static final String NAME_ID = "name";
    public static final String EXECUTE_ID = "execute";
    private static PlayerStatueTabs selectedTab = PlayerStatueTabs.CREATE;
    private static PlayerStatueMemento memento = null;
    private HorizontalDirectionOption direction;
    private ConfigTextBox posX;
    private ConfigTextBox posY;
    private ConfigTextBox posZ;
    private TextBoxComponent nameField;

    private ButtonWidget executeButton;


    public PlayerStatueScreen(@Nullable Screen parent) {
        super("player_statue", "playerStatue", parent);
    }

    @Override
    protected void setup(FlowLayout rootComponent) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        //general
        ContextMenuButton directionButton = rootComponent.childById(ContextMenuButton.class, ContextMenuButtonRow.getButtonId(HORIZONTAL_DIRECTION_ID));
        BaseFzmmScreen.checkNull(directionButton, "context-menu-button", ContextMenuButtonRow.getButtonId(HORIZONTAL_DIRECTION_ID));
        directionButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : HorizontalDirectionOption.values()) {
                dropdownComponent.button(Text.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.direction = option;
                    directionButton.setMessage(Text.translatable(option.getTranslationKey()));
                    dropdownButton.remove();
                });
            }
        });
        this.direction = HorizontalDirectionOption.getPlayerHorizontalDirection();
        directionButton.setMessage(Text.translatable(this.direction.getTranslationKey()));
        this.posX = NumberRow.setup(rootComponent, POS_X_ID, player.getBlockX(), Float.class);
        this.posY = NumberRow.setup(rootComponent, POS_Y_ID, player.getY(), Float.class);
        this.posZ = NumberRow.setup(rootComponent, POS_Z_ID, player.getBlockZ(), Float.class);
        this.nameField = TextBoxRow.setup(rootComponent, NAME_ID, "", 0xFFFF);
        //tabs
        this.setTabs(selectedTab);
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var playerStatueTab : PlayerStatueTabs.values()) {
            IScreenTab tab = this.getTab(playerStatueTab, IPlayerStatueTab.class);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button -> {
                selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab);
                this.executeButton.active = this.getTab(selectedTab, IPlayerStatueTab.class).canExecute();
            });
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);
        //buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId("faq"), true, this::faqExecute);
        this.executeButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(EXECUTE_ID), true, this::execute);
        this.executeButton.active = this.getTab(selectedTab, IPlayerStatueTab.class).canExecute();
    }

    private void faqExecute(ButtonWidget buttonWidget) {
        assert this.client != null;
        ConfirmLinkScreen.open(this.client.currentScreen, FzmmWikiConstants.PLAYER_STATUE_WIKI_LINK, true);
    }

    private void execute(ButtonWidget buttonWidget) {
        float x = (float) this.posX.parsedValue();
        float y = (float) this.posY.parsedValue();
        float z = (float) this.posZ.parsedValue();
        String name = this.nameField.getText();

        this.getTab(selectedTab, IPlayerStatueTab.class).execute(this.direction, x, y, z, name);
    }

    @Override
    public void setMemento(IMementoObject memento) {
        PlayerStatueScreen.memento = (PlayerStatueMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new PlayerStatueMemento(
                this.nameField.getText(),
                this.createMementoTabs()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        PlayerStatueMemento memento = (PlayerStatueMemento) mementoObject;
        this.nameField.text(memento.name());
        this.restoreMementoTabs(memento.mementoTabHashMap);
    }

    private record PlayerStatueMemento(String name,
                                       HashMap<String, IMementoObject> mementoTabHashMap) implements IMementoObject {

    }
}