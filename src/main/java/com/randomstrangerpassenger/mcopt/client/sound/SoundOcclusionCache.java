package com.randomstrangerpassenger.mcopt.client.sound;

import com.randomstrangerpassenger.mcopt.config.SoundConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches sound occlusion calculation results to reduce raycast overhead.
 * 
 * <p>
 * Repeated sounds at the same position (water, lava, fire, etc.) reuse
 * cached occlusion values instead of recalculating each time.
 * </p>
 * 
 * <p>
 * <strong>Cache Invalidation:</strong>
 * </p>
 * <ul>
 * <li>Player moves more than 2 blocks</li>
 * <li>Cache entry older than 20 ticks</li>
 * <li>World unload</li>
 * </ul>
 * 
 * <p>
 * <strong>Immersion First:</strong> Occlusion results are cached, not skipped.
 * Sound behavior is identical to vanilla, just computed less frequently.
 * </p>
 */
@SuppressWarnings("null")
public class SoundOcclusionCache {

    // Cached config values
    private static boolean enabled = true;
    private static int cacheDurationTicks = 20;
    private static double invalidateDistance = 2.0;

    // Cache storage
    private static final Map<CacheKey, OcclusionResult> cache = new ConcurrentHashMap<>();

    // Player position tracking for invalidation
    private static double lastPlayerX = 0;
    private static double lastPlayerY = 0;
    private static double lastPlayerZ = 0;
    private static long lastCheckTick = 0;

    /**
     * Cache key combining sound position and type.
     */
    private record CacheKey(BlockPos pos, ResourceLocation soundId) {
    }

    /**
     * Cached occlusion result.
     */
    private static class OcclusionResult {
        final float occlusionFactor;
        final long cacheTick;

        OcclusionResult(float factor, long tick) {
            this.occlusionFactor = factor;
            this.cacheTick = tick;
        }

        boolean isExpired(long currentTick) {
            return (currentTick - cacheTick) > cacheDurationTicks;
        }
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = SoundConfig.ENABLE_OCCLUSION_CACHING.get();
        cacheDurationTicks = SoundConfig.OCCLUSION_CACHE_DURATION_TICKS.get();
        invalidateDistance = SoundConfig.OCCLUSION_CACHE_INVALIDATE_DISTANCE.get();
    }

    /**
     * Check if occlusion caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get cached occlusion factor for a sound, or null if not cached.
     * 
     * @param pos     Sound block position
     * @param soundId Sound resource location
     * @return Cached occlusion factor (0.0-1.0), or null if not cached/expired
     */
    @Nullable
    public static Float getCachedOcclusion(@Nonnull BlockPos pos, @Nonnull ResourceLocation soundId) {
        if (!enabled) {
            return null;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }

        long currentTick = mc.level.getGameTime();

        // Check if player moved too much (invalidates all cache)
        if (hasPlayerMovedTooMuch()) {
            clearCache();
            updatePlayerPosition();
            return null;
        }

        // Look up cached result
        CacheKey key = new CacheKey(pos, soundId);
        @Nullable
        OcclusionResult result = cache.get(key);

        if (result == null) {
            return null;
        }

        // Check if expired
        if (result.isExpired(currentTick)) {
            cache.remove(key);
            return null;
        }

        return result.occlusionFactor;
    }

    /**
     * Store occlusion result in cache.
     * 
     * @param pos             Sound block position
     * @param soundId         Sound resource location
     * @param occlusionFactor Calculated occlusion factor (0.0-1.0)
     */
    public static void cacheOcclusion(@Nonnull BlockPos pos, @Nonnull ResourceLocation soundId,
            float occlusionFactor) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        long currentTick = mc.level.getGameTime();
        CacheKey key = new CacheKey(pos, soundId);
        cache.put(key, new OcclusionResult(occlusionFactor, currentTick));
    }

    /**
     * Check if an occlusion calculation can be skipped for static sounds.
     * Static sounds (water, lava, fire) at the same position benefit most from
     * caching.
     */
    public static boolean isStaticSound(@Nonnull ResourceLocation soundId) {
        String path = soundId.getPath();
        return path.contains("water") ||
                path.contains("lava") ||
                path.contains("fire") ||
                path.contains("ambient") ||
                path.contains("portal");
    }

    /**
     * Check if player has moved beyond the invalidation threshold.
     */
    private static boolean hasPlayerMovedTooMuch() {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }

        @Nonnull
        Vec3 pos = player.position();
        double dx = pos.x - lastPlayerX;
        double dy = pos.y - lastPlayerY;
        double dz = pos.z - lastPlayerZ;
        double distSq = dx * dx + dy * dy + dz * dz;

        return distSq > (invalidateDistance * invalidateDistance);
    }

    /**
     * Update stored player position.
     */
    private static void updatePlayerPosition() {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        @Nonnull
        Vec3 pos = player.position();
        lastPlayerX = pos.x;
        lastPlayerY = pos.y;
        lastPlayerZ = pos.z;
    }

    /**
     * Clear all cached occlusion results.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Periodic cleanup of expired entries.
     * Call this every few ticks to prevent memory buildup.
     */
    public static void cleanupExpired() {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        long currentTick = mc.level.getGameTime();

        // Only cleanup every 40 ticks (2 seconds)
        if (currentTick - lastCheckTick < 40) {
            return;
        }
        lastCheckTick = currentTick;

        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTick));
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "SoundOcclusionCache: " + cache.size() + " entries, enabled=" + enabled;
    }
}
