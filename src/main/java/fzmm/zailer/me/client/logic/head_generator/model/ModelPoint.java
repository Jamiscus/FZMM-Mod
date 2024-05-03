package fzmm.zailer.me.client.logic.head_generator.model;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.utils.SkinPart;

public class ModelPoint {
    public static ModelPoint ZERO = new ModelPoint(SkinPart.HEAD, false, 0, 0);
    protected SkinPart offset;
    protected final boolean hatLayer;
    protected byte x;
    protected byte y;

    public ModelPoint(SkinPart offset, boolean hatLayer, int x, int y) {
        this.offset = offset;
        this.hatLayer = hatLayer;
        this.x = (byte) x;
        this.y = (byte) y;
    }

    public static ModelPoint parse(JsonObject jsonObject) {
        String offsetString = HeadResourcesLoader.get(jsonObject, "offset").getAsString();
        SkinPart offset = SkinPart.fromString(offsetString);
        boolean hat_layer = HeadResourcesLoader.get(jsonObject, "hat_layer").getAsBoolean();
        int x = HeadResourcesLoader.get(jsonObject, "x").getAsInt();
        int y = HeadResourcesLoader.get(jsonObject, "y").getAsInt();
        return new ModelPoint(offset, hat_layer, x, y);
    }

    public int getXWithOffset() {
        return this.getXWithOffset(this.hatLayer);
    }

    public int getXWithOffset(boolean hatLayer) {
        return (hatLayer ? this.offset.hatX() : this.offset.x()) + this.x;
    }

    public int getYWithOffset() {
        return this.getYWithOffset(this.hatLayer);
    }

    public int getYWithOffset(boolean hatLayer) {
        return (hatLayer ? this.offset.hatY() : this.offset.y()) + this.y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public SkinPart offset() {
        return offset;
    }

    public boolean hatLayer() {
        return hatLayer;
    }

    public void swapLeftAndRight() {
        this.offset = this.offset.invert();
    }
}
