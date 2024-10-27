package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.options.BookOption;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ImagetextBookTooltipTab implements IImagetextTab {
    private static final String BOOK_TOOLTIP_MODE_ID = "bookTooltipMode";
    private static final String BOOK_TOOLTIP_AUTHOR_ID = "bookTooltipAuthor";
    private static final String BOOK_TOOLTIP_MESSAGE_ID = "bookTooltipMessage";
    private ContextMenuButton bookTooltipButton;
    private BookOption bookMode;
    private TextBoxComponent bookTooltipAuthor;
    private TextAreaComponent bookTooltipMessage;

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        String author = this.bookTooltipAuthor.getText();
        String bookMessage = this.bookTooltipMessage.getText();

        BookBuilder bookBuilder = this.bookMode.getBookBuilder()
                .author(author)
                .addPage(Text.literal(Formatting.BLUE + bookMessage)
                        .setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, logic.getText()))
                        )
                );

        ItemStack book = bookBuilder.get();

        WrittenBookContentComponent bookContent = book.getComponents().get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();

        if (bookContent == null) {
            FzmmClient.LOGGER.warn("[ImagetextBookTooltipTab] Book has no written book content component");
            return;
        }

        for (var pageFilteredPair : bookContent.pages()) {
            Text pageText = pageFilteredPair.raw();
            if (pageText != null && WrittenBookContentComponent.exceedsSerializedLengthLimit(pageText, registryManager)) {
                int serializedLength = Text.Serialization.toJsonString(pageText, registryManager).length();
                MinecraftClient.getInstance().execute(() -> {
                    ISnackBarComponent toast = BaseSnackBarComponent.builder(SnackBarManager.IMAGETEXT_ID)
                            .title(Text.translatable("fzmm.snack_bar.bookTooltip.overflow.title", serializedLength, WrittenBookContentComponent.MAX_SERIALIZED_PAGE_LENGTH))
                            .details(Text.translatable("fzmm.snack_bar.bookTooltip.overflow.details"))
                            .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                            .keepOnLimit()
                            .highTimer()
                            .startTimer()
                            .closeButton()
                            .build();
                    SnackBarManager.getInstance().add(toast);
                });
                return;
            }
        }

        ItemUtils.give(book);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        assert MinecraftClient.getInstance().player != null;
        this.bookTooltipButton = rootComponent.childById(ContextMenuButton.class, BOOK_TOOLTIP_MODE_ID);
        BaseFzmmScreen.checkNull(this.bookTooltipButton, "context-menu-button", BOOK_TOOLTIP_MODE_ID);
        this.bookTooltipButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : BookOption.values()) {
                dropdownComponent.button(Text.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateBookTooltip(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateBookTooltip(BookOption.ADD_PAGE);
        this.bookTooltipAuthor = TextBoxRow.setup(rootComponent, BOOK_TOOLTIP_AUTHOR_ID, MinecraftClient.getInstance().player.getName().getString(), 512);
        this.bookTooltipMessage = rootComponent.childById(TextAreaComponent.class, BOOK_TOOLTIP_MESSAGE_ID + "-text-area");
        BaseFzmmScreen.checkNull(this.bookTooltipMessage, "text-area", BOOK_TOOLTIP_MESSAGE_ID + "-text-area");
        this.bookTooltipMessage.maxLines(14);
        this.bookTooltipMessage.setMaxLength(4096);
        this.bookTooltipMessage.text(FzmmClient.CONFIG.imagetext.defaultBookMessage());

        FlowLayout layout = rootComponent.childById(FlowLayout.class, BOOK_TOOLTIP_MESSAGE_ID + "-text-area-parent");
        BaseFzmmScreen.checkNull(this.bookTooltipMessage, "text-area", BOOK_TOOLTIP_MESSAGE_ID + "-text-area-parent");
        layout.verticalSizing(Sizing.content());
    }

    private void updateBookTooltip(BookOption mode) {
        this.bookMode = mode;
        this.bookTooltipButton.setMessage(Text.translatable(this.bookMode.getTranslationKey()));
    }

    @Override
    public String getId() {
        return "bookTooltip";
    }

    @Override
    public IMementoObject createMemento() {
        return new BookTooltipMementoTab(this.bookMode,
                this.bookTooltipAuthor.getText(), this.bookTooltipMessage.getText());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        BookTooltipMementoTab memento = (BookTooltipMementoTab) mementoTab;
        this.bookTooltipAuthor.text(memento.author);
        this.bookTooltipMessage.text(memento.message);
        this.updateBookTooltip(memento.mode);
    }

    private record BookTooltipMementoTab(BookOption mode, String author, String message) implements IMementoObject {
    }
}
