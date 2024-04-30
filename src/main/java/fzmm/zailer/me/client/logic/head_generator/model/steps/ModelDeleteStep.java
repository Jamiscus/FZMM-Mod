package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.model.ModelArea;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;

import java.awt.*;

public class ModelDeleteStep implements IModelStep {

    private final ModelArea area;

    public ModelDeleteStep(ModelArea area) {
        this.area = area;
    }

    @Override
    public void apply(ModelData data) {
        if (data.isInvertedLeftAndRight()) {
            this.area.swapLeftAndRight();
        }

        ModelArea area = this.area.copyWithOffset(data.offsets().parameterList());
        Graphics2D graphics = data.destinationGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(area.getXWithOffset(), area.getYWithOffset(), area.width(), area.height());

        if (data.isInvertedLeftAndRight()) {
            this.area.swapLeftAndRight();
        }
    }

    public static ModelDeleteStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(jsonObject.get("area").getAsJsonObject());

        return new ModelDeleteStep(area);
    }
}
