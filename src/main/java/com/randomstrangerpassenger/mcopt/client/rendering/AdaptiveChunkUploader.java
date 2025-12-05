package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

/**
 * Provides adaptive chunk upload limiting based on frame counter.
 * 
 * <p>
 * The vanilla chunk upload system uses System.currentTimeMillis() which
 * can behave poorly at high FPS (sub-millisecond frame times). This
 * system uses frame counting for more consistent behavior.
 * </p>
 * 
 * <p>
 * <strong>Adaptive Logic:</strong>
 * </p>
 * <ul>
 * <li>Base limit per frame (configurable)</li>
 * <li>Adjusts based on render distance</li>
 * <li>Considers GPU frame time when available</li>
 * </ul>
 * 
 * <p>
 * <strong>Sodium Synergy:</strong> Complements Sodium's chunk rendering
 * with better upload scheduling.
 * </p>
 */
public class AdaptiveChunkUploader {

    // Cached config values
    private static boolean enabled = true;
    private static int baseLimit = 4;

    // Frame tracking
    private static long currentFrame = 0;
    private static int uploadsThisFrame = 0;
    private static long lastResetFrame = 0;

    // Adaptive adjustment
    private static float adaptiveMultiplier = 1.0f;
    private static long lastAdjustTime = 0;
    private static final long ADJUST_INTERVAL_MS = 1000; // Adjust every second

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_ADAPTIVE_CHUNK_UPLOAD.get();
        baseLimit = RenderingConfig.BASE_CHUNK_UPLOAD_LIMIT.get();
    }

    /**
     * Check if adaptive chunk upload is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Called at the start of each frame to reset counter.
     */
    public static void onFrameStart() {
        currentFrame++;
        if (currentFrame != lastResetFrame) {
            lastResetFrame = currentFrame;
            uploadsThisFrame = 0;
        }

        // Periodic adaptive adjustment
        long now = System.currentTimeMillis();
        if (now - lastAdjustTime > ADJUST_INTERVAL_MS) {
            lastAdjustTime = now;
            adjustMultiplier();
        }
    }

    /**
     * Check if a chunk upload can proceed this frame.
     * 
     * @return true if upload is allowed
     */
    public static boolean canUpload() {
        if (!enabled) {
            return true;
        }

        int currentLimit = getEffectiveLimit();
        return uploadsThisFrame < currentLimit;
    }

    /**
     * Record that a chunk upload occurred.
     */
    public static void recordUpload() {
        if (enabled) {
            uploadsThisFrame++;
        }
    }

    /**
     * Get the effective upload limit for this frame.
     */
    public static int getEffectiveLimit() {
        if (!enabled) {
            return Integer.MAX_VALUE;
        }

        int limit = Math.round(baseLimit * adaptiveMultiplier);

        // Adjust for render distance
        Minecraft mc = Minecraft.getInstance();
        int renderDistance = mc.options.getEffectiveRenderDistance();
        if (renderDistance > 16) {
            // Allow more uploads for higher render distances
            limit += (renderDistance - 16) / 4;
        }

        return Math.max(1, limit);
    }

    /**
     * Adjust the adaptive multiplier based on performance.
     */
    private static void adjustMultiplier() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        // Simple FPS-based adjustment
        float fps = mc.getFps();

        if (fps > 60) {
            // Good FPS: can afford more uploads
            adaptiveMultiplier = Math.min(2.0f, adaptiveMultiplier + 0.1f);
        } else if (fps < 30) {
            // Low FPS: reduce uploads
            adaptiveMultiplier = Math.max(0.5f, adaptiveMultiplier - 0.1f);
        }
        // 30-60 FPS: keep current multiplier
    }

    /**
     * Reset adaptive state (call on world load).
     */
    public static void reset() {
        currentFrame = 0;
        uploadsThisFrame = 0;
        lastResetFrame = 0;
        adaptiveMultiplier = 1.0f;
        lastAdjustTime = 0;
    }

    /**
     * Get current frame number.
     */
    public static long getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "AdaptiveChunk: " + uploadsThisFrame + "/" + getEffectiveLimit() +
                " (mult=" + String.format("%.2f", adaptiveMultiplier) + "), enabled=" + enabled;
    }
}
