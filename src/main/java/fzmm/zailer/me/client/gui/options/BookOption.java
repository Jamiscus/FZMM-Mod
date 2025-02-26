package fzmm.zailer.me.client.gui.options;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.gui.components.IMode;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.function.Supplier;

public enum BookOption implements IMode {
    CREATE_BOOK("createBook", () -> BookBuilder.builder().title(Text.translatable("fzmm.item.imagetext.book.title").getString())),
    ADD_PAGE("addPage", () -> {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        return BookBuilder.of(ItemUtils.from(Hand.MAIN_HAND)).orElse(CREATE_BOOK.bookBuilderSupplier.get());
    });


    private final String name;
    private final Supplier<BookBuilder> bookBuilderSupplier;

    BookOption(String name, Supplier<BookBuilder> getBookSupplier) {
        this.name = name;
        this.bookBuilderSupplier = getBookSupplier;
    }

    @Override
    public String getTranslationKey() {
        return "fzmm.gui.option.book." + this.name;
    }

    public BookBuilder getBookBuilder() {
        return this.bookBuilderSupplier.get();
    }
}