package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
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

        byte[][] area = this.area.copyWithOffset(data.offsets().parameterList()).optimize();
        Graphics2D graphics = data.destinationGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        for (var rect : area) {
            graphics.clearRect(rect[0], rect[1], rect[2] - rect[0], rect[3] - rect[1]);
        }

        if (data.isInvertedLeftAndRight()) {
            this.area.swapLeftAndRight();
        }
    }

    public static ModelDeleteStep parse(JsonObject jsonObject) {
        ModelArea area = ModelArea.parse(HeadResourcesLoader.get(jsonObject, "area").getAsJsonObject());

        return new ModelDeleteStep(area);
    }
}
