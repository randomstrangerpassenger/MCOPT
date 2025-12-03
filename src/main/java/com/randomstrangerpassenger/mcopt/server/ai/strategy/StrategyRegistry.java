package com.randomstrangerpassenger.mcopt.server.ai.strategy;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.server.ai.modifiers.ModifierChain;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for AI optimization strategies.
 * <p>
 * This registry manages all available optimization strategies and provides
 * efficient lookup based on entity types and tags.
 * <p>
 * Features:
 * - Thread-safe registration and lookup
 * - Priority-based strategy ordering
 * - Lazy modifier chain building and caching
 * - Support for multiple strategies per entity type
 * - Extensible - mods can register custom strategies
 * <p>
 * Usage:
 * 
 * <pre>{@code
 * // Register a strategy (typically during mod initialization)
 * StrategyRegistry.register(new FarmAnimalStrategy());
 *
 * // Get modifier chain for an entity
 * ModifierChain chain = StrategyRegistry.getModifierChain(mob);
 * }</pre>
 */
public class StrategyRegistry {

    /**
     * All registered strategies, sorted by priority (highest first).
     */
    private static final List<OptimizationStrategy> strategies = new ArrayList<>();

    /**
     * Cache of built modifier chains per tag.
     * Key: EntityType tag
     * Value: Pre-built and cached ModifierChain
     */
    private static final Map<TagKey<EntityType<?>>, ModifierChain> modifierCache = new ConcurrentHashMap<>();

    /**
     * Thread-safe initialization flag.
     */
    private static volatile boolean initialized = false;

    /**
     * Lock object for thread-safe registration.
     */
    private static final Object LOCK = new Object();

    /**
     * Initialize the strategy registry with built-in strategies.
     * Must be called during mod initialization.
     */
    public static void init() {
        if (initialized) {
            MCOPT.LOGGER.warn("StrategyRegistry already initialized");
            return;
        }

        synchronized (LOCK) {
            if (initialized) {
                return;
            }

            MCOPT.LOGGER.info("Initializing AI Optimization Strategy Registry...");

            // Register built-in strategies
            register(new FarmAnimalStrategy());
            register(new WoolGrowingStrategy());
            register(new FishStrategy());
            // register(new SquidStrategy());

            // Sort strategies by priority (highest first)
            sortStrategies();

            initialized = true;
            MCOPT.LOGGER.info("Strategy Registry initialized with {} strategies", strategies.size());

            // Log registered strategies for debugging
            for (OptimizationStrategy strategy : strategies) {
                MCOPT.LOGGER.debug("Registered Strategy: {} (Priority: {})", strategy.getName(),
                        strategy.getPriority());
            }
        }
    }

    /**
     * Register a new optimization strategy.
     *
     * @param strategy The strategy to register
     */
    public static void register(OptimizationStrategy strategy) {
        synchronized (LOCK) {
            strategies.add(strategy);
            // If already initialized, re-sort
            if (initialized) {
                sortStrategies();
                // Clear cache as new strategy might affect existing chains
                modifierCache.clear();
            }
        }
    }

    /**
     * Get the combined modifier chain for a specific mob.
     *
     * @param mob The mob to get modifiers for
     * @return The combined modifier chain, or null if no strategies apply
     */
    public static ModifierChain getModifierChain(Mob mob) {
        if (!initialized) {
            return null;
        }

        EntityType<?> type = mob.getType();
        ModifierChain combinedChain = new ModifierChain();
        boolean foundMatch = false;

        // Iterate through strategies to find applicable ones
        for (OptimizationStrategy strategy : strategies) {
            TagKey<EntityType<?>> targetTag = strategy.getTargetTag();

            // Check if mob type has this tag
            if (type.is(targetTag)) {
                // Get or build cached chain for this tag
                ModifierChain strategyChain = modifierCache.computeIfAbsent(targetTag,
                        k -> strategy.buildModifiers());

                // Merge into combined chain
                combinedChain.addAll(strategyChain);
                foundMatch = true;
            }
        }

        return foundMatch ? combinedChain : null;
    }

    /**
     * Sort strategies by priority (descending).
     */
    private static void sortStrategies() {
        strategies.sort(Comparator.comparingInt(OptimizationStrategy::getPriority).reversed());
    }

    /**
     * Clear the registry (useful for testing or reloading).
     */
    public static void clear() {
        synchronized (LOCK) {
            strategies.clear();
            modifierCache.clear();
            initialized = false;
        }
    }

    /**
     * Get all registered strategies.
     *
     * @return Unmodifiable list of registered strategies
     */
    public static List<OptimizationStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }
}
