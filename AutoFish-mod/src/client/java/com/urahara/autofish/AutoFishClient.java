package com.urahara.autofish;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * AutoFish - casts, watches the bobber, and reels in automatically the
 * moment a fish bites, then recasts.
 *
 * Mechanism: right-clicking to cast/reel is simulated with
 * MinecraftClient#interactionManager.interactItem(...), which sends the
 * exact same client->server "use item" packet a real right-click sends.
 * There's no special "catch fish" packet in vanilla - cast and reel both
 * go through that one call, so that's all this mod ever sends.
 *
 * Bite detection watches the bobber's vertical position each tick for the
 * sharp downward jerk that happens right before a catch, rather than
 * reaching into obfuscated internal fields - keeps this resilient across
 * minor mapping/version bumps.
 */
public class AutoFishClient implements ClientModInitializer {

	public static final String MOD_ID = "autofish";

	private static KeyBinding toggleKey;
	private static KeyBinding menuKey;

	private FishingBobberEntity trackedBobber = null;
	private double lastBobberY = 0;
	private int settleTicks = 0;
	private int recastCooldown = 0;

	@Override
	public void onInitializeClient() {
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autofish.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_J,
				"category.autofish"
		));

		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autofish.menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				"category.autofish"
		));

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
		HudRenderCallback.EVENT.register(this::onHudRender);
	}

	private void onTick(MinecraftClient client) {
		while (toggleKey.wasPressed()) {
			AutoFishConfig.enabled = !AutoFishConfig.enabled;
			trackedBobber = null;
			recastCooldown = 0;
			if (client.player != null) {
				client.player.sendMessage(
						Text.literal("[AutoFish] " + (AutoFishConfig.enabled ? "Enabled" : "Disabled")), true);
			}
		}

		while (menuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new AutoFishScreen());
			}
		}

		if (!AutoFishConfig.enabled || client.player == null || client.world == null
				|| client.interactionManager == null) {
			return;
		}

		if (recastCooldown > 0) {
			recastCooldown--;
			return;
		}

		if (!isHoldingFishingRod(client)) {
			return;
		}

		if (trackedBobber == null || !trackedBobber.isAlive()) {
			trackedBobber = findOwnBobber(client);
			if (trackedBobber == null) {
				castLine(client);
			} else {
				settleTicks = 0;
				lastBobberY = trackedBobber.getY();
			}
			return;
		}

		double currentY = trackedBobber.getY();
		double deltaY = currentY - lastBobberY;
		lastBobberY = currentY;
		settleTicks++;

		if (settleTicks > AutoFishConfig.minSettleTicks && deltaY < AutoFishConfig.biteThreshold) {
			reelIn(client);
			trackedBobber = null;
			recastCooldown = AutoFishConfig.recastDelayTicks;
		}
	}

	private void onHudRender(net.minecraft.client.gui.DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) {
			return;
		}

		String label = "AutoFish: " + (AutoFishConfig.enabled ? "ON" : "OFF");
		int color = AutoFishConfig.enabled ? 0x55FF55 : 0xAAAAAA;
		if (AutoFishConfig.enabled && trackedBobber != null) {
			label += " (waiting...)";
		}

		context.drawTextWithShadow(client.textRenderer, Text.literal(label), 6, 6, color);
	}

	private boolean isHoldingFishingRod(MinecraftClient client) {
		ItemStack main = client.player.getMainHandStack();
		ItemStack off = client.player.getOffHandStack();
		return main.getItem() instanceof FishingRodItem || off.getItem() instanceof FishingRodItem;
	}

	private FishingBobberEntity findOwnBobber(MinecraftClient client) {
		for (Entity e : client.world.getEntities()) {
			if (e instanceof FishingBobberEntity bobber && bobber.getOwner() == client.player) {
				return bobber;
			}
		}
		return null;
	}

	private void castLine(MinecraftClient client) {
		Hand hand = client.player.getMainHandStack().getItem() instanceof FishingRodItem
				? Hand.MAIN_HAND : Hand.OFF_HAND;
		client.interactionManager.interactItem(client.player, hand);
		client.player.swingHand(hand);
		// give the server time to spawn the bobber; without this the next tick
		// sees no bobber yet and "casts" again, which reels the new cast straight back in
		recastCooldown = 20;
	}

	private void reelIn(MinecraftClient client) {
		Hand hand = client.player.getMainHandStack().getItem() instanceof FishingRodItem
				? Hand.MAIN_HAND : Hand.OFF_HAND;
		client.interactionManager.interactItem(client.player, hand);
		client.player.swingHand(hand);
	}
}
