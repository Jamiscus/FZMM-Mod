package fzmm.zailer.me.client.logic.head_generator.model.steps.conditions;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.ModelPoint;
import io.wispforest.owo.ui.core.Color;

public class ConditionIsPixel implements ICondition{
    private final ModelPoint pos;
    private final Color expectedColor;

    public ConditionIsPixel(JsonObject jsonObject) {
        this.pos = ModelPoint.parse(HeadResourcesLoader.get(jsonObject, "pos").getAsJsonObject());
        String colorHex = HeadResourcesLoader.get(jsonObject, "expected_color").getAsString();
        this.expectedColor = HeadResourcesLoader.parseColor(colorHex);
    }

    @Override
    public boolean predicate(ModelData data) {
        int x = this.pos.xWithOffset();
        int y = this.pos.yWithOffset();

        return data.selectedTexture().getRGB(x, y) == this.expectedColor.argb();
    }
}
