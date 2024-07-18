package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.config.ui.component.ConfigSlider;

@SuppressWarnings("UnstableApiUsage")
public class SliderWidget extends ConfigSlider  {

    public SliderWidget() {
        super();
        this.snap(true);
    }

    public void setDiscreteValueWithoutCallback(double discreteValue) {
        this.value = (discreteValue - this.min) / (this.max - this.min);
        this.updateMessage();
    }

    @Override
    public void updateMessage() {
        super.updateMessage();
    }
}
