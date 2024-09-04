package fzmm.zailer.me.client.gui.encrypt_book;


import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.containers.ConfirmOverlay;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.component.StyledLabelComponent;
import fzmm.zailer.me.client.gui.components.style.container.StyledFlowLayout;
import fzmm.zailer.me.client.gui.encrypt_book.components.AddEncryptProfileOverlay;
import fzmm.zailer.me.client.gui.encrypt_book.components.DecryptorSaverOverlay;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.enycrpt_book.EncryptbookLogic;
import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmWikiConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EncryptBookScreen extends BaseFzmmScreen implements IMementoScreen {
    private static EncryptBookMemento memento = null;
    private TextAreaComponent messageTextArea;
    private TextBoxComponent paddingCharactersField;
    private TextBoxComponent authorField;
    private TextBoxComponent titleField;
    private FlowLayout decryptorProfileLayout;
    private LabelComponent decryptorStatus;
    @Nullable
    private TranslationEncryptProfile selectedProfile = null;
    private int selectedProfileIndex;

    public EncryptBookScreen(@Nullable Screen parent) {
        super("encrypt_book", "encryptbook", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;

        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        // message
        this.messageTextArea = rootComponent.childById(TextAreaComponent.class, "message-text-area");
        BaseFzmmScreen.checkNull(this.messageTextArea, "text-area", "message-text-area");
        this.messageTextArea.text(config.defaultBookMessage());

        // book options
        this.authorField = TextBoxRow.setup(rootComponent, "author", this.client.player.getName().getString(), 512);
        this.titleField = TextBoxRow.setup(rootComponent, "title", config.defaultBookTitle(), 256);

        // encryptbook options
        String configPadding = config.padding();
        this.paddingCharactersField = TextBoxRow.setup(rootComponent, "paddingCharacters", configPadding, 512);
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId("add-profile"), true, this::addProfileOverlay);
        if (this.paddingCharactersField instanceof SuggestionTextBox suggestionTextBox) {
            suggestionTextBox.setSuggestionProvider((context, builder) -> {
                String defaultValue = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_,.";
                builder.suggest(defaultValue);
                if (!defaultValue.equals(configPadding)) {
                    builder.suggest(configPadding);
                }
                return CompletableFuture.completedFuture(builder.build());
            });
        }

        this.decryptorStatus = rootComponent.childById(StyledLabelComponent.class, "profile-status");
        BaseFzmmScreen.checkNull(this.decryptorStatus, "fzmm.styled-label", "profile-status");
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId("get-decryptor"), true, buttonComponent -> this.decryptorSaverOverlay(this.selectedProfile));

        this.decryptorProfileLayout = rootComponent.childById(StyledFlowLayout.class, "profile-list");
        BaseFzmmScreen.checkNull(this.decryptorProfileLayout, "fzmm.styled-flow-layout", "profile-list");
        this.updateDecryptorProfileList();
        this.selectProfile(0);

        // bottom buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId("give"), true, buttonComponent -> this.giveBook(false));
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId("add-page"), true, buttonComponent -> this.giveBook(true));

        // other
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId("faq"), true, this::faqExecute);
    }

    private void updateDecryptorProfileList() {
        List<TranslationEncryptProfile> decryptorProfiles = getProfiles();

        List<Component> componentList = new ArrayList<>();
        for (int i = 0; i < decryptorProfiles.size(); i++) {
            TranslationEncryptProfile profile = decryptorProfiles.get(i);
            int finalI = i;
            StyledFlowLayout component = this.getModel().expandTemplate(StyledFlowLayout.class, "profile-option", Map.of()).configure(layout -> {
                StyledLabelComponent label = layout.childById(StyledLabelComponent.class, "label");
                BaseFzmmScreen.checkNull(label, "fzmm.styled-label", "label");

                label.text(Text.translatable("fzmm.gui.encryptbook.label.profile",
                        profile.translationKey(),
                        profile.length(),
                        profile.isAsymmetric(),
                        profile.isOldAlgorithm()
                ));

                layout.mouseDown().subscribe((mouseX, mouseY, button) -> this.profileSelect(layout, profile, finalI));

                ButtonComponent removeButton = layout.childById(ButtonComponent.class, "remove-button");
                BaseFzmmScreen.checkNull(removeButton, "button", "remove-button");

                //noinspection CodeBlock2Expr
                removeButton.onPress(button -> {
                    this.addOverlay(new ConfirmOverlay(Text.translatable("fzmm.gui.encryptbook.label.removeDecryptor"), aBoolean -> {
                        if (aBoolean) {
                            FzmmClient.CONFIG.encryptbook.profiles().remove(profile.toModel());
                            FzmmClient.CONFIG.save();
                            layout.remove();

                            this.updateDecryptorStatus(this.selectedProfile);
                        }
                    }));
                });
            });

            componentList.add(component);
        }

        this.decryptorProfileLayout.clearChildren();
        this.decryptorProfileLayout.children(componentList);
    }

    private boolean profileSelect(FlowLayout profileLayout, TranslationEncryptProfile profile, int index) {
        this.selectedProfile = profile;
        this.selectedProfileIndex = index;

        for (var child : this.decryptorProfileLayout.children()) {
            if (!(child instanceof FlowLayout childLayout)) {
                continue;
            }

            Surface surface = Surface.flat(childLayout == profileLayout ? FzmmStyles.SELECTED_COLOR : FzmmStyles.UNSELECTED_COLOR);
            childLayout.surface(surface);
        }

        this.updateDecryptorStatus(profile);
        this.messageTextArea.setMaxLength(profile.length());

        return true;
    }

    public void addProfileOverlay(ButtonWidget buttonWidget) {
        this.addOverlay(new AddEncryptProfileOverlay(profile -> {
            FzmmClient.CONFIG.encryptbook.profiles().add(profile.toModel());
            FzmmClient.CONFIG.save();
            this.updateDecryptorProfileList();
            this.selectProfile(this.decryptorProfileLayout.children().size() - 1);

            this.decryptorSaverOverlay(profile);
        }));
    }

    private void decryptorSaverOverlay(TranslationEncryptProfile profile) {
        this.addOverlay(new DecryptorSaverOverlay(profile));
    }

    public void selectProfile(int index) {
        List<Component> profileLayout = this.decryptorProfileLayout.children();
        if (profileLayout.isEmpty()) {
            return;
        }
        int selectedProfileIndex = index < profileLayout.size() ? index : 0;
        profileLayout.get(selectedProfileIndex).onMouseDown(0, 0, 0);
    }

    public void updateDecryptorStatus(@Nullable TranslationEncryptProfile profile) {
        Text result;
        boolean isValid = false;

        String translationValue = "fzmm.gui.encryptbook.label.profile.";

        if (profile != null && I18n.hasTranslation(profile.translationKey())) {
            String decryptString = Text.translatable(profile.translationKey()).getString();

            isValid = decryptString.equals(profile.decryptorValue());
            String status = isValid ? "loaded" : "outdated";
            result = Text.translatable(translationValue + status);
        } else {
            result = Text.translatable(translationValue + "notFound");
        }

        result = result.copy().setStyle(Style.EMPTY
                .withColor((isValid ? FzmmStyles.TEXT_SUCCESS_COLOR : FzmmStyles.TEXT_ERROR_COLOR).rgb()));

        this.decryptorStatus.text(result);
    }

    private void giveBook(boolean isAddPage) {
        if (this.selectedProfile == null) {
            return;
        }
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;

        String message = this.messageTextArea.getText();
        if (message.isEmpty()) {
            message = config.defaultBookMessage();
        }

        String paddingChars = this.paddingCharactersField.getText();
        if (paddingChars.isEmpty()) {
            paddingChars = " ";
        }

        String author = this.authorField.getText();
        String title = this.titleField.getText();

        EncryptbookLogic.give(message, author, paddingChars, title, this.selectedProfile, isAddPage);
    }

    private void faqExecute(ButtonWidget buttonWidget) {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool) {
                Util.getOperatingSystem().open(FzmmWikiConstants.ENCRYPT_BOOK_WIKI_LINK);
            }

            this.client.setScreen(this);
        }, FzmmWikiConstants.ENCRYPT_BOOK_WIKI_LINK, true));
    }

    public static List<TranslationEncryptProfile> getProfiles() {
        return TranslationEncryptProfile.of(FzmmClient.CONFIG.encryptbook.profiles());
    }

    @Override
    public void setMemento(IMementoObject memento) {
        EncryptBookScreen.memento = (EncryptBookMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new EncryptBookMemento(
                this.messageTextArea.getText(),
                this.authorField.getText(),
                this.titleField.getText(),
                this.paddingCharactersField.getText(),
                this.selectedProfileIndex
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        EncryptBookMemento memento = (EncryptBookMemento) mementoObject;
        this.messageTextArea.text(memento.message);
        this.authorField.text(memento.author);
        this.titleField.text(memento.title);
        this.paddingCharactersField.text(memento.paddingCharacters);
        this.selectProfile(memento.selectedProfileIndex);
    }

    private record EncryptBookMemento(String message, String author, String title, String paddingCharacters,
                                      int selectedProfileIndex) implements IMementoObject {
    }
}