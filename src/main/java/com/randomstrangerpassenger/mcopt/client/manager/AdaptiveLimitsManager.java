package com.randomstrangerpassenger.mcopt.client.manager;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.client.Minecraft;

/**
 * Manages adaptive performance limits based on real-time game performance.
 * 
 * <p>
 * Monitors frame time (FPS) and adjusts optimization strictness dynamically.
 * When performance drops below target, optimization becomes stricter (reducing
 * visuals/load).
 * When performance is good, limits are relaxed to improve quality.
 * </p>
 */
public class AdaptiveLimitsManager {

    private static boolean enabled = true;
    private static int targetFrameTimeMs = 16; // Target ~60 FPS (1000/60 = 16.6)

    // Runtime metrics
    private static double avgFrameTime = 0;
    private static float stressFactor = 0.0f; // 0.0 = no stress, 1.0 = max stress

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_ADAPTIVE_LIMITS.get();
        targetFrameTimeMs = PerformanceConfig.TARGET_FRAME_TIME_MS.get();
    }

    /**
     * Called every frame to update performance metrics.
     */
    public static void onFrameStart() {
        if (!enabled)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null)
            return;

        // Get frame time in nanoseconds and convert to milliseconds
        // Minecraft doesn't expose a simple "last frame time" easily in public API
        // without access transformers sometimes,
        // but we can use our own delta tracker or access widely available metrics.
        // For simplicity, we might assume this is called with a delta, OR we measure
        // it.

        // Actually, let's just rely on a simple rolling average if we can't get engine
        // internals.
        // Or simpler: Use current FPS if available. (mc.fpsString is debug string)

        // Better approach: Rendering mixins usually have partialTicks or frame time
        // logic.
        // Let's assume this method is called from a Mixin that knows the frame delta.
    }

    /**
     * Get the current stress factor (0.0 to 1.0).
     * High stress means low FPS -> Stricter limits needed.
     */
    public static float getStressFactor() {
        return enabled ? stressFactor : 0.0f;
    }

    /**
     * Check if the system is under heavy load.
     */
    public static boolean isUnderLoad() {
        return enabled && stressFactor > 0.5f;
    }

    /**
     * Update metrics. Should be called once per frame from a central mixin (e.g.
     * Minecraft.runTick or similar).
     * 
     * @param deltaMs Time taken for the last frame in milliseconds
     */
    public static void updateMetrics(double deltaMs) {
        if (!enabled)
            return;

        // Simple exponential moving average
        avgFrameTime = (avgFrameTime * 0.9) + (deltaMs * 0.1);

        if (avgFrameTime > targetFrameTimeMs * 1.5) { // e.g. < 40 FPS
            stressFactor = Math.min(1.0f, stressFactor + 0.05f);
        } else if (avgFrameTime < targetFrameTimeMs) { // e.g. > 60 FPS
            stressFactor = Math.max(0.0f, stressFactor - 0.02f);
        }
    }
}
