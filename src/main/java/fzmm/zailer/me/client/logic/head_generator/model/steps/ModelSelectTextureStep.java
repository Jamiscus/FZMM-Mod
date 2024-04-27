package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.ModelParameter;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ModelSelectTextureStep implements IModelStep {

    private final String textureId;

    public ModelSelectTextureStep(String textureId) {
        this.textureId = textureId;
    }

    @Override
    public void apply(ModelData data) {
        ModelParameter<BufferedImage> textureParameter = null;

        for (ModelParameter<BufferedImage> parameter : data.textures()) {
            if (parameter.id().equals(this.textureId)) {
                textureParameter = parameter;
                break;
            }
        }

        if (textureParameter == null) {
            FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture parameter '{}'", this.textureId);
        } else {
            Optional<BufferedImage> textureOptional = textureParameter.value();
            if (!textureParameter.isRequested() && textureOptional.isEmpty()) {
                //FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture '{}'", this.textureId);
                return;
            }

            data.selectedTexture(textureOptional.orElse(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)));
        }
    }

    public static ModelSelectTextureStep parse(JsonObject jsonObject) {
        String textureId = jsonObject.get("texture_id").getAsString();

        return new ModelSelectTextureStep(textureId);
    }
}
