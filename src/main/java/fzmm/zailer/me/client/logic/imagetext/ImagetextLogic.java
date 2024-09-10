package fzmm.zailer.me.client.logic.imagetext;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ImagetextLogic {
    private List<Text> imagetext;
    private int width;
    private int height;
    private int lineWidth;

    public ImagetextLogic() {
        this.imagetext = new ArrayList<>();
        this.width = 0;
        this.height = 0;
        this.lineWidth = 0;
    }

    public void generateImagetext(IImagetextAlgorithm algorithm, ImagetextData data) {
        this.generateImagetext(algorithm, data, Integer.MAX_VALUE);
    }

    public void generateImagetext(IImagetextAlgorithm algorithm, ImagetextData data, int lineSplitInterval) {
        this.width = data.width();
        this.height = data.height();

        List<MutableText> rawLinesList = algorithm.get(this, data, lineSplitInterval);
        List<Text> linesList = rawLinesList.stream()
                .map(mutableText -> (Text) FzmmUtils.toNbtString(mutableText, false))
                .toList();

        Text firstLine = linesList.isEmpty() ? Text.empty() : linesList.get(0);
        this.lineWidth = MinecraftClient.getInstance().textRenderer.getWidth(firstLine);
        this.imagetext = new ArrayList<>(linesList);
    }

    /**
     * Adjusts the size of an image to preserve the aspect ratio when changing either width or height.
     *
     * @param value          The original dimension value (width or height).
     * @param changeValue    The new dimension value (width or height) to maintain the aspect ratio.
     * @param referenceValue The dimension value that corresponds to the original reference (width or height).
     * @return int           The new dimension value that maintains the aspect ratio.
     */
    public static int getResizedAspectRatio(int value, int changeValue, int referenceValue) {
        return (int) ((double) changeValue / value * referenceValue);
    }

    public void addResolution() {
        String message = Text.translatable("fzmm.item.imagetext.resolution", this.width, this.height).getString();
        int color = FzmmClient.CONFIG.colors.imagetextMessages().rgb();
        MutableText text = Text.translatable(message).setStyle(Style.EMPTY.withColor(color));
        this.imagetext.add(FzmmUtils.disableItalicConfig(text));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public List<Text> getWrappedText() {
        return this.imagetext;
    }

    public Text getText() {
        MutableText result = Text.empty();
        List<Text> wrappedText = this.getWrappedText();

        int size = wrappedText.size();
        for (int i = 0; i != size; i++) {
            result.append(wrappedText.get(i));
            if (i != size - 1) {
                result.append("\n");
            }
        }

        return result;
    }

    public boolean isEmpty() {
        return this.imagetext.isEmpty();
    }

    public int getLineWidth() {
        return this.lineWidth;
    }
}