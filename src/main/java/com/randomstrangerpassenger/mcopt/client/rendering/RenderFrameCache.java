package com.randomstrangerpassenger.mcopt.client.rendering;

import net.minecraft.world.phys.Vec3;

/**
 * Per-frame cache for render calculations to avoid redundant computation.
 * This cache is updated once per frame and shared across all render sections.
 *
 * Performance impact: Reduces redundant calculations from O(n sections) to O(1 per frame)
 */
public class RenderFrameCache {

    private static Vec3 cameraPosition = Vec3.ZERO;
    private static double renderDistance = 0.0;
    private static double verticalStretch = 1.0;
    private static double horizontalStretch = 1.0;

    // Pre-calculated values for ellipsoid culling
    private static double renderDistanceSquared = 0.0;

    // Frame tracking to detect stale cache
    private static long lastUpdateFrame = -1;
    private static long currentFrame = 0;

    /**
     * Update cache at the beginning of each frame.
     * Should be called from LevelRendererMixin.renderLevel()
     */
    public static void updateForFrame(
            Vec3 camPos,
            double renderDist,
            double vertStretch,
            double horizStretch) {

        currentFrame++;
        lastUpdateFrame = currentFrame;

        cameraPosition = camPos;
        renderDistance = renderDist;
        verticalStretch = vertStretch;
        horizontalStretch = horizStretch;

        renderDistanceSquared = renderDistance * renderDistance;
    }

    /**
     * Check if a chunk should be culled based on elliptical distance from camera.
     *
     * @param chunkCenterX Chunk center X coordinate
     * @param chunkCenterY Chunk center Y coordinate
     * @param chunkCenterZ Chunk center Z coordinate
     * @return true if chunk should be culled (outside ellipsoid)
     */
    public static boolean shouldCullChunk(double chunkCenterX, double chunkCenterY, double chunkCenterZ) {
        if (!isValid()) {
            return false; // Don't cull if cache is invalid
        }

        // Calculate distance components with stretch factors
        double dx = (cameraPosition.x - chunkCenterX) * horizontalStretch;
        double dy = (cameraPosition.y - chunkCenterY) * verticalStretch;
        double dz = (cameraPosition.z - chunkCenterZ) * horizontalStretch;

        // Calculate squared distance in ellipsoid space
        double distanceSquared = dx * dx + dy * dy + dz * dz;

        // If distance is greater than render distance, cull this chunk
        return distanceSquared > renderDistanceSquared;
    }

    /**
     * Check if the cache has been updated this frame.
     */
    public static boolean isValid() {
        return lastUpdateFrame == currentFrame;
    }

    /**
     * Get cached camera position.
     */
    public static Vec3 getCameraPosition() {
        return cameraPosition;
    }

    /**
     * Get cached render distance.
     */
    public static double getRenderDistance() {
        return renderDistance;
    }

    /**
     * Clear cached values when the client unloads a level to avoid stale world references.
     */
    public static void reset() {
        cameraPosition = Vec3.ZERO;
        renderDistance = 0.0;
        verticalStretch = 1.0;
        horizontalStretch = 1.0;
        renderDistanceSquared = 0.0;
        lastUpdateFrame = -1;
        currentFrame = 0;
    }
}
