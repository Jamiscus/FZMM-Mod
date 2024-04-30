package fzmm.zailer.me.client.logic.head_generator.model.parameters;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IParameterEntry<T> {

    String id();

    Optional<T> value();

    void setValue(@Nullable T value);


    /**
     * @return If it is required to request the parameter from the user, returns true.
     */
    boolean isRequested();
}
