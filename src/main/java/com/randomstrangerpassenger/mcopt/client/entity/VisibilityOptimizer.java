package com.randomstrangerpassenger.mcopt.client.entity;

import com.randomstrangerpassenger.mcopt.config.EntityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Handles visibility-based logic optimization for entities.
 * 
 * <p>
 * This optimizer skips visual interaction logic (like LookAtPlayer)
 * for entities that are not visible to the player.
 * </p>
 * 
 * <p>
 * <strong>Immersion First Philosophy:</strong>
 * </p>
 * <ul>
 * <li>AI pathfinding and combat is NOT affected</li>
 * <li>Only visual goals like "look at player" are skipped</li>
 * <li>Visibility check is cached to reduce raycast overhead</li>
 * </ul>
 */
@SuppressWarnings("null")
public class VisibilityOptimizer {

    private static boolean enabled = true;
    private static int checkInterval = 5;
    private static int cacheDuration = 10;

    // Cache visibility results per entity
    private static final Map<Integer, VisibilityCache> visibilityCache = new WeakHashMap<>();

    /**
     * Stores cached visibility result.
     */
    private static class VisibilityCache {
        boolean isVisible;
        long lastCheckTick;

        VisibilityCache(boolean visible, long tick) {
            this.isVisible = visible;
            this.lastCheckTick = tick;
        }
    }

    /**
     * Refresh configuration cache from config values.
     */
    public static void refreshConfigCache() {
        enabled = EntityConfig.ENABLE_VISIBILITY_SKIP.get();
        checkInterval = EntityConfig.VISIBILITY_CHECK_INTERVAL.get();
        cacheDuration = EntityConfig.VISIBILITY_CACHE_DURATION.get();
    }

    /**
     * Check if visibility optimization is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if an entity is currently visible to the player.
     * Uses caching to reduce raycast overhead.
     * 
     * @param entity The entity to check
     * @return true if visible to player, false if hidden
     */
    public static boolean isEntityVisibleToPlayer(@Nonnull LivingEntity entity) {
        if (!enabled) {
            return true; // Default to visible when disabled
        }

        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        @Nullable
        Level level = mc.level;
        if (player == null || level == null) {
            return true;
        }

        // Check cache
        int entityId = entity.getId();
        long currentTick = level.getGameTime();
        @Nullable
        VisibilityCache cached = visibilityCache.get(entityId);

        if (cached != null && (currentTick - cached.lastCheckTick) < cacheDuration) {
            return cached.isVisible;
        }

        // Should we check this tick?
        if (cached != null && (currentTick % checkInterval) != 0) {
            return cached.isVisible;
        }

        // Perform visibility check
        boolean visible = performVisibilityCheck(entity);
        visibilityCache.put(entityId, new VisibilityCache(visible, currentTick));

        return visible;
    }

    /**
     * Check if a specific visual goal should be executed.
     * Returns false if the entity is not visible and visual logic can be skipped.
     * 
     * @param entity The entity with the goal
     * @return true if the goal should execute, false to skip
     */
    public static boolean shouldExecuteVisualGoal(@Nonnull LivingEntity entity) {
        if (!enabled) {
            return true;
        }

        // Always execute for nearby entities (performance vs accuracy trade-off)
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return true;
        }

        double distanceSq = entity.distanceToSqr(player);
        if (distanceSq < 256) { // Within 16 blocks, always execute
            return true;
        }

        return isEntityVisibleToPlayer(entity);
    }

    /**
     * Perform the actual visibility check using raycasting.
     */
    private static boolean performVisibilityCheck(@Nonnull LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        @Nullable
        Level level = mc.level;
        if (player == null || level == null) {
            return true;
        }

        // Check if entity is within player's view frustum
        if (!isInViewFrustum(entity)) {
            return false;
        }

        // Check if there's a clear line of sight
        @Nonnull
        Vec3 playerEye = player.getEyePosition();
        @Nonnull
        Vec3 entityEye = entity.getEyePosition();

        // Use block occlusion check
        return level.clip(new net.minecraft.world.level.ClipContext(
                playerEye,
                entityEye,
                net.minecraft.world.level.ClipContext.Block.VISUAL,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player)).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    /**
     * Check if entity is within the player's view frustum.
     */
    private static boolean isInViewFrustum(@Nonnull Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return true;
        }

        // Get direction from player to entity
        @Nonnull
        Vec3 entityPos = entity.position();
        @Nonnull
        Vec3 playerEye = player.getEyePosition();
        @Nonnull
        Vec3 toEntity = entityPos.subtract(playerEye).normalize();
        @Nonnull
        Vec3 lookDir = player.getViewVector(1.0f);

        // Check if entity is roughly in front (within ~100 degree FOV)
        double dot = toEntity.dot(lookDir);
        return dot > -0.2; // Roughly 100 degree cone
    }

    /**
     * Clear visibility cache for an entity.
     */
    public static void clearEntity(int entityId) {
        visibilityCache.remove(entityId);
    }

    /**
     * Clear all visibility cache.
     */
    public static void clearAll() {
        visibilityCache.clear();
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        long visibleCount = visibilityCache.values().stream()
                .filter(c -> c.isVisible).count();
        return "VisibilityOptimizer: " + visibilityCache.size() + " cached (" + visibleCount + " visible), enabled="
                + enabled;
    }
}
