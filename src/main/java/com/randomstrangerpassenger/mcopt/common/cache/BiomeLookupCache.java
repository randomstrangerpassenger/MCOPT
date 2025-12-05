package com.randomstrangerpassenger.mcopt.common.cache;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches biome lookup results per chunk.
 * 
 * <p>
 * getBiome() is called frequently for rendering, world gen, and entity AI.
 * This cache stores results per chunk, invalidating when chunks unload.
 * </p>
 * 
 * <p>
 * <strong>Cache Structure:</strong> ChunkPos -> Map&lt;BlockPos, Biome&gt;
 * </p>
 * 
 * <p>
 * <strong>Lithium Synergy:</strong> Lithium does not optimize biome lookups.
 * </p>
 */
@SuppressWarnings("null")
public class BiomeLookupCache {

    // Cached config value
    private static boolean enabled = true;

    // Cache storage: ChunkPos -> (BlockPos -> Biome)
    private static final Map<ChunkPos, Map<BlockPos, Holder<Biome>>> cache = new ConcurrentHashMap<>();

    // Statistics
    private static long hits = 0;
    private static long misses = 0;

    // Max cache size per chunk (to limit memory)
    private static final int MAX_ENTRIES_PER_CHUNK = 64;

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_BIOME_CACHING.get();
    }

    /**
     * Check if biome caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get cached biome at position.
     * 
     * @param pos Block position
     * @return Cached biome holder, or null if not cached
     */
    @Nullable
    public static Holder<Biome> getCached(@Nonnull BlockPos pos) {
        if (!enabled) {
            return null;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        Map<BlockPos, Holder<Biome>> chunkCache = cache.get(chunkPos);

        if (chunkCache == null) {
            misses++;
            return null;
        }

        Holder<Biome> biome = chunkCache.get(pos);
        if (biome != null) {
            hits++;
        } else {
            misses++;
        }

        return biome;
    }

    /**
     * Store biome in cache.
     * 
     * @param pos   Block position
     * @param biome Biome holder
     */
    public static void cache(@Nonnull BlockPos pos, @Nonnull Holder<Biome> biome) {
        if (!enabled) {
            return;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        Map<BlockPos, Holder<Biome>> chunkCache = cache.computeIfAbsent(
                chunkPos, k -> new ConcurrentHashMap<>());

        // Limit entries per chunk to prevent memory issues
        if (chunkCache.size() < MAX_ENTRIES_PER_CHUNK) {
            chunkCache.put(pos.immutable(), biome);
        }
    }

    /**
     * Invalidate cache for a chunk (call on chunk unload).
     * 
     * @param chunkPos Chunk position
     */
    public static void invalidateChunk(@Nonnull ChunkPos chunkPos) {
        cache.remove(chunkPos);
    }

    /**
     * Invalidate cache for a chunk by block position.
     * 
     * @param pos Any block position within the chunk
     */
    public static void invalidateChunkAt(@Nonnull BlockPos pos) {
        invalidateChunk(new ChunkPos(pos));
    }

    /**
     * Clear all cached biomes.
     */
    public static void invalidateAll() {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    /**
     * Get total number of cached entries across all chunks.
     */
    public static int getTotalEntries() {
        return cache.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * Get number of cached chunks.
     */
    public static int getCachedChunkCount() {
        return cache.size();
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
        return String.format("BiomeCache: %d chunks, %d entries, %.1f%% hit rate, enabled=%s",
                getCachedChunkCount(), getTotalEntries(), getHitRate() * 100, enabled);
    }
}
