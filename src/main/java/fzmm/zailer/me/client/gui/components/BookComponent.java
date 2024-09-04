package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.input.CursorMovement;
import net.minecraft.text.Style;

public class BookComponent extends TextAreaComponent {

    public BookComponent() {
        // vanilla book screen parity

        // rightWidgetOffset = 10
        // text widget width diff = this.getScrollerWidth() = 8
        // bottomWidgetOffset = displayCharCount offset = 4
        super(Sizing.fixed(BookScreen.MAX_TEXT_WIDTH + 8 + 10), Sizing.fixed(BookScreen.MAX_TEXT_HEIGHT + 4));

        int rightWidgetOffset = 10;
        int bottomWidgetOffset = 4;
        int top = 26; // matching vanilla
        int bottom = BookScreen.HEIGHT - BookScreen.MAX_TEXT_HEIGHT - top - bottomWidgetOffset;
        int left = 32; // matching vanilla
        int right = BookScreen.WIDTH - BookScreen.MAX_TEXT_WIDTH - left - this.getScrollerWidth() - rightWidgetOffset;
        // 192x192 and align text with texture
        this.margins(Insets.of(top, bottom, left, right));

        this.maxLines(BookScreen.MAX_TEXT_HEIGHT / MinecraftClient.getInstance().textRenderer.fontHeight);

        this.editBox.setChangeListener(this::textChange);
    }

    @Override
    public void drawBox(DrawContext context, int x, int y, int width, int height) {
        Insets margins = this.margins().get();
        context.drawTexture(BookScreen.BOOK_TEXTURE, this.x() - margins.left(), this.y() - margins.top(), 0, 0, BookScreen.WIDTH, BookScreen.HEIGHT);
    }

    @Override
    protected boolean overflows() {
        return false;
    }

    @Override
    public int heightOffset() {
        return 0;
    }

    @Override
    public int widthOffset() {
        return 0;
    }

    private void textChange(String value) {
        // remove overflow text because edit box does not allow to disable it
        int overflow = this.editBox.getLineCount() - this.maxLines();
        if (overflow <= 0) {
            return;
        }
        TextHandler textHandler = MinecraftClient.getInstance().textRenderer.getTextHandler();
        int cursor = this.editBox.getCursor();
        String modifiedText = value;
        int difference = 0;
        // as I cannot know where the cursor was before the text changed,
        // the characters are removed until the text enters into the line limit,
        // this is preferable to deleting the last line in case text is added
        // in the middle of the book and exceeds the maximum number of lines.
        while (textHandler.wrapLines(modifiedText, BookScreen.MAX_TEXT_WIDTH, Style.EMPTY).size() > this.maxLines()) {
            int index = Math.min(Math.abs(cursor - difference), modifiedText.length());
            modifiedText = modifiedText.substring(0, index - 1) + modifiedText.substring(index);
        }

        if (!modifiedText.equals(value)) {
            this.text(modifiedText);
            this.editBox.moveCursor(CursorMovement.ABSOLUTE, cursor - difference - 1);
        }
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return false;
    }
}
