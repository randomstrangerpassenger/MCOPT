package com.randomstrangerpassenger.mcopt.common.cache;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Caches recipe lookup results to reduce O(n) search overhead.
 * 
 * <p>
 * Recipe lookups iterate through all recipes of a type each time.
 * This cache stores recent lookups using an LRU eviction strategy.
 * </p>
 * 
 * <p>
 * <strong>Cache Key:</strong> (RecipeType, input pattern hash)
 * </p>
 * 
 * <p>
 * <strong>Invalidation:</strong>
 * </p>
 * <ul>
 * <li>Resource pack reload</li>
 * <li>Server change</li>
 * <li>Manual invalidation</li>
 * </ul>
 * 
 * <p>
 * <strong>Lithium Synergy:</strong> Lithium does not optimize recipe lookups.
 * </p>
 */
@SuppressWarnings("null")
public class RecipeLookupCache {

    // Cached config values
    private static boolean enabled = true;
    private static int cacheSize = 256;

    // LRU cache implementation
    private static final Map<CacheKey, CachedRecipe> cache = Collections.synchronizedMap(
            new LinkedHashMap<CacheKey, CachedRecipe>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CacheKey, CachedRecipe> eldest) {
                    return size() > cacheSize;
                }
            });

    // Cache statistics
    private static long hits = 0;
    private static long misses = 0;

    /**
     * Cache key combining recipe type and input pattern.
     */
    private record CacheKey(RecipeType<?> type, int patternHash) {
    }

    /**
     * Cached recipe result.
     */
    private record CachedRecipe(@Nullable RecipeHolder<?> holder, long cacheTick) {
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_RECIPE_CACHING.get();
        int newSize = PerformanceConfig.RECIPE_CACHE_SIZE.get();
        if (newSize != cacheSize) {
            cacheSize = newSize;
            // Clear cache when size changes
            cache.clear();
        }
    }

    /**
     * Check if recipe caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get cached recipe for the given inputs.
     * 
     * @param type   Recipe type
     * @param inputs Input items (for hash calculation)
     * @return Cached recipe holder, or null if not cached
     */
    @Nullable
    public static RecipeHolder<?> getCachedRecipe(@Nonnull RecipeType<?> type, @Nonnull List<ItemStack> inputs) {
        if (!enabled) {
            return null;
        }

        int hash = calculateInputHash(inputs);
        CacheKey key = new CacheKey(type, hash);

        @Nullable
        CachedRecipe cached = cache.get(key);
        if (cached != null) {
            hits++;
            return cached.holder();
        }

        misses++;
        return null;
    }

    /**
     * Store a recipe lookup result in cache.
     * 
     * @param type   Recipe type
     * @param inputs Input items
     * @param result Recipe holder (can be null for "no match")
     */
    public static void cacheRecipe(@Nonnull RecipeType<?> type, @Nonnull List<ItemStack> inputs,
            @Nullable RecipeHolder<?> result) {
        if (!enabled) {
            return;
        }

        int hash = calculateInputHash(inputs);
        CacheKey key = new CacheKey(type, hash);
        cache.put(key, new CachedRecipe(result, System.currentTimeMillis()));
    }

    /**
     * Check if a recipe is cached.
     */
    public static boolean isCached(@Nonnull RecipeType<?> type, @Nonnull List<ItemStack> inputs) {
        if (!enabled) {
            return false;
        }

        int hash = calculateInputHash(inputs);
        CacheKey key = new CacheKey(type, hash);
        return cache.containsKey(key);
    }

    /**
     * Calculate hash for input item pattern.
     * Considers item type, count, and slot positions.
     */
    private static int calculateInputHash(@Nonnull List<ItemStack> inputs) {
        int hash = 17;
        int slot = 0;
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                hash = 31 * hash + slot;
                hash = 31 * hash + stack.getItem().hashCode();
                hash = 31 * hash + stack.getCount();
            }
            slot++;
        }
        return hash;
    }

    /**
     * Invalidate all cached recipes.
     * Call on resource pack reload or server change.
     */
    public static void invalidateAll() {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    /**
     * Invalidate recipes of a specific type.
     */
    public static void invalidateType(@Nonnull RecipeType<?> type) {
        cache.entrySet().removeIf(entry -> entry.getKey().type() == type);
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
        return String.format("RecipeCache: %d entries, %.1f%% hit rate, enabled=%s",
                cache.size(), getHitRate() * 100, enabled);
    }
}
