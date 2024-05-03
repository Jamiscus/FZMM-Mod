package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;

public class ModelToggleOffsetStep implements IModelStep {

    private final String offsetId;
    private final boolean enabled;

    public ModelToggleOffsetStep(String offsetId, boolean enabled) {
        this.offsetId = offsetId;
        this.enabled = enabled;
    }

    @Override
    public void apply(ModelData data) {
        for (var offset : data.offsets().parameterList()) {
            if (offset.id().equals(this.offsetId)) {
                offset.value().ifPresent(offsetParameter -> offsetParameter.setEnabled(this.enabled));
                return;
            }
        }
    }
    public static ModelToggleOffsetStep parse(JsonObject jsonObject) {
        String offsetId = HeadResourcesLoader.get(jsonObject, "offset_id").getAsString();
        boolean enabled = HeadResourcesLoader.get(jsonObject, "enabled").getAsBoolean();

        return new ModelToggleOffsetStep(offsetId, enabled);
    }
}
