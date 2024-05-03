package fzmm.zailer.me.client.logic.head_generator.model.steps.conditions;

import fzmm.zailer.me.client.logic.head_generator.model.ModelData;

public interface ICondition {

    boolean predicate(ModelData data);
}
