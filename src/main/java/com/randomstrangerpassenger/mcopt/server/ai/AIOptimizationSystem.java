package com.randomstrangerpassenger.mcopt.server.ai;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;

import com.randomstrangerpassenger.mcopt.server.ai.modifiers.ModifierChain;
import com.randomstrangerpassenger.mcopt.server.ai.modifiers.RemoveGoalModifier;
import com.randomstrangerpassenger.mcopt.server.ai.strategy.StrategyRegistry;
import com.randomstrangerpassenger.mcopt.mixin.accessor.MobAccessor;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import com.randomstrangerpassenger.mcopt.util.FeatureKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import net.neoforged.neoforge.common.Tags;

/**
 * Main system for AI optimization and goal management using Strategy pattern.
 * <p>
 * This system handles:
 * - Math caching initialization
 * - LookControl replacement with optimized version
 * - AI goal filtering and removal based on configuration
 * - Entity-specific optimizations via Strategy pattern
 * <p>
 * Architecture:
 * -
 * {@link com.randomstrangerpassenger.mcopt.server.ai.strategy.OptimizationStrategy}
 * defines optimization behavior
 * -
 * {@link com.randomstrangerpassenger.mcopt.server.ai.strategy.StrategyRegistry}
 * manages strategies
 * - This class orchestrates the optimization process
 * <p>
 * Benefits of Strategy pattern:
 * - New entity types can be added by creating new Strategy classes
 * - No code modification needed in this file
 * - Each strategy is self-contained and testable
 * - Strategies can be enabled/disabled independently
 * - Better separation of concerns
 * <p>
 * Inspired by AI-Improvements mod, but with independent implementation using
 * Mixin
 * instead of Access Transformers.
 */
public class AIOptimizationSystem {

    // Thread-safe initialization flag (volatile ensures visibility across threads)
    private static volatile boolean initialized = false;

    // Global modifiers that apply to all mobs
    private static final ModifierChain globalModifiers = new ModifierChain();

    /**
     * Initialize the AI optimization system.
     * Must be called during FMLCommonSetupEvent.
     * Thread-safe using double-checked locking pattern.
     */
    public static void init() {
        // Double-checked locking for thread-safe initialization
        if (!initialized) {
            synchronized (AIOptimizationSystem.class) {
                if (!initialized) {
                    MCOPT.LOGGER.info("Initializing AI Optimization System...");

                    // 1. Initialize Math Cache
                    MathCache.init();

                    // 2. Initialize Strategy Registry
                    StrategyRegistry.init();

                    // 3. Setup global modifiers
                    setupGlobalModifiers();

                    initialized = true;
                    MCOPT.LOGGER.info("AI Optimization System initialized successfully.");
                }
            }
        }
    }

    /**
     * Check if the system is initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Process a mob's AI goals to apply optimizations.
     * Called from MobAIMixin after registerGoals().
     *
     * @param mob The mob to optimize
     */
    public static void processMobGoals(Mob mob) {
        if (!initialized) {
            return;
        }

        // Check if AI optimizations are enabled globally
        if (!FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS)) {
            return;
        }

        // Skip if mob is blacklisted (e.g. boss mobs or specific config exclusions)
        if (shouldSkipOptimization(mob)) {
            return;
        }

        try {
            // 1. Replace LookControl with optimized version
            optimizeLookControl(mob);

            // 2. Apply global modifiers
            globalModifiers.apply(mob);

            // 3. Apply entity-specific strategies from registry
            ModifierChain strategyChain = StrategyRegistry.getModifierChain(mob);
            if (strategyChain != null) {
                strategyChain.apply(mob);
            }

        } catch (Exception e) {
            MCOPT.LOGGER.error("Failed to process AI goals for entity: {}", mob.getType().getDescriptionId(), e);
        }
    }

    /**
     * Setup global modifiers that apply to all mobs.
     */
    private static void setupGlobalModifiers() {
        // Example: Remove LookAtPlayerGoal if configured globally (very aggressive)
        if (PerformanceConfig.REMOVE_LOOK_AT_PLAYER.get()) {
            globalModifiers.add(RemoveGoalModifier.forClassHierarchy(LookAtPlayerGoal.class, null));
        }

        // Remove RandomLookAroundGoal if configured
        if (PerformanceConfig.REMOVE_RANDOM_LOOK_AROUND.get()) {
            globalModifiers.add(RemoveGoalModifier.forClassHierarchy(RandomLookAroundGoal.class, null));
        }
    }

    /**
     * Replace the mob's LookControl with our optimized version.
     */
    private static void optimizeLookControl(Mob mob) {
        if (!PerformanceConfig.ENABLE_OPTIMIZED_LOOK_CONTROL.get()) {
            return;
        }

        // Use accessor to swap the LookControl
        // This is safe because OptimizedLookControl extends LookControl
        ((MobAccessor) mob).setLookControl(new OptimizedLookControl(mob));
    }

    /**
     * Check if we should skip optimizing this mob.
     */
    private static boolean shouldSkipOptimization(Mob mob) {
        // Always skip bosses unless explicitly enabled
        if (mob instanceof EnderDragon || mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return true;
        }

        return false;
    }
}
