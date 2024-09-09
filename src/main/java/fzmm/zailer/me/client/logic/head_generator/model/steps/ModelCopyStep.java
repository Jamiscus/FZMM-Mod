package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.ModelArea;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;

import java.awt.*;
import java.awt.geom.AffineTransform;

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

        ModelArea destination = this.destination.copyWithOffset(data.offsets().parameterList());
        if (this.addHatLayer) {
            this.apply(data, destination, false, false);
            this.apply(data, destination, true, true);
        } else if (this.overlapSourceHat) {
            this.apply(data, destination, false, this.destination.hatLayer());
            this.apply(data, destination, true, this.destination.hatLayer());
        } else {
            this.apply(data, destination, this.source.hatLayer(), this.destination.hatLayer());
        }

        if (data.isInvertedLeftAndRight()) {
            this.swapLeftAndRight();
        }
    }


    private void apply(ModelData data, ModelArea destination, boolean sourceHatLayer, boolean destinationHatLayer) {
        byte[][] sourceRect = this.source.optimize(sourceHatLayer);
        byte[][] destinationRect = destination.optimize(destinationHatLayer);

        if (sourceRect.length > destinationRect.length) {
            sourceRect = this.source.asArray(sourceHatLayer);
        } else if (destinationRect.length > sourceRect.length) {
            destinationRect = destination.asArray(destinationHatLayer);
        }

        Graphics2D graphics = data.destinationGraphics();

        AffineTransform transform = new AffineTransform();
        transform.setToRotation(
                Math.toRadians(this.degrees),
                (this.source.width() / 2f) + destinationRect[0][0],
                (this.source.height() / 2f) + destinationRect[0][1]
        );
        graphics.setTransform(transform);

        for (int i = 0; i !=  destinationRect.length; i++) {

            if (this.mirrorHorizontal) {
                byte aux = destinationRect[i][0];
                destinationRect[i][0] = destinationRect[i][2];
                destinationRect[i][2] = aux;
            }

            if (this.mirrorVertical) {
                byte aux = destinationRect[i][1];
                destinationRect[i][1] = destinationRect[i][3];
                destinationRect[i][3] = aux;
            }

            graphics.drawImage(data.selectedTexture(),
                    destinationRect[i][0], destinationRect[i][1], destinationRect[i][2], destinationRect[i][3],
                    sourceRect[i][0], sourceRect[i][1], sourceRect[i][2], sourceRect[i][3],
                    null
            );
        }

        graphics.setTransform(data.originalTransform());
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
        ModelArea source = ModelArea.parse(HeadResourcesLoader.get(jsonObject, "source").getAsJsonObject());
        ModelArea destination = null;

        if (jsonObject.has("destination"))
            destination = ModelArea.parse(jsonObject.get("destination").getAsJsonObject());

        if (destination == null || source.equals(destination)) {
            destination = source;
        }

        boolean addHatLayer = jsonObject.has("add_hat_layer") && jsonObject.get("add_hat_layer").getAsBoolean();
        boolean overlapSourceHat = jsonObject.has("overlap_source_hat") && jsonObject.get("overlap_source_hat").getAsBoolean();
        int degrees = jsonObject.has("degrees") ? jsonObject.get("degrees").getAsInt() : 0;
        boolean mirrorHorizontal = jsonObject.has("mirror_horizontal") && jsonObject.get("mirror_horizontal").getAsBoolean();
        boolean mirrorVertical = jsonObject.has("mirror_vertical") && jsonObject.get("mirror_vertical").getAsBoolean();

        return new ModelCopyStep(destination, source, addHatLayer, overlapSourceHat, degrees, mirrorHorizontal, mirrorVertical);
    }
}
