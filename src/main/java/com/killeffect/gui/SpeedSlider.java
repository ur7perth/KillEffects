package com.killeffect.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/** A 0.25x - 3.0x speed slider for the currently selected effect. */
public class SpeedSlider extends SliderWidget {
    private static final double MIN_SPEED = 0.25;
    private static final double MAX_SPEED = 3.0;

    private final Consumer<Float> onChange;

    public SpeedSlider(int x, int y, int width, int height, float initialSpeed, Consumer<Float> onChange) {
        super(x, y, width, height, Text.empty(), normalize(initialSpeed));
        this.onChange = onChange;
        updateMessage();
    }

    private static double normalize(float speed) {
        double clamped = Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed));
        return (clamped - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
    }

    private float denormalize() {
        return (float) (MIN_SPEED + this.value * (MAX_SPEED - MIN_SPEED));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.literal(String.format("السرعة: %.2fx", denormalize())));
    }

    @Override
    protected void applyValue() {
        onChange.accept(denormalize());
    }
}
