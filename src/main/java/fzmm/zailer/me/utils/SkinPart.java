package fzmm.zailer.me.utils;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.utils.position.PosI;

import java.util.ArrayList;
import java.util.List;

public final class SkinPart {
    public static final int MAX_WIDTH = 64;
    public static final int MAX_HEIGHT = 64;

    // https://imgur.com/3LlJdua
    public static final SkinPart RIGHT_LEG;
    public static final SkinPart RIGHT_ARM;
    public static final SkinPart LEFT_LEG;
    public static final SkinPart LEFT_ARM;
    public static final SkinPart BODY;
    public static final SkinPart HEAD;
    public static final List<SkinPart> BODY_PARTS;
    public static final byte[][] EMPTY_AREAS;
    public static final byte[][] ALL_USED_AREAS;
    private final byte x;
    private final byte y;
    private final byte width;
    private final byte height;
    private final byte hatX;
    private final byte hatY;
    private final byte[][] usedAreas;
    private final byte[][] emptyAreas;
    private final byte emptyAreaSize;

    private SkinPart(int x, int y, int width, int height, int hatX, int hatY, int emptyAreaSize) {
        this.x = (byte) x;
        this.y = (byte) y;
        this.width = (byte) width;
        this.height = (byte) height;
        this.hatX = (byte) hatX;
        this.hatY = (byte) hatY;
        this.emptyAreaSize = (byte) emptyAreaSize;
        this.usedAreas = AreaGetter.USED_AREA.get(this.x, this.y, this.hatX, this.hatY, this.width, this.height, this.emptyAreaSize);
        this.emptyAreas = AreaGetter.EMPTY_AREA.get(this.x, this.y, this.hatX, this.hatY, this.width, this.height, this.emptyAreaSize);
    }

    /**
     * Since all parts of the skins follow the same pattern,
     * knowing the area of the skin and the size of the empty zone,
     * you simply disregard that area from the top corners
     * <br>
     * [x][x][x][x] -> [ ][x][x][ ]<br>
     * [x][x][x][x] -> [x][x][x][x]
     *
     * @return The array contains: x, y, x2, y2
     */
    public static byte[][] getUsedAreas(byte x, byte y, byte width, byte height, byte maxWidth, byte maxHeight, byte emptyAreaSize) {
        return AreaGetter.USED_AREA.getLayer(x, y, width, height, maxWidth, maxHeight, emptyAreaSize);
    }

    public PosI getNormalLayer() {
        return new PosI(this.x, this.y);
    }

    public PosI getHatLayer() {
        return new PosI(this.hatX, this.hatY);
    }

    public SkinPart invert() {
        if (this == RIGHT_ARM)
            return LEFT_ARM;

        if (this == LEFT_ARM)
            return RIGHT_ARM;

        if (this == RIGHT_LEG)
            return LEFT_LEG;

        if (this == LEFT_LEG)
            return RIGHT_LEG;

        return this;
    }

    public static SkinPart fromString(String value) {
        return switch (value.toUpperCase()) {
            case "RIGHT_LEG" -> RIGHT_LEG;
            case "RIGHT_ARM" -> RIGHT_ARM;
            case "LEFT_LEG" -> LEFT_LEG;
            case "LEFT_ARM" -> LEFT_ARM;
            case "BODY" -> BODY;
            default -> HEAD;
        };
    }

    public byte x() {
        return this.x;
    }

    public byte y() {
        return this.y;
    }

    public byte width() {
        return this.width;
    }

    public byte height() {
        return this.height;
    }

    public byte hatX() {
        return this.hatX;
    }

    public byte hatY() {
        return this.hatY;
    }

    /**
     * @return first two areas are from the first layer,
     * last two areas are from the hat layer.
     * The array contains: x, y, x2, y2
     */
    public byte[][] usedAreas() {
        return this.usedAreas;
    }

    /**
     * @return first two areas are from the first layer,
     * last two areas are from the hat layer.
     * The array contains: x, y, x2, y2
     */
    public byte[][] emptyAreas() {
        return this.emptyAreas;
    }

    public byte emptyAreaSize() {
        return this.emptyAreaSize;
    }

