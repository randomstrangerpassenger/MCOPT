package com.randomstrangerpassenger.mcopt.client.ui;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches sign text rendering to reduce font rendering overhead.
 * 
 * <p>
 * Signs contain text that rarely changes but is rendered every frame.
 * This cache stores rendering state and provides LOD based on distance.
 * </p>
 * 
 * <p>
 * <strong>LOD Tiers:</strong>
 * </p>
 * <ul>
 * <li>Near: Full text rendering</li>
 * <li>Mid: Simplified text (no formatting)</li>
 * <li>Far: Skip text entirely (just sign block)</li>
 * </ul>
 * 
 * <p>
 * <strong>Sodium Synergy:</strong> Text/UI rendering is not covered by Sodium.
 * </p>
 */
@SuppressWarnings("null")
public class SignTextCache {

    // Cached config values
    private static boolean enabled = true;
    private static int nearDistance = 16;
    private static int farDistance = 32;

    // Cache storage: BlockPos -> CachedSignData
    private static final Map<BlockPos, CachedSignData> cache = new ConcurrentHashMap<>();

    // Cleanup tracking
    private static long lastCleanupTick = 0;
    private static final int CLEANUP_INTERVAL = 200; // 10 seconds

    /**
     * LOD tier for sign rendering.
     */
    public enum SignLOD {
        /** Full text with formatting */
        FULL,
        /** Simplified text without formatting */
        SIMPLIFIED,
        /** Skip text rendering entirely */
        SKIP
    }

    /**
     * Cached data for a sign.
     */
    private static class CachedSignData {
        final int contentHash;
        final long cacheTick;

        CachedSignData(int hash, long tick, SignLOD lod) {
            this.contentHash = hash;
            this.cacheTick = tick;

        }

        boolean isContentChanged(int newHash) {
            return contentHash != newHash;
        }
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_TEXT_CACHING.get();
        nearDistance = RenderingConfig.SIGN_RENDER_DISTANCE_NEAR.get();
        farDistance = RenderingConfig.SIGN_RENDER_DISTANCE_FAR.get();
    }

    /**
     * Check if text caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the LOD tier for a sign at the given position.
     * 
     * @param signPos Sign block position
     * @return Appropriate LOD tier
     */
    @Nonnull
    public static SignLOD getSignLOD(@Nonnull BlockPos signPos) {
        if (!enabled) {
            return SignLOD.FULL;
        }

        double distanceSq = getDistanceToPlayerSq(signPos);
        int nearDistSq = nearDistance * nearDistance;
        int farDistSq = farDistance * farDistance;

        if (distanceSq <= nearDistSq) {
            return SignLOD.FULL;
        } else if (distanceSq <= farDistSq) {
            return SignLOD.SIMPLIFIED;
        } else {
            return SignLOD.SKIP;
        }
    }

    /**
     * Check if sign text should be rendered.
     * 
     * @param signPos Sign block position
     * @return true if text should be rendered
     */
    public static boolean shouldRenderText(@Nonnull BlockPos signPos) {
        if (!enabled) {
            return true;
        }

        return getSignLOD(signPos) != SignLOD.SKIP;
    }

    /**
     * Check if sign needs full text formatting.
     * 
     * @param signPos Sign block position
     * @return true if full formatting is needed
     */
    public static boolean needsFullFormatting(@Nonnull BlockPos signPos) {
        if (!enabled) {
            return true;
        }

        return getSignLOD(signPos) == SignLOD.FULL;
    }

    /**
     * Register a sign in the cache.
     * 
     * @param signPos     Sign position
     * @param contentHash Hash of sign content
     */
    public static void cacheSign(@Nonnull BlockPos signPos, int contentHash) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        long currentTick = mc.level.getGameTime();
        SignLOD lod = getSignLOD(signPos);
        cache.put(signPos.immutable(), new CachedSignData(contentHash, currentTick, lod));

        // Periodic cleanup
        if (currentTick - lastCleanupTick > CLEANUP_INTERVAL) {
            cleanup(currentTick);
        }
    }

    /**
     * Check if sign content has changed.
     * 
     * @param signPos     Sign position
     * @param contentHash Current content hash
     * @return true if content changed or not cached
     */
    public static boolean hasContentChanged(@Nonnull BlockPos signPos, int contentHash) {
        if (!enabled) {
            return true;
        }

        @Nullable
        CachedSignData data = cache.get(signPos);
        if (data == null) {
            return true;
        }

        return data.isContentChanged(contentHash);
    }

    /**
     * Calculate content hash for a sign.
     */
    public static int calculateContentHash(@Nonnull SignBlockEntity sign) {
        // Simple hash based on all text lines
        int hash = 17;
        for (int i = 0; i < 4; i++) {
            String frontText = sign.getFrontText().getMessage(i, false).getString();
            String backText = sign.getBackText().getMessage(i, false).getString();
            hash = 31 * hash + frontText.hashCode();
            hash = 31 * hash + backText.hashCode();
        }
        return hash;
    }

    /**
     * Get squared distance from position to local player.
     */
    private static double getDistanceToPlayerSq(@Nonnull BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return 0;
        }

        return player.blockPosition().distSqr(pos);
    }

    /**
     * Remove stale cache entries.
     */
    private static void cleanup(long currentTick) {
        lastCleanupTick = currentTick;

        // Remove entries older than 5 minutes
        long maxAge = 6000; // 5 minutes in ticks
        cache.entrySet().removeIf(entry -> (currentTick - entry.getValue().cacheTick) > maxAge);
    }

    /**
     * Clear cache for a specific sign.
     */
    public static void invalidate(@Nonnull BlockPos signPos) {
        cache.remove(signPos);
    }

    /**
     * Clear all cached sign data.
     */
    public static void clearAll() {
        cache.clear();
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "SignTextCache: " + cache.size() + " signs cached, enabled=" + enabled;
    }
}
