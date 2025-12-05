package com.randomstrangerpassenger.mcopt.common.cache;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches tag membership lookup results for one tick.
 * 
 * <p>
 * Tag checks like {@code block.is(BlockTags.MINEABLE_WITH_PICKAXE)} are
 * called frequently. This cache stores results for the current tick.
 * </p>
 * 
 * <p>
 * <strong>Cache Duration:</strong> 1 tick (auto-invalidated each tick)
 * </p>
 * 
 * <p>
 * <strong>Common Use Cases:</strong>
 * </p>
 * <ul>
 * <li>Block tags: is_air, mineable/pickaxe, etc.</li>
 * <li>Item tags: tools, weapons, etc.</li>
 * <li>Entity tags, fluid tags, etc.</li>
 * </ul>
 * 
 * <p>
 * <strong>Lore-Friendly:</strong> Only caches results, logic unchanged.
 * </p>
 */
@SuppressWarnings("null")
public class TagLookupCache {

    // Cached config value
    private static boolean enabled = true;

    // Cache storage: (Holder + TagKey hash) -> boolean result
    private static final Map<CacheKey, Boolean> cache = new ConcurrentHashMap<>();

    // Tick tracking for automatic invalidation
    private static long lastTick = 0;

    // Statistics
    private static long hits = 0;
    private static long misses = 0;

    /**
     * Cache key combining holder and tag.
     */
    private record CacheKey(int holderHash, int tagHash) {
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_TAG_CACHING.get();
    }

    /**
     * Check if tag caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Called at the start of each tick to reset cache.
     */
    public static void onTickStart(long currentTick) {
        if (currentTick != lastTick) {
            lastTick = currentTick;
            cache.clear();
        }
    }

    /**
     * Get cached tag membership result.
     * 
     * @param holder The holder to check
     * @param tag    The tag to check membership in
     * @return Cached result, or null if not cached
     */
    public static Boolean getCached(@Nonnull Holder<?> holder, @Nonnull TagKey<?> tag) {
        if (!enabled) {
            return null;
        }

        CacheKey key = new CacheKey(System.identityHashCode(holder), tag.hashCode());
        Boolean result = cache.get(key);

        if (result != null) {
            hits++;
        } else {
            misses++;
        }

        return result;
    }

    /**
     * Store tag membership result in cache.
     * 
     * @param holder The holder that was checked
     * @param tag    The tag that was checked
     * @param result The membership result
     */
    public static void cache(@Nonnull Holder<?> holder, @Nonnull TagKey<?> tag, boolean result) {
        if (!enabled) {
            return;
        }

        CacheKey key = new CacheKey(System.identityHashCode(holder), tag.hashCode());
        cache.put(key, result);
    }

    /**
     * Check tag membership with caching.
     * Returns cached result if available, otherwise checks and caches.
     * 
     * @param holder The holder to check
     * @param tag    The tag to check membership in
     * @return true if holder is in tag
     */
    @SuppressWarnings("unchecked")
    public static boolean isIn(@Nonnull Holder<?> holder, @Nonnull TagKey<?> tag) {
        if (!enabled) {
            return ((Holder<Object>) holder).is((TagKey<Object>) tag);
        }

        Boolean cached = getCached(holder, tag);
        if (cached != null) {
            return cached;
        }

        boolean result = ((Holder<Object>) holder).is((TagKey<Object>) tag);
        cache(holder, tag, result);
        return result;
    }

    /**
     * Clear all cached results.
     */
    public static void invalidateAll() {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    /**
     * Get cache hit rate.
     */
    public static float getHitRate() {
        long total = hits + misses;
        return total > 0 ? (float) hits / total : 0;
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return String.format("TagCache: %d entries, %.1f%% hit rate, enabled=%s",
                cache.size(), getHitRate() * 100, enabled);
    }
}