    private static byte[][] initAllUsedAreas() {
        List<SkinPart> partsList = new ArrayList<>(BODY_PARTS);
        partsList.add(HEAD);
        byte[][] allParts = new byte[partsList.size() * 2][4];

        for (int i = 0; i < BODY_PARTS.size(); i++) {
            byte[][] areas = BODY_PARTS.get(i).usedAreas();
            allParts[i] = areas[0];
            allParts[i + 1] = areas[1];
        }

        return allParts;
    }

    private static byte[][] initEmptyAreas() {
        List<SkinPart> allParts = new ArrayList<>(BODY_PARTS);
        allParts.add(HEAD);
        byte[][] result = new byte[(allParts.size() * 4) + 1][4];
        for (int i = 0; i < allParts.size(); i++) {
            byte[][] areas = allParts.get(i).emptyAreas();
            System.arraycopy(areas, 0, result, i * 4, areas.length);
        }
        result[result.length - 1] = new byte[]{56, 16, 64, 48};

        return result;
    }


    static {
        RIGHT_LEG = new SkinPart(16, 48, 16, 16, 0, 48, 4);
        RIGHT_ARM = new SkinPart(32, 48, 16, 16, 48, 48, 4);
        LEFT_LEG = new SkinPart(0, 16, 16, 16, 0, 32, 4);
        LEFT_ARM = new SkinPart(40, 16, 16, 16, 40, 32, 4);
        BODY = new SkinPart(16, 16, 24, 16, 16, 32, 4);
        HEAD = new SkinPart(0, 0, 32, 16, 32, 0, 8);
        BODY_PARTS = ImmutableList.of(RIGHT_LEG, RIGHT_ARM, LEFT_LEG, LEFT_ARM, BODY);

        EMPTY_AREAS = initEmptyAreas();
        ALL_USED_AREAS = initAllUsedAreas();
    }

    private interface AreaGetter {
        /**
         * @return The array contains: x, y, x2, y2
         */
        default byte[][] get(byte x, byte y, byte hatX, byte hatY, byte width, byte height, byte emptyAreaSize) {
            byte[][] result = new byte[4][4];

            byte[][] layer = this.getLayer(x, y, width, height, width, height, emptyAreaSize);
            result[0] = layer[0];
            result[1] = layer[1];
            layer = this.getLayer(hatX, hatY, width, height, width, height, emptyAreaSize);
            result[2] = layer[0];
            result[3] = layer[1];

            return result;
        }

        /**
         * @return The array contains: x, y, x2, y2
         */
        byte[][] getLayer(byte x, byte y, byte width, byte height, byte maxWidth, byte maxHeight, byte emptyAreaSize);

        AreaGetter USED_AREA = (x, y, width, height, maxWidth, maxHeight, emptyAreaSize) -> {
            // [ ][x][x][ ]
            // [x][x][x][x]
            byte[][] result = new byte[2][4];

            byte rightEmptyAreaSize = (byte) Math.clamp(width - maxWidth + emptyAreaSize, 0, emptyAreaSize);

            // [ ][x][x][ ]
            // [ ][ ][ ][ ]
            result[0] = new byte[]{(byte) (x + emptyAreaSize), y, (byte) (x + width - rightEmptyAreaSize), (byte) (y + emptyAreaSize)};
            // [ ][ ][ ][ ]
            // [x][x][x][x]
            result[1] = new byte[]{x, (byte) (y + emptyAreaSize), (byte) (x + width), (byte) (y + height)};

            return result;
        };

        AreaGetter EMPTY_AREA = (x, y, width, height, maxWidth, maxHeight, emptyAreaSize) -> {
            // [x][ ][ ][x]
            // [ ][ ][ ][ ]
            byte[][] result = new byte[2][4];

            // [x][ ][ ][ ]
            // [ ][ ][ ][ ]
            result[0] = new byte[]{x, y, (byte) (x + emptyAreaSize), (byte) (y + emptyAreaSize)};
            // [ ][ ][ ][x]
            // [ ][ ][ ][ ]
            result[1] = new byte[]{(byte) (x + width - emptyAreaSize), y, (byte) (x + width), (byte) (y + emptyAreaSize)};

            return result;
        };
    }

}
