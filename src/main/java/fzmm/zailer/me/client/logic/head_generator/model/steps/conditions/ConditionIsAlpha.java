package fzmm.zailer.me.client.logic.head_generator.model.steps.conditions;

import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.ModelPoint;

public class ConditionIsAlpha implements ICondition{
    private final ModelPoint pos;
    private final int minValue;
    private final int maxValue;

    public ConditionIsAlpha(JsonObject jsonObject) {
        this.pos = ModelPoint.parse(HeadResourcesLoader.get(jsonObject, "pos").getAsJsonObject());
        this.minValue =  HeadResourcesLoader.get(jsonObject, "min_alpha").getAsInt();
        this.maxValue =  HeadResourcesLoader.get(jsonObject, "max_alpha").getAsInt();

        if (this.minValue > this.maxValue) {
            throw new IllegalArgumentException("[ConditionIsAlpha] 'min_alpha' must be smaller than 'max_alpha'");
        }
    }
    @Override
    public boolean predicate(ModelData data) {
        int x = this.pos.getXWithOffset();
        int y = this.pos.getYWithOffset();

        int alpha = data.selectedTexture().getRGB(x, y) >> 24;
        return alpha >= this.minValue && alpha <= this.maxValue;
    }
}
