package fzmm.zailer.me.client.logic.head_generator.model.steps;

import fzmm.zailer.me.client.logic.head_generator.model.ModelData;

public interface IModelStep {

    void apply(ModelData data);


    /**
     * It validates that the step is correct; it runs after loading all resources
     * from HeadResourceLoader so that, in case one is required, it can be checked
     * for existence. If it's false, a message will be displayed in the chat, and
     * it will be removed from the loaded resources.
     */
    default boolean validate() throws IllegalArgumentException {
        return true;
    }
}
