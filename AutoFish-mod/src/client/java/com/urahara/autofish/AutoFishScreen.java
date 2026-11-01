package com.urahara.autofish;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * Simple in-game settings screen for AutoFish. Open with the configured
 * "Open AutoFish Menu" key (default: K).
 */
public class AutoFishScreen extends Screen {

	private static final int WIDGET_WIDTH = 220;

	protected AutoFishScreen() {
		super(Text.literal("AutoFish Settings"));
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int y = this.height / 2 - 60;

		// Toggle on/off
		this.addDrawableChild(ButtonWidget.builder(
				toggleLabel(),
				button -> {
					AutoFishConfig.enabled = !AutoFishConfig.enabled;
					button.setMessage(toggleLabel());
				}
		).dimensions(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20).build());

		y += 28;

		// Bite sensitivity: threshold ranges roughly -0.005 (very sensitive)
		// to -0.06 (requires a very hard jerk). Slider value is 0..1.
		this.addDrawableChild(new SliderWidget(
				centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
				Text.literal("Bite sensitivity: " + sensitivityLabel(AutoFishConfig.biteThreshold)),
				normalize(AutoFishConfig.biteThreshold, -0.06, -0.005)
		) {
			@Override
			protected void updateMessage() {
				double threshold = denormalize(this.value, -0.06, -0.005);
				this.setMessage(Text.literal("Bite sensitivity: " + sensitivityLabel(threshold)));
			}

			@Override
			protected void applyValue() {
				AutoFishConfig.biteThreshold = denormalize(this.value, -0.06, -0.005);
			}
		});

		y += 28;

		// Settle ticks: 0..40
		this.addDrawableChild(new SliderWidget(
				centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
				Text.literal("Settle delay: " + AutoFishConfig.minSettleTicks + " ticks"),
				AutoFishConfig.minSettleTicks / 40.0
		) {
			@Override
			protected void updateMessage() {
				int ticks = (int) Math.round(this.value * 40);
				this.setMessage(Text.literal("Settle delay: " + ticks + " ticks"));
			}

			@Override
			protected void applyValue() {
				AutoFishConfig.minSettleTicks = (int) Math.round(this.value * 40);
			}
		});

		y += 28;

		// Recast delay: 0..60
		this.addDrawableChild(new SliderWidget(
				centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20,
				Text.literal("Recast delay: " + AutoFishConfig.recastDelayTicks + " ticks"),
				AutoFishConfig.recastDelayTicks / 60.0
		) {
			@Override
			protected void updateMessage() {
				int ticks = (int) Math.round(this.value * 60);
				this.setMessage(Text.literal("Recast delay: " + ticks + " ticks"));
			}

			@Override
			protected void applyValue() {
				AutoFishConfig.recastDelayTicks = (int) Math.round(this.value * 60);
			}
		});

		y += 32;

		// Done
		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("Done"),
				button -> this.close()
		).dimensions(centerX - WIDGET_WIDTH / 2, y, WIDGET_WIDTH, 20).build());
	}

	private static Text toggleLabel() {
		return Text.literal("AutoFish: " + (AutoFishConfig.enabled ? "ON" : "OFF"));
	}

	private static String sensitivityLabel(double threshold) {
		if (threshold > -0.015) return "High";
		if (threshold > -0.035) return "Medium";
		return "Low";
	}

	private static double normalize(double value, double min, double max) {
		return (value - min) / (max - min);
	}

	private static double denormalize(double t, double min, double max) {
		return min + t * (max - min);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 90, 0xFFFFFF);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
