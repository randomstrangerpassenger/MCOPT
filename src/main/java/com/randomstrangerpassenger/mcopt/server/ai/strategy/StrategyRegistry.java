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
    private static final Map<TagKey<EntityType<?>>, ModifierChain> modifierCache =
            new ConcurrentHashMap<>();

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
            register(new SquidStrategy());

            // Sort strategies by priority (highest first)
            sortStrategies();

            initialized = true;
            MCOPT.LOGGER.info("Strategy Registry initialized with {} strategies", strategies.size());

            // Log registered strategies for debugging
            if (MCOPT.LOGGER.isDebugEnabled()) {
                for (OptimizationStrategy strategy : strategies) {
                    MCOPT.LOGGER.debug("  - {} (priority: {}): {}",
                            strategy.getName(),
                            strategy.getPriority(),
                            strategy.getDescription());
                }
            }
        }
    }

    /**
     * Register a new optimization strategy.
     * <p>
     * Thread-safe. Can be called by other mods to register custom strategies.
     *
     * @param strategy Strategy to register
     */
    public static void register(OptimizationStrategy strategy) {
        synchronized (LOCK) {
            if (!strategies.contains(strategy)) {
                strategies.add(strategy);
                MCOPT.LOGGER.debug("Registered strategy: {}", strategy.getName());

                // Clear cache when new strategy is added
                modifierCache.clear();

                // Re-sort if already initialized
                if (initialized) {
                    sortStrategies();
                }
            } else {
                MCOPT.LOGGER.warn("Strategy already registered: {}", strategy.getName());
            }
        }
    }

    /**
     * Get the modifier chain for a given mob.
     * <p>
     * This method finds all applicable strategies based on entity tags,
     * builds and caches the modifier chains, and returns the combined chain.
     *
     * @param mob Mob to get modifiers for
     * @return Combined modifier chain (never null, may be empty)
     */
    public static ModifierChain getModifierChain(Mob mob) {
        ModifierChain combinedChain = new ModifierChain();
        EntityType<?> entityType = mob.getType();

        // Find and apply all matching strategies
        for (OptimizationStrategy strategy : strategies) {
            // Check if strategy is enabled
            if (!strategy.isEnabled()) {
                continue;
            }

            // Check if entity type matches strategy's target tag
            if (!entityType.is(strategy.getTargetTag())) {
                continue;
            }

            // Check if strategy wants to optimize this specific mob
            if (!strategy.shouldOptimize(mob)) {
                continue;
            }

            // Get or build modifier chain for this strategy
            ModifierChain strategyChain = getOrBuildModifierChain(strategy);

            // Add all modifiers from this strategy to the combined chain
            combinedChain.addAll(strategyChain);
        }

        return combinedChain;
    }

    /**
     * Get or build modifier chain for a strategy.
     * Uses caching to avoid rebuilding chains repeatedly.
     *
     * @param strategy Strategy to get chain for
     * @return Cached or newly built modifier chain
     */
    private static ModifierChain getOrBuildModifierChain(OptimizationStrategy strategy) {
        TagKey<EntityType<?>> tag = strategy.getTargetTag();

        // Check cache first
        ModifierChain cached = modifierCache.get(tag);
        if (cached != null) {
            return cached;
        }

        // Build new chain
        ModifierChain chain = strategy.buildModifiers();

        // Cache it
        modifierCache.put(tag, chain);

        MCOPT.LOGGER.debug("Built and cached modifier chain for strategy: {} (tag: {}, modifiers: {})",
                strategy.getName(), tag.location(), chain.size());

        return chain;
    }

    /**
     * Sort strategies by priority (highest first).
     */
    private static void sortStrategies() {
        strategies.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }

    /**
     * Clear all cached modifier chains.
     * Useful when configuration changes or strategies are re-registered.
     */
    public static void clearCache() {
        synchronized (LOCK) {
            modifierCache.clear();
            MCOPT.LOGGER.debug("Cleared modifier chain cache");
        }
    }

    /**
     * Get all registered strategies.
     * For debugging and testing purposes.
     *
     * @return Unmodifiable list of strategies
     */
    public static List<OptimizationStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    /**
     * Check if registry has been initialized.
     *
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Reset the registry (for testing purposes only).
     * WARNING: This is not thread-safe and should only be used in tests.
     */
    public static void reset() {
        synchronized (LOCK) {
            strategies.clear();
            modifierCache.clear();
            initialized = false;
            MCOPT.LOGGER.warn("Strategy Registry has been reset (test mode)");
        }
    }
}
