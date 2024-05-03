package fzmm.zailer.me.client.logic.head_generator.model.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.ModelData;
import fzmm.zailer.me.client.logic.head_generator.model.steps.conditions.ConditionIsAlpha;
import fzmm.zailer.me.client.logic.head_generator.model.steps.conditions.ConditionIsPixel;
import fzmm.zailer.me.client.logic.head_generator.model.steps.conditions.ConditionIsSlimModel;
import fzmm.zailer.me.client.logic.head_generator.model.steps.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModelConditionStep implements IModelStep{
    private final ICondition condition;
    private final List<IModelStep> ifTrueList;
    private final List<IModelStep> ifFalseList;

    public ModelConditionStep(ICondition condition, @Nullable List<IModelStep> ifTrueList, @Nullable List<IModelStep> ifFalseList) {
        this.condition = condition;
        this.ifTrueList = ifTrueList;
        this.ifFalseList = ifFalseList;
    }

    @Override
    public void apply(ModelData data) {
        boolean conditionResult = this.condition.predicate(data);

        List<IModelStep> trueSteps = this.ifTrueList == null ? new ArrayList<>() : this.ifTrueList;
        List<IModelStep> falseSteps = this.ifFalseList == null ? new ArrayList<>() : this.ifFalseList;

        for (var step : conditionResult ? trueSteps : falseSteps) {
            step.apply(data);
        }
    }

    @Override
    public boolean validate() throws IllegalArgumentException {
        if (this.ifFalseList != null) {
            for (var step : this.ifFalseList) {
                if (!step.validate()) {
                    return false;
                }
            }
        }

        if (this.ifTrueList != null) {
            for (var step : this.ifTrueList) {
                if (!step.validate()) {
                    return false;
                }
            }
        }

        return true;
    }

    public static ModelConditionStep parse(JsonObject jsonObject) {
        String conditionId = HeadResourcesLoader.get(jsonObject, "condition").getAsString();
        JsonArray ifTrueJson = jsonObject.has("if_true") ? jsonObject.get("if_true").getAsJsonArray() : null;
        JsonArray ifFalseJson = jsonObject.has("if_false") ? jsonObject.get("if_false").getAsJsonArray() : null;
        JsonObject conditionArguments = jsonObject.has("arguments") ? jsonObject.get("arguments").getAsJsonObject() : null;

        if (ifTrueJson == null && ifFalseJson == null) {
            throw new IllegalArgumentException("[ModelConditionStep] Must contain either 'if_true' and/or 'if_false'");
        }

        List<IModelStep> ifTrueList = null;
        List<IModelStep> ifFalseList = null;

        if (ifTrueJson != null) {
            ifTrueList = new ArrayList<>();
            for (var element : ifTrueJson) {
                ifTrueList.add(HeadResourcesLoader.parseStep(element.getAsJsonObject()));
            }
        }

        if (ifFalseJson != null) {
            ifFalseList = new ArrayList<>();
            for (var element : ifFalseJson) {
                ifFalseList.add(HeadResourcesLoader.parseStep(element.getAsJsonObject()));
            }
        }


        ICondition condition = switch (conditionId) {
            case "is_slim_model" -> new ConditionIsSlimModel();
            case "is_pixel" -> new ConditionIsPixel(conditionArguments);
            case "is_alpha" -> new ConditionIsAlpha(conditionArguments);
            default -> throw new IllegalArgumentException(String.format("[ModelConditionStep] Unknown condition type: %s", conditionId));
        };

        return new ModelConditionStep(condition, ifTrueList, ifFalseList);
    }
}
