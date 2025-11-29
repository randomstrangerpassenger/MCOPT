package com.randomstrangerpassenger.mcopt.ai.modifiers;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A chain of goal modifiers that are applied in sequence.
 *
 * Modifiers are executed in order until one returns null (removes the goal)
 * or all modifiers have been applied.
 *
 * Includes performance optimization through call frequency tracking.
 */
public class ModifierChain implements GoalModifier {

    private final List<ModifierNode> modifiers = new ArrayList<>();
    private boolean enableOptimization = true;

    /**
     * Add a modifier to the end of the chain.
     *
     * @param modifier The modifier to add
     */
    public void add(GoalModifier modifier) {
        modifiers.add(new ModifierNode(modifier));
    }

    /**
     * Enable or disable automatic optimization (call frequency reordering).
     *
     * @param enable true to enable optimization
     */
    public void setOptimizationEnabled(boolean enable) {
        this.enableOptimization = enable;
    }

    @Nullable
    @Override
    public Goal modify(Mob mob, Goal goal) {
        Goal current = goal;

        for (int i = 0; i < modifiers.size(); i++) {
            ModifierNode node = modifiers.get(i);
            current = node.modifier.modify(mob, current);

            // Track call frequency for optimization
            if (enableOptimization) {
                node.callCount++;

                // Bubble up frequently-called modifiers for better performance
                if (i > 0 && node.callCount > modifiers.get(i - 1).callCount) {
                    modifiers.set(i, modifiers.get(i - 1));
                    modifiers.set(i - 1, node);
                }
            }

            // If goal was removed, stop processing
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * Get the number of modifiers in this chain.
     *
     * @return Modifier count
     */
    public int size() {
        return modifiers.size();
    }

    /**
     * Internal node that wraps a modifier with call tracking.
     */
    private static class ModifierNode {
        final GoalModifier modifier;
        long callCount = 0;

        ModifierNode(GoalModifier modifier) {
            this.modifier = modifier;
        }
    }
}
