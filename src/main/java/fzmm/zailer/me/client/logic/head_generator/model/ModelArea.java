package fzmm.zailer.me.client.logic.head_generator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.IModelParameter;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.OffsetParameter;
import fzmm.zailer.me.utils.SkinPart;

import java.util.List;
import java.util.Objects;

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

    public static ModelArea parse(JsonObject areaObject) {
        String offsetString = areaObject.get("offset").getAsString();
        if (offsetString.equalsIgnoreCase("ALL")) {
            return ALL_AREA;
        } else if (offsetString.equalsIgnoreCase("ALL_BODY")) {
            return ALL_BODY;
        }

        SkinPart offset = SkinPart.fromString(offsetString);
        boolean hat_layer = areaObject.get("hat_layer").getAsBoolean();
        int x = areaObject.get("x").getAsInt();
        int y = areaObject.get("y").getAsInt();
        int width = areaObject.get("width").getAsInt();
        int height = areaObject.get("height").getAsInt();

        ModelArea result = new ModelArea(offset, hat_layer, x, y, width, height);

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

    public ModelArea copyWithOffset(List<IModelParameter<OffsetParameter>> offsets) {
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

    @Override
    public int hashCode() {
        return Objects.hash(this.width, this.height);
    }
}
