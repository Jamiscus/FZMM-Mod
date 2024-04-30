package fzmm.zailer.me.client.logic.head_generator.model.steps.select;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;


public class ModelSelectColorStep implements IModelStep {

    private final String colorId;

    public ModelSelectColorStep(String colorId) {
        this.colorId = colorId;
    }

    @Override
    public void apply(ModelData data) {
        data.selectedColor(data.getColor(this.colorId));
    }

    public static ModelSelectColorStep parse(JsonObject jsonObject) {
        String colorId = jsonObject.get("color_id").getAsString();

        return new ModelSelectColorStep(colorId);
    }
}
