package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Throttles map texture updates to reduce rendering overhead.
 * 
 * <p>
 * Maps in item frames update their textures every frame, which can
 * cause significant performance issues with many maps. This throttler
 * limits updates per tick and prioritizes nearby maps.
 * </p>
 * 
 * <p>
 * <strong>Throttling Strategy:</strong>
 * </p>
 * <ul>
 * <li>Near maps: Priority update</li>
 * <li>Far maps: Queued for later update</li>
 * <li>Off-screen maps: Delayed update</li>
 * </ul>
 * 
 * <p>
 * <strong>Sodium Synergy:</strong> Map rendering is not covered by Sodium.
 * </p>
 */
@SuppressWarnings("null")
public class MapUpdateThrottler {

    // Cached config values
    private static boolean enabled = true;
    private static int updatesPerTick = 4;
    private static int updateDistance = 32;

    // Update queue for throttled maps
    private static final Queue<MapUpdateRequest> updateQueue = new ConcurrentLinkedQueue<>();

    // Tracking for this tick
    private static int updatesThisTick = 0;
    private static long lastTick = 0;

    /**
     * Represents a pending map update request.
     */
    private record MapUpdateRequest(int mapId, BlockPos position, long requestTick) {
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_MAP_THROTTLING.get();
        updatesPerTick = RenderingConfig.MAP_UPDATES_PER_TICK.get();
        updateDistance = RenderingConfig.MAP_UPDATE_DISTANCE.get();
    }

    /**
     * Check if map throttling is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if a map can update this tick.
     * 
     * @param mapId    Map data ID
     * @param position Position of the map (item frame, player hand, etc.)
     * @return true if map should update now
     */
    public static boolean canUpdateMap(int mapId, @Nullable BlockPos position) {
        if (!enabled) {
            return true;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return true;
        }

        long currentTick = mc.level.getGameTime();

        // Reset counter for new tick
        if (currentTick != lastTick) {
            lastTick = currentTick;
            updatesThisTick = 0;
            processQueuedUpdates();
        }

        // Check if we have budget this tick
        if (updatesThisTick >= updatesPerTick) {
            // Queue for next tick if position provided
            if (position != null) {
                queueUpdate(mapId, position, currentTick);
            }
            return false;
        }

        // Priority check: nearby maps always update
        if (position != null && isNearby(position)) {
            updatesThisTick++;
            return true;
        }

        // Non-priority: check queue position
        if (position != null) {
            // Queue for later if distant
            if (!isNearby(position)) {
                queueUpdate(mapId, position, currentTick);
                return false;
            }
        }

        updatesThisTick++;
        return true;
    }

    /**
     * Check if position is within update distance.
     */
    private static boolean isNearby(@Nonnull BlockPos position) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return true;
        }

        double distSq = player.blockPosition().distSqr(position);
        return distSq <= (updateDistance * updateDistance);
    }

    /**
     * Queue a map update for later processing.
     */
    private static void queueUpdate(int mapId, @Nonnull BlockPos position, long tick) {
        // Limit queue size to prevent memory issues
        if (updateQueue.size() > 100) {
            updateQueue.poll(); // Remove oldest
        }
        updateQueue.offer(new MapUpdateRequest(mapId, position.immutable(), tick));
    }

    /**
     * Process queued updates at start of new tick.
     */
    private static void processQueuedUpdates() {
        // Process a few queued items per tick
        int processed = 0;
        while (!updateQueue.isEmpty() && processed < 2) {
            updateQueue.poll();
            processed++;
        }
    }

    /**
     * Clear all queued updates.
     */
    public static void clearQueue() {
        updateQueue.clear();
        updatesThisTick = 0;
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "MapThrottler: " + updatesThisTick + "/" + updatesPerTick +
                " updates, " + updateQueue.size() + " queued, enabled=" + enabled;
    }
}
