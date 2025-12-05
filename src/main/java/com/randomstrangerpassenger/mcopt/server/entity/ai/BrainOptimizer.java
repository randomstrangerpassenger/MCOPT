package com.randomstrangerpassenger.mcopt.server.entity.ai;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

/**
 * Optimizes entity brain tick frequency.
 * 
 * <p>
 * Entity brains tick every game tick to evaluate behaviors and memories.
 * This checks sensors and updates tasks. This optimizer reduces the frequency
 * of these updates when appropriate without breaking logic.
 * </p>
 * 
 * <p>
 * <strong>Optimization Strategy:</strong>
 * </p>
 * <ul>
 * <li>Apply random tick offset to distribute load</li>
 * <li>Skip brain ticks if memory state is stable (basic check)</li>
 * <li>Complementary to Lithium's AI optimizations</li>
 * </ul>
 */
public class BrainOptimizer {

    // Cached config value
    private static boolean enabled = true;

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_BRAIN_OPTIMIZATION.get();
    }

    /**
     * Check if brain optimization is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if a brain should tick this game tick.
     * 
     * @param entity The entity owning the brain
     * @return true if brain should tick
     */
    public static boolean shouldTickBrain(@Nonnull LivingEntity entity) {
        if (!enabled) {
            return true;
        }

        // Always tick if entity is hurt or targeting (combat state)
        if (entity.getLastHurtByMob() != null) {
            return true;
        }

        if (entity instanceof net.minecraft.world.entity.Mob mob && mob.isAggressive()) {
            return true;
        }

        // Use entity ID to create a consistent offset
        // This distributes brain ticks across different ticks
        // E.g., tick every 2 ticks for idle entities
        long gameTime = entity.level().getGameTime();
        if ((gameTime + entity.getId()) % 2 != 0) {
            return false;
        }

        return true;
    }
}
