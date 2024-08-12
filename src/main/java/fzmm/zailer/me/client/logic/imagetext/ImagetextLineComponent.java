package fzmm.zailer.me.client.logic.imagetext;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.awt.*;

public final class ImagetextLineComponent {
    private final int pixelColor;
    private short repetitions;

    public ImagetextLineComponent(int pixelColor) {
        this.pixelColor = pixelColor;
        this.repetitions = 1;
    }

    public boolean tryAdd(int color, double percentageOfSimilarity) {
        if (!this.isSimilar(color, percentageOfSimilarity))
            return false;

        this.repetitions++;
        return true;
    }

    public boolean isSimilar(int color, double percentageOfSimilarity) {
        if (this.pixelColor == color)
            return true;

        Color pixelColor = new Color(this.pixelColor, true);
        int a1 = pixelColor.getAlpha();
        int r1 = pixelColor.getRed();
        int g1 = pixelColor.getGreen();
        int b1 = pixelColor.getBlue();

        Color colorObj = new Color(color, true);
        int a2 = colorObj.getAlpha();
        int r2 = colorObj.getRed();
        int g2 = colorObj.getGreen();
        int b2 = colorObj.getBlue();

        int colorsDifference = this.getDifference(r1, r2) +
                this.getDifference(g1, g2) +
                this.getDifference(b1,  b2) +
                this.getDifference(a1, a2);

        int colorSum = a1 + r1 + g1 + b1;
        return (colorsDifference * 100.0 / colorSum) < percentageOfSimilarity;
    }

    private int getDifference(int n, int n2) {
        return Math.abs(n - n2);
    }

    public short getRepetitions() {
        return this.repetitions;
    }

    public Text getText(String[] charactersToUse, int lineIndex, boolean isDefaultText) {
        int alpha = (this.pixelColor >> 24) & 0xFF;
        return isDefaultText && alpha < 128 ? this.getEmptyText() : this.getText(charactersToUse, lineIndex);
    }

    public Text getEmptyText() {
        String spaceString = " ".repeat(this.repetitions);
        return Text.literal(spaceString + Formatting.BOLD + spaceString + Formatting.RESET);
    }

    private Text getText(String[] charactersToUse, int lineIndex) {
        StringBuilder textStrBuilder = new StringBuilder();
        int colorRGB = this.pixelColor & 0x00FFFFFF;

        for (int x = 0; x != this.repetitions; x++) {
            textStrBuilder.append(this.getCharacter(charactersToUse, lineIndex++));
        }

        return Text.literal(textStrBuilder.toString()).setStyle(Style.EMPTY.withColor(colorRGB));
    }

    private String getCharacter(String[] charactersToUse, int index) {
        return charactersToUse[index % charactersToUse.length];
    }
}