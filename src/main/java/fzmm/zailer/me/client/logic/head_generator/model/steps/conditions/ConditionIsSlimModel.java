package fzmm.zailer.me.client.logic.head_generator.model.steps.conditions;

import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.utils.ImageUtils;

public class ConditionIsSlimModel implements ICondition{
    @Override
    public boolean predicate(ModelData data) {
        return ImageUtils.isSlimSimpleCheck(data.selectedTexture());
    }
}
