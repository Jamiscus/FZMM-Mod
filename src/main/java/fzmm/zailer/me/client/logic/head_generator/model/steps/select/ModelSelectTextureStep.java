package fzmm.zailer.me.client.logic.head_generator.model.steps.select;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.parameters.IParameterEntry;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class ModelSelectTextureStep implements IModelStep {

    private final String textureId;

    public ModelSelectTextureStep(String textureId) {
        this.textureId = textureId;
    }

    @Override
    public void apply(ModelData data) {
        Optional<IParameterEntry<BufferedImage>> textureParamOptional = data.textures().getParameter(this.textureId);

        textureParamOptional.ifPresentOrElse(parameter -> {
            Optional<BufferedImage> textureOptional = parameter.value();
            if (!parameter.isRequested() && textureOptional.isEmpty()) {
                FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture '{}'", this.textureId);
                return;
            }

            data.selectedTexture(textureOptional.orElse(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)));
        }, () -> FzmmClient.LOGGER.warn("[ModelSelectTextureStep] Could not find texture parameter '{}'", this.textureId));
    }

    public static ModelSelectTextureStep parse(JsonObject jsonObject) {
        String textureId = jsonObject.get("texture_id").getAsString();

        return new ModelSelectTextureStep(textureId);
    }
}
