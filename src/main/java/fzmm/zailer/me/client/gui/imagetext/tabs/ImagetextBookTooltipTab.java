package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.imagetext.ImagetextBookOption;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.exceptions.BookNbtOverflow;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.concurrent.TimeUnit;

public class ImagetextBookTooltipTab implements IImagetextTab {
    private static final String BOOK_TOOLTIP_MODE_ID = "bookTooltipMode";
    private static final String BOOK_TOOLTIP_AUTHOR_ID = "bookTooltipAuthor";
    private static final String BOOK_TOOLTIP_MESSAGE_ID = "bookTooltipMessage";
    private EnumWidget bookTooltipMode;
    private TextBoxComponent bookTooltipAuthor;
    private TextAreaComponent bookTooltipMessage;

    @Override
    public void generate(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        ImagetextBookOption bookOption = (ImagetextBookOption) this.bookTooltipMode.getValue();
        String author = this.bookTooltipAuthor.getText();
        String bookMessage = this.bookTooltipMessage.getText();

        BookBuilder bookBuilder = bookOption.getBookBuilder()
                .author(author)
                .addPage(Text.literal(Formatting.BLUE + bookMessage)
                        .setStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, logic.getText()))
                        )
                );

        ItemStack book = bookBuilder.get();
        assert book.getNbt() != null;

        long bookLength = FzmmUtils.getLengthInBytes(book);
        if (bookLength > BookNbtOverflow.MAX_BOOK_NBT_SIZE) {
            MinecraftClient.getInstance().execute(() -> {
                ISnackBarComponent toast = BaseSnackBarComponent.builder()
                        .title(Text.translatable("fzmm.snack_bar.bookTooltip.overflow.title", bookLength, BookNbtOverflow.MAX_BOOK_NBT_SIZE))
                        .details(Text.translatable("fzmm.snack_bar.bookTooltip.overflow.details"))
                        .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                        .timer(5, TimeUnit.SECONDS)
                        .startTimer()
                        .closeButton()
                        .build();
                FzmmUtils.addSnackBar(toast);
            });
        } else {
            FzmmUtils.giveItem(book);
        }
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.bookTooltipMode = EnumRow.setup(rootComponent, BOOK_TOOLTIP_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
        assert MinecraftClient.getInstance().player != null;
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

    @Override
    public String getId() {
        return "bookTooltip";
    }

    @Override
    public IMementoObject createMemento() {
        return new BookTooltipMementoTab((ImagetextBookOption) this.bookTooltipMode.getValue(),
                this.bookTooltipAuthor.getText(), this.bookTooltipMessage.getText());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        BookTooltipMementoTab memento = (BookTooltipMementoTab) mementoTab;
        this.bookTooltipAuthor.text(memento.author);
        this.bookTooltipMessage.text(memento.message);
        this.bookTooltipMode.setValue(memento.mode);
    }

    private record BookTooltipMementoTab(ImagetextBookOption mode, String author, String message) implements IMementoObject {
    }
}
