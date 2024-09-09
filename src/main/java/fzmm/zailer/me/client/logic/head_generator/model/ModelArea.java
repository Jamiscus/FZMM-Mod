package fzmm.zailer.me.client.logic.head_generator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.IParameterEntry;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.OffsetParameter;
import fzmm.zailer.me.utils.SkinPart;

import java.util.List;

public class ModelArea extends ModelPoint {
    private static final ModelArea ALL_AREA = new ModelArea(SkinPart.HEAD, false, 0, 0, 64, 64);
    private static final ModelArea ALL_BODY = new ModelArea(SkinPart.HEAD, false, 0, 16, 64, 48);
    private static final ModelArea HEAD_LAYER = new ModelArea(SkinPart.HEAD, false, 0, 0, 32, 16);
    private static final ModelArea HAT_HEAD_LAYER = new ModelArea(SkinPart.HEAD, true, 0, 0, 32, 16);
    protected final byte width;
    protected final byte height;

    public ModelArea(SkinPart offset, boolean hatLayer, int x, int y, int width, int height) {
        super(offset, hatLayer, x, y);
        this.width = (byte) width;
        this.height = (byte) height;
    }

    public static ModelArea parse(JsonObject jsonObject) {
        String offsetString = HeadResourcesLoader.get(jsonObject, "offset").getAsString();
        if (offsetString.equalsIgnoreCase("ALL")) {
            return ALL_AREA;
        } else if (offsetString.equalsIgnoreCase("ALL_BODY")) {
            return ALL_BODY;
        }

        ModelPoint point = ModelPoint.parse(jsonObject);

        int width = HeadResourcesLoader.get(jsonObject, "width").getAsInt();
        int height = HeadResourcesLoader.get(jsonObject, "height").getAsInt();

        ModelArea result = new ModelArea(point.offset, point.hatLayer, point.x, point.y, width, height);

        if (result.equals(HEAD_LAYER)) {
            result = HEAD_LAYER;
        } else if (result.equals(HAT_HEAD_LAYER)) {
            result = HAT_HEAD_LAYER;
        }

        return result;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public ModelArea copyWithOffset(List<IParameterEntry<OffsetParameter>> offsets) {
        ModelArea copy = new ModelArea(this.offset, this.hatLayer, this.x, this.y, this.width, this.height);
        for (var offset : offsets) {
            offset.value().ifPresent(offsetParameter -> {
                if (!offsetParameter.enabled())
                    return;

                if (offsetParameter.isXAxis())
                    copy.x += offsetParameter.value();
                else
                    copy.y += offsetParameter.value();
            });
        }
        return copy;
    }

    public byte[][] optimize() {
        return this.optimize(this.hatLayer);
    }

    public byte[][] optimize(boolean hatLayer) {
        // Avoid using any unused areas
        if (this == ALL_AREA) {
            return hatLayer ? new byte[][]{SkinPart.ALL_USED_AREAS[2], SkinPart.ALL_USED_AREAS[3]} : new byte[][]{SkinPart.ALL_USED_AREAS[0], SkinPart.ALL_USED_AREAS[1]};
        }

        // Nothing to optimize
        if (this.x != 0 || this.y != 0 || this.width <= this.offset.emptyAreaSize() ||
                this.height <= this.offset.emptyAreaSize() || this.width > this.offset.width() || this.height > this.offset.height()) {
            return this.asArray(hatLayer);
        }

        // Avoid using unused areas
        if (this.offset.width() == this.width && this.offset.height() == this.height) {
            byte[][] usedAreas = this.offset.usedAreas();
            return hatLayer ? new byte[][]{usedAreas[2], usedAreas[3]} : new byte[][]{usedAreas[0], usedAreas[1]};
        }

        byte xOffset = (byte) this.xWithOffset(hatLayer);
        byte yOffset = (byte) this.yWithOffset(hatLayer);
        // Avoid using left unused area
        return SkinPart.getUsedAreas(xOffset, yOffset, this.width, this.height, this.offset.width(), this.offset.height(), this.offset.emptyAreaSize());
    }

    public byte[][] asArray(boolean hatLayer) {
        byte xOffset = (byte) this.xWithOffset(hatLayer);
        byte yOffset = (byte) this.yWithOffset(hatLayer);
        return new byte[][]{{
                xOffset, yOffset,
                (byte) (xOffset + this.width), (byte) (yOffset + this.height)
        }};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelArea modelArea)) return false;

        return this.width == modelArea.width &&
                this.height == modelArea.height &&
                this.y == modelArea.y &&
                this.x == modelArea.x &&
                this.offset == modelArea.offset
                && this.hatLayer == modelArea.hatLayer;
    }
}
