package fzmm.zailer.me.client.logic.head_generator.model.parameters;

import fzmm.zailer.me.client.logic.head_generator.model.steps.IModelStep;
import io.wispforest.owo.ui.core.Color;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface INestedParameters {

    default boolean hasRequestedParameters() {
        return this.getNestedColorParameters().hasRequestedParameters() ||
                this.getNestedTextureParameters().hasRequestedParameters() ||
                this.getNestedOffsetParameters().hasRequestedParameters();
    }

    default <T> ParameterList<T> getNestedParameters(Supplier<ParameterList<T>> parametersSupplier,
                                                     Function<INestedParameters, ParameterList<T>> nestedParametersGetter) {
        ParameterList<T> result = new ParameterList<>();
        result.put(parametersSupplier.get());

        for (var step : this.getSteps()) {
            if (step instanceof INestedParameters nestedParameters) {
                ParameterList<T> stepParameters = nestedParametersGetter.apply(nestedParameters);
                result.put(stepParameters);
            }
        }

        return result;
    }

    default ParameterList<OffsetParameter> getNestedOffsetParameters() {
        return this.getNestedParameters(this::getOffsetParameters, INestedParameters::getNestedOffsetParameters);
    }

    default ParameterList<BufferedImage> getNestedTextureParameters() {
        return this.getNestedParameters(this::getTextureParameters, INestedParameters::getNestedTextureParameters);
    }

    default ParameterList<Color> getNestedColorParameters() {
        return this.getNestedParameters(this::getColorParameters, INestedParameters::getNestedColorParameters);
    }

    ParameterList<OffsetParameter> getOffsetParameters();

    ParameterList<BufferedImage> getTextureParameters();

    ParameterList<Color> getColorParameters();

    List<IModelStep> getSteps();
}
