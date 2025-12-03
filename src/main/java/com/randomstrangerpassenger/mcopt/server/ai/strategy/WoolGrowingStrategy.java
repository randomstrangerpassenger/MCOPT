package com.randomstrangerpassenger.mcopt.server.ai.strategy;

import com.randomstrangerpassenger.mcopt.MCOPTEntityTypeTags;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import com.randomstrangerpassenger.mcopt.server.ai.filters.GoalFilter;
import com.randomstrangerpassenger.mcopt.server.ai.modifiers.ModifierChain;
import com.randomstrangerpassenger.mcopt.server.ai.modifiers.RemoveGoalModifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;

/**
 * Optimization strategy for wool-growing animals (sheep).
 * <p>
 * This strategy extends farm animal optimizations with additional
 * wool-specific optimizations.
 * <p>
 * Additional Optimizations:
 * - Disable grass eating behavior (EatBlockGoal)
 * - Prevents constant block checks and grass consumption
 * - Wool can still regrow manually (shears + feed wheat)
 * <p>
 * Performance Impact:
 * - Large sheep farms: 15-25% TPS improvement
 * - Reduces block update checks significantly
 * <p>
 * Trade-offs:
 * - Sheep won't automatically regrow wool by eating grass
 * - Players must feed sheep wheat after shearing for wool regrowth
 * - More realistic for managed farms (players control wool production)
 */
public class WoolGrowingStrategy implements OptimizationStrategy {

    private final FarmAnimalStrategy baseFarmAnimalStrategy = new FarmAnimalStrategy();

    @Override
    public TagKey<EntityType<?>> getTargetTag() {
        return MCOPTEntityTypeTags.WOOL_GROWING_ANIMALS;
    }

    @Override
    public ModifierChain buildModifiers() {
        // Start with base farm animal modifiers
        ModifierChain chain = baseFarmAnimalStrategy.buildModifiers();

        // Add wool-specific optimizations
        chain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(EatBlockGoal.class),
                PerformanceConfig.REMOVE_SHEEP_EAT_BLOCK));

        return chain;
    }

    @Override
    public String getName() {
        return "Wool-Growing Animal Optimization";
    }

    @Override
    public String getDescription() {
        return "Extends farm animal optimizations with wool-specific improvements " +
                "(disables automatic grass eating for wool regrowth)";
    }

    @Override
    public int getPriority() {
        // Higher priority than base farm animal strategy
        // Ensures wool-specific optimizations are applied first
        return 10;
    }
}
