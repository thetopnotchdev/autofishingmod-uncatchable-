package com.urahara.autofish;

/**
 * Shared, mutable runtime settings. A single static instance so the tick
 * logic, HUD overlay, and settings screen all read/write the same state.
 */
public class AutoFishConfig {

	public static boolean enabled = false;

	// how sharp the bobber's downward dip must be to count as a bite
	public static double biteThreshold = -0.02;

	// ticks to ignore right after casting (lets the initial splash settle)
	public static int minSettleTicks = 15;

	// ticks to wait after reeling in before casting again
	public static int recastDelayTicks = 10;
}
