package fzmm.zailer.me.client.logic.head_generator.model.parameters;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParameterList<VALUE> {
    private final List<IParameterEntry<VALUE>> parameters = new ArrayList<>();

    public ParameterList(List<? extends IParameterEntry<VALUE>> parameters) {
        this.parameters.addAll(parameters);
    }

    public ParameterList() {
    }

    public List<IParameterEntry<VALUE>> parameterList() {
        return this.parameters;
    }

    public boolean hasRequestedParameters() {
        return this.parameters.stream().anyMatch(IParameterEntry::isRequested);
    }

    public synchronized void put(ParameterList<VALUE> parameterList) {
        this.put(parameterList.parameterList());
    }

    public void put(List<? extends IParameterEntry<VALUE>> parameterList) {
        for (var parameter : parameterList) {
            this.put(parameter);
        }
    }

    public void put(IParameterEntry<VALUE> parameter) {
        this.remove(parameter.id());
        this.parameters.add(parameter);
    }

    public void update(List<? extends IParameterEntry<VALUE>> parameterList) {
        for (var parameter : parameterList) {
            this.update(parameter.id(), parameter.value().orElse(null));
        }
    }

    public void update(String key, @Nullable VALUE value) {
        for (var valueParameter : this.parameterList()) {
            if (valueParameter.id().equals(key)) {
                valueParameter.setValue(value);
                return;
            }
        }
    }

    public Optional<VALUE> get(String key) {
        Optional<IParameterEntry<VALUE>> parameter = this.getParameter(key);

        if (parameter.isEmpty()) {
            return Optional.empty();
        }

        return parameter.get().value();
    }

    public Optional<IParameterEntry<VALUE>> getParameter(String key) {
        for (var valueParameter : this.parameterList()) {
            if (valueParameter.id().equals(key)) {
                return Optional.of(valueParameter);
            }
        }
        return Optional.empty();
    }

    public void remove(String key) {
        this.parameters.removeIf(parameter -> parameter.id().equals(key));
    }

    public boolean isEmpty() {
        return this.parameters.isEmpty();
    }

    public ParameterList<VALUE> copy() {
        return new ParameterList<>(new ArrayList<>(this.parameters));
    }
}
