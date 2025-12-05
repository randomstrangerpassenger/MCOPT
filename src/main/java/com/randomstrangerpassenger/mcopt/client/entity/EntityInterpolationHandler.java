package com.randomstrangerpassenger.mcopt.client.entity;

import com.randomstrangerpassenger.mcopt.config.EntityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Handles entity interpolation throttling for distant entities.
 * 
 * <p>
 * This handler reduces tick rate for distant entities while maintaining
 * smooth visual movement through interpolation.
 * </p>
 * 
 * <p>
 * <strong>Immersion First Philosophy:</strong>
 * </p>
 * <ul>
 * <li>AI logic is NOT affected - only visual updates are throttled</li>
 * <li>Movement appears smooth due to client-side interpolation</li>
 * <li>Near entities always update at full rate</li>
 * </ul>
 */
@SuppressWarnings("null")
public class EntityInterpolationHandler {

    private static boolean enabled = true;
    private static int nearDistance = 32;
    private static int midDistance = 64;
    private static int nearTickInterval = 1;
    private static int midTickInterval = 2;
    private static int farTickInterval = 4;

    // Track entity positions for interpolation
    private static final Map<Integer, EntityMotionData> motionDataMap = new WeakHashMap<>();

    /**
     * Stores motion data for smooth interpolation.
     */
    private static class EntityMotionData {
        @Nonnull
        Vec3 lastPosition;
        @Nonnull
        Vec3 targetPosition;
        long lastUpdateTick;
        int tickInterval;

        EntityMotionData(@Nonnull Vec3 pos, long tick) {
            this.lastPosition = pos;
            this.targetPosition = pos;
            this.lastUpdateTick = tick;
            this.tickInterval = 1;
        }

        void update(@Nonnull Vec3 newPos, long tick, int interval) {
            this.lastPosition = this.targetPosition;
            this.targetPosition = newPos;
            this.lastUpdateTick = tick;
            this.tickInterval = interval;
        }
    }

    /**
     * Refresh configuration cache from config values.
     */
    public static void refreshConfigCache() {
        enabled = EntityConfig.ENABLE_ENTITY_INTERPOLATION.get();
        nearDistance = EntityConfig.NEAR_DISTANCE.get();
        midDistance = EntityConfig.MID_DISTANCE.get();
        nearTickInterval = EntityConfig.NEAR_TICK_INTERVAL.get();
        midTickInterval = EntityConfig.MID_TICK_INTERVAL.get();
        farTickInterval = EntityConfig.FAR_TICK_INTERVAL.get();
    }

    /**
     * Check if entity interpolation is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Determine if this entity's tick should be processed this frame.
     * 
     * @param entity      The entity to check
     * @param currentTick Current game tick
     * @return true if tick should be processed, false to skip
     */
    public static boolean shouldProcessTick(@Nonnull Entity entity, long currentTick) {
        if (!enabled) {
            return true;
        }

        // Always process player and their vehicle
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (entity == mc.player || isPlayerVehicle(entity))) {
            return true;
        }

        // Calculate distance to player
        double distanceSq = getDistanceToPlayerSq(entity);
        int tickInterval = getTickInterval(distanceSq);

        return (currentTick % tickInterval) == 0;
    }

    /**
     * Get the tick interval based on distance squared.
     */
    public static int getTickInterval(double distanceSq) {
        int nearDistSq = nearDistance * nearDistance;
        int midDistSq = midDistance * midDistance;

        if (distanceSq <= nearDistSq) {
            return nearTickInterval;
        } else if (distanceSq <= midDistSq) {
            return midTickInterval;
        } else {
            return farTickInterval;
        }
    }

    /**
     * Store entity position for later interpolation.
     * Called when entity tick is actually processed.
     */
    public static void recordPosition(@Nonnull Entity entity, long currentTick) {
        if (!enabled) {
            return;
        }

        int entityId = entity.getId();
        @Nonnull
        Vec3 pos = entity.position();
        double distanceSq = getDistanceToPlayerSq(entity);
        int interval = getTickInterval(distanceSq);

        @Nullable
        EntityMotionData data = motionDataMap.get(entityId);
        if (data == null) {
            motionDataMap.put(entityId, new EntityMotionData(pos, currentTick));
        } else {
            data.update(pos, currentTick, interval);
        }
    }

    /**
     * Get interpolated position for rendering.
     * 
     * @param entity       The entity
     * @param partialTicks Partial tick for smooth rendering
     * @return Interpolated position, or null if no interpolation needed
     */
    @Nonnull
    public static Vec3 getInterpolatedPosition(@Nonnull Entity entity, float partialTicks) {
        @Nonnull
        Vec3 currentPos = entity.position();
        if (!enabled) {
            return currentPos;
        }

        int entityId = entity.getId();
        @Nullable
        EntityMotionData data = motionDataMap.get(entityId);

        if (data == null || data.tickInterval <= 1) {
            return currentPos;
        }

        // Calculate interpolation progress
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        Level level = mc.level;
        if (level == null) {
            return currentPos;
        }

        long currentTick = level.getGameTime();
        long ticksSinceUpdate = currentTick - data.lastUpdateTick;
        float progress = (ticksSinceUpdate + partialTicks) / data.tickInterval;
        progress = Math.min(1.0f, Math.max(0.0f, progress));

        // Interpolate between last and target position
        return data.lastPosition.lerp(data.targetPosition, progress);
    }

    /**
     * Check if entity is the local player's vehicle.
     */
    private static boolean isPlayerVehicle(@Nonnull Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        @Nullable
        Entity vehicle = player.getVehicle();
        return vehicle != null && vehicle.getId() == entity.getId();
    }

    /**
     * Get squared distance from entity to local player.
     */
    private static double getDistanceToPlayerSq(@Nonnull Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return 0;
        }
        return entity.distanceToSqr(player);
    }

    /**
     * Clear interpolation data for an entity.
     */
    public static void clearEntity(int entityId) {
        motionDataMap.remove(entityId);
    }

    /**
     * Clear all interpolation data.
     */
    public static void clearAll() {
        motionDataMap.clear();
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "EntityInterpolation: " + motionDataMap.size() + " tracked entities, enabled=" + enabled;
    }
}
