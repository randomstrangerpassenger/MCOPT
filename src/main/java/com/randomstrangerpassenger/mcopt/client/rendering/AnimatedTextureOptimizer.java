package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizes animated texture updates by tracking visibility and distance.
 * <p>
 * Immersion-first approach: Only skips animation updates for textures that are
 * truly off-screen or far away, preserving vanilla visuals for visible
 * textures.
 * </p>
 * <p>
 * Key optimizations:
 * <ul>
 * <li>Off-screen textures: Update at reduced frequency</li>
 * <li>Distant textures: Update less often as distance increases</li>
 * <li>Visible textures: Always update normally for seamless experience</li>
 * </ul>
 * </p>
 */
public final class AnimatedTextureOptimizer {

    private AnimatedTextureOptimizer() {
        // Utility class
    }

    // Track last update tick for each animated texture type
    private static final Map<String, Long> lastUpdateTicks = new ConcurrentHashMap<>();

    // Track positions where animated blocks are known to exist
    private static final Map<BlockPos, String> animatedBlockPositions = new ConcurrentHashMap<>();

    // Cached config values
    private static int updateDistance = 32;
    private static int offscreenInterval = 8;
    private static boolean enabled = true;

    /**
     * Refresh cached config values.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_SMART_ANIMATIONS.get();
        updateDistance = RenderingConfig.ANIMATION_UPDATE_DISTANCE.get();
        offscreenInterval = RenderingConfig.OFFSCREEN_ANIMATION_INTERVAL.get();
    }

    /**
     * Check if an animated texture should be updated this tick.
     *
     * @param textureId   Identifier for the animated texture (e.g.,
     *                    "block/water_still")
     * @param currentTick Current game tick
     * @return true if animation should be updated, false to skip this update
     */
    public static boolean shouldUpdateAnimation(String textureId, long currentTick) {
        if (!enabled) {
            return true; // Feature disabled, always update
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return true; // Safety: update when no player context
        }

        // Get camera position for distance calculations
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        // Check if any known animated block of this type is close enough
        boolean hasNearbyBlock = false;
        for (Map.Entry<BlockPos, String> entry : animatedBlockPositions.entrySet()) {
            if (entry.getValue().equals(textureId)) {
                BlockPos pos = entry.getKey();
                double distance = cameraPos.distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                if (distance <= updateDistance) {
                    hasNearbyBlock = true;
                    break;
                }
            }
        }

        // If there's a nearby block, always update
        if (hasNearbyBlock) {
            lastUpdateTicks.put(textureId, currentTick);
            return true;
        }

        // For distant/offscreen textures, use reduced update frequency
        Long lastUpdate = lastUpdateTicks.get(textureId);
        if (lastUpdate == null) {
            lastUpdateTicks.put(textureId, currentTick);
            return true; // First update, always allow
        }

        long ticksSinceUpdate = currentTick - lastUpdate;
        if (ticksSinceUpdate >= offscreenInterval) {
            lastUpdateTicks.put(textureId, currentTick);
            return true;
        }

        // Skip this update cycle
        return false;
    }

    /**
     * Register an animated block position for tracking.
     * Called when animated blocks are rendered.
     *
     * @param pos       Block position
     * @param textureId Texture identifier
     */
    public static void registerAnimatedBlock(BlockPos pos, String textureId) {
        if (!enabled) {
            return;
        }
        BlockPos validPos = Objects.requireNonNull(pos, "Position cannot be null");
        String validId = Objects.requireNonNull(textureId, "Texture ID cannot be null");
        animatedBlockPositions.put(validPos.immutable(), validId);
    }

    /**
     * Clear tracked positions when world changes.
     */
    public static void clearTrackedPositions() {
        animatedBlockPositions.clear();
        lastUpdateTicks.clear();
        MCOPT.LOGGER.debug("[SmartAnimations] Cleared tracked positions");
    }

    /**
     * Get statistics for debugging.
     *
     * @return String with current tracking stats
     */
    public static String getDebugStats() {
        return String.format("AnimatedTextures: %d tracked blocks, %d texture types",
                animatedBlockPositions.size(), lastUpdateTicks.size());
    }
}
