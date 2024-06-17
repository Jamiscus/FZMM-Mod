package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.imagetext.ImagetextBookOption;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.client.toast.BookNbtOverflowToast;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
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
    private EnumWidget bookTooltipMode;
    private TextBoxComponent bookTooltipAuthor;
    private TextBoxComponent bookTooltipMessage;

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

        WrittenBookContentComponent bookContent = book.getComponents().get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();

        if (bookContent == null) {
            FzmmClient.LOGGER.warn("[ImagetextBookTooltipTab] Book has no written book content component");
            return;
        }

        for (var pageFilteredPair : bookContent.pages()) {
            Text pageText = pageFilteredPair.raw();
            if (pageText != null && WrittenBookContentComponent.exceedsSerializedLengthLimit(pageText, registryManager)) {
                int length = Text.Serialization.toJsonString(pageText, registryManager).length();
                MinecraftClient.getInstance().getToastManager().add(new BookNbtOverflowToast(length));
            }
        }

        FzmmUtils.giveItem(book);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.bookTooltipMode = EnumRow.setup(rootComponent, BOOK_TOOLTIP_MODE_ID, ImagetextBookOption.ADD_PAGE, null);
        assert MinecraftClient.getInstance().player != null;
        this.bookTooltipAuthor = TextBoxRow.setup(rootComponent, BOOK_TOOLTIP_AUTHOR_ID, MinecraftClient.getInstance().player.getName().getString(), 512);
        this.bookTooltipMessage = TextBoxRow.setup(rootComponent, BOOK_TOOLTIP_MESSAGE_ID, FzmmClient.CONFIG.imagetext.defaultBookMessage(), 1024);
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
