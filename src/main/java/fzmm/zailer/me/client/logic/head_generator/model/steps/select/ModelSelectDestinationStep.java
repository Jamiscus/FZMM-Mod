package fzmm.zailer.me.client.logic.head_generator.model.steps.select;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.IParameterEntry;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;
import fzmm.zailer.me.utils.SkinPart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class ModelSelectDestinationStep implements IModelStep {

    private final String textureId;

    public ModelSelectDestinationStep(String textureId) {
        this.textureId = textureId;
    }

    @Override
    public void apply(ModelData data) {
        Optional<IParameterEntry<BufferedImage>> textureParamOptional = data.textures().getParameter(this.textureId);

        textureParamOptional.ifPresentOrElse(parameter -> {
            Optional<BufferedImage> destionationOptional = parameter.value();
            BufferedImage destination;

            if (destionationOptional.isEmpty()) {
                destination = new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                parameter.setValue(destination);
            } else if (data.destinationId().equals(this.textureId)) {
                FzmmClient.LOGGER.warn("[ModelSelectDestinationStep] Destination texture already set to '{}'", this.textureId);
                return;
            } else {
                destination = destionationOptional.get();
            }

            Graphics2D destinationGraphics = destination.createGraphics();
            if (!this.textureId.equals(HeadModelEntry.DESTINATION_ID)) {
                data.destinationGraphics().dispose();
            }

            data.destinationGraphics(destinationGraphics);
            data.destinationId(this.textureId);
        }, () -> FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture parameter '{}'", this.textureId));
    }

    public static ModelSelectDestinationStep parse(JsonObject jsonObject) {
        String textureId = HeadResourcesLoader.get(jsonObject, "texture_id").getAsString();

        return new ModelSelectDestinationStep(textureId);
    }
}
