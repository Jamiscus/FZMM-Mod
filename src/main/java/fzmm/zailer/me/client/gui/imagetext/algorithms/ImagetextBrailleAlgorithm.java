package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.gui.components.BooleanButton;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLine;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.text.MutableText;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagetextBrailleAlgorithm implements IImagetextAlgorithm {
    private static final String[] BRAILLE_CHARACTERS;
    private static final String EDGE_THRESHOLD_ID = "edgeThreshold";
    private static final String EDGE_DISTANCE_ID = "edgeDistance";
    private static final String INVERT_ID = "invert";
    private static final byte BRAILLE_CHARACTER_WIDTH = 2;
    private static final byte BRAILLE_CHARACTER_HEIGHT = 4;
    private SliderWidget edgeThresholdSlider;
    private SliderWidget edgeDistanceSlider;
    private BooleanButton invertBooleanButton;

    @Override
    public String getId() {
        return "algorithm.braille";
    }

    @Override
    public List<MutableText> get(ImagetextLogic logic, ImagetextData data, int lineSplitInterval) {
        BufferedImage resultColors = ImageUtils.fastResizeImage(data.image(), data.width(), data.height(), data.smoothRescaling());
        BufferedImage upscaledImage = ImageUtils.fastResizeImage(data.image(), data.width() * BRAILLE_CHARACTER_WIDTH, data.height() * BRAILLE_CHARACTER_HEIGHT, data.smoothRescaling());
        byte[][] grayScaleImage = this.toGrayScale(upscaledImage);
        List<String> charactersList = this.getBrailleCharacters(grayScaleImage, data.width(), data.height());
        List<MutableText> linesList = new ArrayList<>();

        for (int y = 0; y != data.height(); y++) {
            ImagetextLine line = new ImagetextLine(charactersList.get(y), data.percentageOfSimilarityToCompress(), lineSplitInterval);
            for (int x = 0; x != data.width(); x++) {
                line.add(resultColors.getRGB(x, y));
            }

            linesList.addAll(line.getLineComponents());
        }

        return linesList;
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.edgeThresholdSlider = SliderRow.setup(rootComponent, EDGE_THRESHOLD_ID, 30, 1, 255, Integer.class, 0, 5, null);
        this.edgeDistanceSlider = SliderRow.setup(rootComponent, EDGE_DISTANCE_ID, 2, 1, 5, Integer.class, 0, 1, null);
        this.invertBooleanButton = BooleanRow.setup(rootComponent, INVERT_ID, false, null);
    }

    @Override
    public String getCharacters() {
        return BRAILLE_CHARACTERS[BRAILLE_CHARACTERS.length - 1];
    }

    public List<String> getBrailleCharacters(byte[][] grayScaleImage, int width, int height) {
        List<String> result = new ArrayList<>();
        int edgeThreshold = (int) this.edgeThresholdSlider.discreteValue();
        int edgeDistance = (int) this.edgeDistanceSlider.discreteValue();

        for (int y = 0; y != height; y++) {
            StringBuilder builder = new StringBuilder();
            int yOffset = y * BRAILLE_CHARACTER_HEIGHT;
            for (int x = 0; x != width; x++) {
                int xOffset = x * BRAILLE_CHARACTER_WIDTH;
                builder.append(this.getBrailleCharacter(grayScaleImage, xOffset, yOffset, edgeThreshold, edgeDistance));
            }
            result.add(builder.toString());
        }

        return result;
    }

    /**
     * @param grayScaleImage the image must have width multiply of {@link ImagetextBrailleAlgorithm#BRAILLE_CHARACTER_WIDTH}
     *                      and height multiply of {@link ImagetextBrailleAlgorithm#BRAILLE_CHARACTER_HEIGHT}
     */
    public String getBrailleCharacter(byte[][] grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        int index = BRAILLE_CHARACTERS.length - 1;
        int yOffset = y;

        index -= this.getBrailleCharacterIndex(0, grayScaleImage, x, yOffset, edgeThreshold, edgeDistance);
        index -= this.getBrailleCharacterIndex(1, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.getBrailleCharacterIndex(2, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.getBrailleCharacterIndex(3, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);

        yOffset = y;

        index -= this.getBrailleCharacterIndex(4, grayScaleImage, ++x, yOffset, edgeThreshold, edgeDistance);
        index -= this.getBrailleCharacterIndex(5, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.getBrailleCharacterIndex(6, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);
        index -= this.getBrailleCharacterIndex(7, grayScaleImage, x, ++yOffset, edgeThreshold, edgeDistance);

        if (this.invertBooleanButton.enabled()) {
            index = BRAILLE_CHARACTERS.length - 1 - index;
        }

        return BRAILLE_CHARACTERS[index];
    }

    private int getBrailleCharacterIndex(int index, byte[][] grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        if (this.isEdge(grayScaleImage, x, y, edgeThreshold, edgeDistance))
            return 1 << index;

        return 0;
    }

    // pain
    public boolean isEdge(byte[][] grayScaleImage, int x, int y, int edgeThreshold, int edgeDistance) {
        byte pixel = grayScaleImage[x][y];

        byte left = x < edgeDistance ? pixel : grayScaleImage[x - edgeDistance][y];
        byte right = x > grayScaleImage.length - edgeDistance - 1 ? pixel : grayScaleImage[x + edgeDistance][y];
        byte top = y < edgeDistance ? pixel : grayScaleImage[x][y - edgeDistance];
        byte bottom = y > grayScaleImage[0].length - edgeDistance - 1 ? pixel : grayScaleImage[x][y + edgeDistance];

        boolean isPixelLeftEdge = this.isEdgeThreshold(pixel, left, edgeThreshold);
        boolean isPixelRightEdge = this.isEdgeThreshold(pixel, right, edgeThreshold);
        boolean isPixelTopEdge = this.isEdgeThreshold(pixel, top, edgeThreshold);
        boolean isPixelBottomEdge = this.isEdgeThreshold(pixel, bottom, edgeThreshold);

        return isPixelLeftEdge || isPixelRightEdge || isPixelTopEdge || isPixelBottomEdge;
    }

    private boolean isEdgeThreshold(byte pixelByte, byte edgePixelByte, int edgeThreshold) {
        int pixel = Byte.toUnsignedInt(pixelByte);
        int edgePixel = Byte.toUnsignedInt(edgePixelByte);
        return (pixel > edgePixel + edgeThreshold) || (pixel < edgePixel - edgeThreshold);
    }

    /**
     * The use of BufferedImage is avoided here because when the grayscale color is obtained with BufferedImage#getRgb
     * the colors are combined, and since in braille the final imagetext is upscaled, then there are more pixels
     * and a little bit expensive
     */
    public byte[][] toGrayScale(BufferedImage image) {
        byte[][] result = new byte[image.getWidth()][image.getHeight()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgba = image.getRGB(x, y);
                int red = (rgba >> 16) & 0xFF;
                int green = (rgba >> 8) & 0xFF;
                int blue = rgba & 0xFF;
                int average = (red + green + blue) / 3;

                result[x][y] = (byte) average;
            }
        }

        return result;
    }

    @Override
    public IMementoObject createMemento() {
        return new BrailleAlgorithmMementoTab(
                (int) this.edgeThresholdSlider.discreteValue(),
                (int) this.edgeDistanceSlider.discreteValue(),
                this.invertBooleanButton.enabled()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        BrailleAlgorithmMementoTab memento = (BrailleAlgorithmMementoTab) mementoObject;
        this.edgeThresholdSlider.setFromDiscreteValue(memento.edgeThreshold);
        this.edgeDistanceSlider.setFromDiscreteValue(memento.edgeDistance);
        this.invertBooleanButton.enabled(memento.invert);
    }

    private record BrailleAlgorithmMementoTab(int edgeThreshold, int edgeDistance, boolean invert) implements IMementoObject {
    }

    static {
        BRAILLE_CHARACTERS = "⠀⠁⠂⠃⠄⠅⠆⠇⡀⡁⡂⡃⡄⡅⡆⡇⠈⠉⠊⠋⠌⠍⠎⠏⡈⡉⡊⡋⡌⡍⡎⡏⠐⠑⠒⠓⠔⠕⠖⠗⡐⡑⡒⡓⡔⡕⡖⡗⠘⠙⠚⠛⠜⠝⠞⠟⡘⡙⡚⡛⡜⡝⡞⡟⠠⠡⠢⠣⠤⠥⠦⠧⡠⡡⡢⡣⡤⡥⡦⡧⠨⠩⠪⠫⠬⠭⠮⠯⡨⡩⡪⡫⡬⡭⡮⡯⠰⠱⠲⠳⠴⠵⠶⠷⡰⡱⡲⡳⡴⡵⡶⡷⠸⠹⠺⠻⠼⠽⠾⠿⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⣀⣁⣂⣃⣄⣅⣆⣇⢈⢉⢊⢋⢌⢍⢎⢏⣈⣉⣊⣋⣌⣍⣎⣏⢐⢑⢒⢓⢔⢕⢖⢗⣐⣑⣒⣓⣔⣕⣖⣗⢘⢙⢚⢛⢜⢝⢞⢟⣘⣙⣚⣛⣜⣝⣞⣟⢠⢡⢢⢣⢤⢥⢦⢧⣠⣡⣢⣣⣤⣥⣦⣧⢨⢩⢪⢫⢬⢭⢮⢯⣨⣩⣪⣫⣬⣭⣮⣯⢰⢱⢲⢳⢴⢵⢶⢷⣰⣱⣲⣳⣴⣵⣶⣷⢸⢹⢺⢻⢼⢽⢾⢿⣸⣹⣺⣻⣼⣽⣾⣿"
                .split("");
    }
}
