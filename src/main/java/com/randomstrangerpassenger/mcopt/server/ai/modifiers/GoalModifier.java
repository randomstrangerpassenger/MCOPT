package com.randomstrangerpassenger.mcopt.server.ai.modifiers;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for AI goal modification operations.
 *
 * Goal modifiers can inspect, modify, or remove goals from a mob's AI system.
 * Return null to remove a goal, return the original goal to keep it unchanged,
 * or return a new goal to replace it.
 */
@FunctionalInterface
public interface GoalModifier {

    /**
     * Process an AI goal and decide what to do with it.
     *
     * @param mob The mob that owns this goal
     * @param goal The goal to process
     * @return The goal to keep (original or replacement), or null to remove it
     */
    @Nullable
    Goal modify(Mob mob, Goal goal);

    /**
     * Chain this modifier with another, applying both in sequence.
     *
     * @param next The next modifier to apply
     * @return Combined modifier
     */
    default GoalModifier andThen(GoalModifier next) {
        return (mob, goal) -> {
            Goal result = this.modify(mob, goal);
            if (result == null) return null;  // Already removed
            return next.modify(mob, result);
        };
    }
}
