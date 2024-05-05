package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.model.ModelArea;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ModelCopyStep implements IModelStep {

    private final ModelArea destination;
    private final ModelArea source;
    private final boolean addHatLayer;
    private final boolean overlapSourceHat;
    private final int degrees;
    private final boolean mirrorHorizontal;
    private final boolean mirrorVertical;

    public ModelCopyStep(ModelArea destination, ModelArea source, boolean addHatLayer, boolean overlapSourceHat, int degrees, boolean mirrorHorizontal, boolean mirrorVertical) {
        this.destination = destination;
        this.source = source;
        this.addHatLayer = addHatLayer;
        this.overlapSourceHat = overlapSourceHat;
        this.degrees = degrees;
        this.mirrorHorizontal = mirrorHorizontal;
        this.mirrorVertical = mirrorVertical;
    }

    @Override
    public void apply(ModelData data) {
        if (data.isInvertedLeftAndRight()) {
            this.swapLeftAndRight();
        }
        Graphics2D graphics = data.destinationGraphics();
        BufferedImage selectedTexture = data.selectedTexture();

        ModelArea destination = this.destination.copyWithOffset(data.offsets().parameterList());
        if (this.addHatLayer) {
            this.apply(graphics, destination, selectedTexture, false, false);
            this.apply(graphics, destination, selectedTexture, true, true);
        } else if (this.overlapSourceHat) {
            this.apply(graphics, destination, selectedTexture, false, this.destination.hatLayer());
            this.apply(graphics, destination, selectedTexture, true, this.destination.hatLayer());
        } else {
            this.apply(graphics, destination, selectedTexture, this.source.hatLayer(), this.destination.hatLayer());
        }

        if (data.isInvertedLeftAndRight()) {
            this.swapLeftAndRight();
        }
    }


    private void apply(Graphics2D graphics, ModelArea destination, BufferedImage selectedTexture, boolean sourceHatLayer, boolean destinationHatLayer) {
        int destinationX = destination.getXWithOffset(destinationHatLayer);
        int destinationY = destination.getYWithOffset(destinationHatLayer);
        int sourceX = this.source.getXWithOffset(sourceHatLayer);
        int sourceY = this.source.getYWithOffset(sourceHatLayer);

        AffineTransform transform = graphics.getTransform();
        transform.setToRotation(
                Math.toRadians(this.degrees),
                (this.source.width() / 2f) + destinationX,
                (this.source.height() / 2f) + destinationY
        );
        graphics.setTransform(transform);

        int destinationX2 = destinationX + destination.width();
        if (this.mirrorHorizontal) {
            int aux = destinationX;
            destinationX = destinationX2;
            destinationX2 = aux;
        }

        int destinationY2 = destinationY + destination.height();
        if (this.mirrorVertical) {
            int aux = destinationY;
            destinationY = destinationY2;
            destinationY2 = aux;
        }


        graphics.drawImage(selectedTexture,
                destinationX,
                destinationY,
                destinationX2,
                destinationY2,
                sourceX,
                sourceY,
                sourceX + this.source.width(),
                sourceY + this.source.height(),
                null
        );
    }

    /**
     * @return copy with source and destination reversed
     */
    public ModelCopyStep reverseCopy() {
        return new ModelCopyStep(this.source,
                this.destination,
                this.addHatLayer,
                this.overlapSourceHat,
                this.degrees,
                this.mirrorHorizontal,
                this.mirrorVertical
        );
    }

    private void swapLeftAndRight() {
        this.destination.swapLeftAndRight();

        // as they can be the same instance, it is necessary to avoid swapping them both at the same time
        if (this.source != this.destination) {
            this.source.swapLeftAndRight();

        }
    }

    public static ModelCopyStep parse(JsonObject jsonObject) {
        ModelArea source = ModelArea.parse(jsonObject.get("source").getAsJsonObject());
        ModelArea destination = null;

        if (jsonObject.has("destination"))
            destination = ModelArea.parse(jsonObject.get("destination").getAsJsonObject());

        if (destination == null || source.equals(destination))
            destination = source;


        boolean addHatLayer = jsonObject.has("add_hat_layer") && jsonObject.get("add_hat_layer").getAsBoolean();
        boolean overlapSourceHat = jsonObject.has("overlap_source_hat") && jsonObject.get("overlap_source_hat").getAsBoolean();
        int degrees = jsonObject.has("degrees") ? jsonObject.get("degrees").getAsInt() : 0;
        boolean mirrorHorizontal = jsonObject.has("mirror_horizontal") && jsonObject.get("mirror_horizontal").getAsBoolean();
        boolean mirrorVertical = jsonObject.has("mirror_vertical") && jsonObject.get("mirror_vertical").getAsBoolean();

        return new ModelCopyStep(destination, source, addHatLayer, overlapSourceHat, degrees, mirrorHorizontal, mirrorVertical);
    }
}
