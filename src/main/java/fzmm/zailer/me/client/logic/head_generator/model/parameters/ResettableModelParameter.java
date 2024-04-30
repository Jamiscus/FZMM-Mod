package fzmm.zailer.me.client.logic.head_generator.model.parameters;

import org.jetbrains.annotations.Nullable;

public class ResettableModelParameter<VALUE> extends ModelParameter<VALUE> {
    @Nullable
    private final String  defaultValue;

    /**
     * @param defaultValue The default value is a string because it is the value used in the text field
     */
    public ResettableModelParameter(String id, @Nullable VALUE value, @Nullable String defaultValue, boolean isRequested) {
        super(id, value, isRequested);
        this.defaultValue = defaultValue;
    }

    @Nullable
    public String getDefaultValue() {
        return this.defaultValue;
    }

}
