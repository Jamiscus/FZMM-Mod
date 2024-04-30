package fzmm.zailer.me.client.logic.head_generator.model.steps.select;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.IParameterEntry;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;

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
            Optional<BufferedImage> textureOptional = parameter.value();
            if (!parameter.isRequested() && textureOptional.isEmpty()) {
                FzmmClient.LOGGER.warn("[ModelSelectDestinationStep] Could not find texture '{}'", this.textureId);
                return;
            }

            BufferedImage destination = textureOptional.orElse(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
            Graphics2D destinationGraphics = destination.createGraphics();

            data.destinationGraphics().dispose();
            data.destinationGraphics(destinationGraphics);
            data.destinationId(this.textureId);
        }, () -> FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture parameter '{}'", this.textureId));
    }

    public static ModelSelectDestinationStep parse(JsonObject jsonObject) {
        String textureId = jsonObject.get("texture_id").getAsString();

        return new ModelSelectDestinationStep(textureId);
    }
}
