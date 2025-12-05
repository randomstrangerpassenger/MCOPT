package com.randomstrangerpassenger.mcopt.server.entity.ai;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caches pathfinding results to avoid redundant expensive calculations.
 * 
 * <p>
 * Entities often request paths to the same location repeatedly or multiple
 * entities of the same type request similar paths. This cache helps reuse
 * recent valid paths.
 * </p>
 * 
 * <p>
 * <strong>Cache Key:</strong> (StartPos, TargetPos, EntityType)
 * <strong>Duration:</strong> Short (e.g., 5 seconds) or until structural change
 * </p>
 */
public class PathfindingCache {

    // Cached config values
    private static boolean enabled = true;
    private static int cacheSize = 128;

    // LRU Cache
    private static final Map<PathCacheKey, CachedPath> cache = Collections.synchronizedMap(
            new LinkedHashMap<PathCacheKey, CachedPath>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<PathCacheKey, CachedPath> eldest) {
                    return size() > cacheSize;
                }
            });

    /**
     * Cache key for pathfinding.
     */
    private record PathCacheKey(BlockPos start, BlockPos target, String entityType) {
    }

    /**
     * Cached path result.
     */
    private record CachedPath(Path path, long timestamp) {
        boolean isValid(long currentTime) {
            // Valid for 3 seconds
            return (currentTime - timestamp) < 3000;
        }
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_PATHFINDING_CACHE.get();
        cacheSize = PerformanceConfig.PATHFINDING_CACHE_SIZE.get();
    }

    /**
     * Check if pathfinding caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get a cached path if available and valid.
     */
    @Nullable
    public static Path getCachedPath(@Nonnull Mob mob, @Nonnull BlockPos target) {
        if (!enabled) {
            return null;
        }

        PathCacheKey key = new PathCacheKey(mob.blockPosition(), target, mob.getType().getDescriptionId());
        CachedPath cached = cache.get(key);

        if (cached != null && cached.isValid(System.currentTimeMillis())) {
            return cached.path();
        }

        return null;
    }

    /**
     * Cache a pathfinding result.
     */
    public static void cachePath(@Nonnull Mob mob, @Nonnull BlockPos target, @Nonnull Path path) {
        if (!enabled) {
            return;
        }

        PathCacheKey key = new PathCacheKey(mob.blockPosition(), target, mob.getType().getDescriptionId());
        cache.put(key, new CachedPath(path, System.currentTimeMillis()));
    }

    /**
     * Clear the cache.
     */
    public static void clear() {
        cache.clear();
    }
}
