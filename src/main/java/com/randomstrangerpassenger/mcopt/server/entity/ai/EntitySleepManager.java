package com.randomstrangerpassenger.mcopt.server.entity.ai;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Manages "sleeping" state for distant entities.
 * 
 * <p>
 * Entities far from players and not interacting with anything don't need
 * full AI processing every tick. This manager puts them to "sleep", reducing
 * their AI tick rate significantly (e.g., 1 tick every 4-20 ticks).
 * </p>
 * 
 * <p>
 * <strong>Sleep Conditions:</strong>
 * </p>
 * <ul>
 * <li>Distance > sleepingDistance</li>
 * <li>No recent damage or interaction</li>
 * <li>No current target</li>
 * </ul>
 */
public class EntitySleepManager {

    // Cached config values
    private static boolean enabled = true;
    private static int sleepingDistance = 48;
    private static int sleepingInterval = 4;

    // Entity state tracking
    private static final Map<LivingEntity, Long> lastActiveTime = new WeakHashMap<>();

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_ENTITY_SLEEPING.get();
        sleepingDistance = PerformanceConfig.ENTITY_SLEEPING_DISTANCE.get();
        // Fixed interval for now to be safe, could make configurable
        sleepingInterval = 4;
    }

    /**
     * Check if entity sleeping is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Mark entity as active (wakes it up).
     * Call when damaged, interacting, or targeted.
     */
    public static void markActive(@Nonnull LivingEntity entity) {
        if (!enabled) {
            return;
        }
        lastActiveTime.put(entity, entity.level().getGameTime());
    }

    /**
     * Check if an entity should sleep (skip AI tick).
     * 
     * @param entity The entity checking sleep status
     * @return true if entity should skip AI tick
     */
    public static boolean shouldSleep(@Nonnull Mob entity) {
        if (!enabled) {
            return false;
        }

        // Never sleep if aggressive or has target
        if (entity.isAggressive() || entity.getTarget() != null) {
            markActive(entity);
            return false;
        }

        // Never sleep if recently active (within 5 seconds)
        long gameTime = entity.level().getGameTime();
        Long lastActive = lastActiveTime.get(entity);
        if (lastActive != null && (gameTime - lastActive) < 100) {
            return false;
        }

        // Check distance to nearest player
        @Nullable
        ServerPlayer nearestPlayer = (ServerPlayer) entity.level().getNearestPlayer(entity, sleepingDistance * 2);

        // If player is close, wake up and don't sleep
        if (nearestPlayer != null && entity.distanceToSqr(nearestPlayer) < (sleepingDistance * sleepingDistance)) {
            markActive(entity); // Keep active while player is near
            return false;
        }

        // If no player nearby, check if we should tick this frame
        // Ticks 1 time every 'sleepingInterval' ticks
        return (gameTime + entity.getId()) % sleepingInterval != 0;
    }
}
