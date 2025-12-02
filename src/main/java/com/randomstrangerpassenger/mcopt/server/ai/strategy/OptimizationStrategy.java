package com.randomstrangerpassenger.mcopt.server.ai.strategy;

import com.randomstrangerpassenger.mcopt.server.ai.modifiers.ModifierChain;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Strategy interface for entity-specific AI optimizations.
 * <p>
 * Each strategy defines how to optimize a specific type or category of mobs.
 * Strategies are applied based on entity type tags, allowing for flexible
 * configuration and mod compatibility.
 * <p>
 * Implementation guidelines:
 * - Keep strategies focused on a single mob category (farm animals, aquatic, etc.)
 * - Use configuration values to control which optimizations are applied
 * - Build ModifierChains that can be reused across multiple entities
 * - Document expected performance impact and behavior changes
 * <p>
 * Example:
 * <pre>{@code
 * public class FarmAnimalStrategy implements OptimizationStrategy {
 *     @Override
 *     public TagKey<EntityType<?>> getTargetTag() {
 *         return MCOPTEntityTypeTags.FARM_ANIMALS;
 *     }
 *
 *     @Override
 *     public ModifierChain buildModifiers() {
 *         ModifierChain chain = new ModifierChain();
 *         // Add goal removers based on config...
 *         return chain;
 *     }
 * }
 * }</pre>
 */
public interface OptimizationStrategy {

    /**
     * Get the entity type tag this strategy applies to.
     * <p>
     * Entities matching this tag will have this strategy's modifiers applied.
     * Tags can be defined in data packs, allowing users and mod authors to
     * customize which entities are affected.
     *
     * @return Entity type tag key
     */
    TagKey<EntityType<?>> getTargetTag();

    /**
     * Build the modifier chain for this optimization strategy.
     * <p>
     * The returned chain should contain all goal modifiers (removers, replacers, etc.)
     * that should be applied to entities matching the target tag.
     * <p>
     * This method is called during AI system initialization. The returned
     * chain will be cached and reused for all entities matching the tag.
     *
     * @return Modifier chain to apply
     */
    ModifierChain buildModifiers();

    /**
     * Check if this strategy should be applied based on configuration.
     * <p>
     * Default implementation returns true (always apply).
     * Override this to check feature toggles or config values.
     *
     * @return true if strategy should be applied
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Check if a specific mob should be optimized by this strategy.
     * <p>
     * This provides fine-grained control beyond tag matching.
     * Default implementation always returns true (rely on tag matching only).
     * <p>
     * Override this to add additional conditions, such as:
     * - Exclude specific entity types
     * - Check mob properties (health, age, custom data)
     * - Apply optimizations only in certain biomes or dimensions
     *
     * @param mob Mob to check
     * @return true if this mob should be optimized by this strategy
     */
    default boolean shouldOptimize(Mob mob) {
        return true;
    }

    /**
     * Get a human-readable name for this strategy.
     * Used for logging and debugging.
     *
     * @return Strategy name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get a description of what this strategy does.
     * Used for documentation and logging.
     *
     * @return Strategy description
     */
    default String getDescription() {
        return "No description provided";
    }

    /**
     * Get the priority of this strategy when multiple strategies apply.
     * <p>
     * Strategies with higher priority are applied first.
     * Default priority is 0.
     * <p>
     * Use this when you need to control the order of strategy application:
     * - Negative values for low-priority strategies
     * - Positive values for high-priority strategies
     * - 0 for normal priority
     *
     * @return Priority value (higher = applied first)
     */
    default int getPriority() {
        return 0;
    }
}
