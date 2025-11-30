package com.randomstrangerpassenger.mcopt.server.ai.modifiers;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A chain of goal modifiers that are applied in sequence.
 * <p>
 * Modifiers are executed in order until one returns null (removes the goal)
 * or all modifiers have been applied.
 * <p>
 * <b>Simplified Design:</b>
 * Previous implementation used runtime "bubble up" optimization based on call frequency.
 * This has been removed because:
 * <ul>
 *   <li>Typically only 2-3 modifiers per chain - minimal benefit from reordering</li>
 *   <li>ArrayList mutations during iteration caused potential thread-safety issues</li>
 *   <li>Call frequency tracking added unnecessary overhead for small lists</li>
 * </ul>
 * <p>
 * Instead, modifiers should be added in priority order during initialization,
 * with highest-priority (most likely to modify/remove) modifiers first.
 */
public class ModifierChain implements GoalModifier {

    private final List<GoalModifier> modifiers = new ArrayList<>();

    /**
     * Add a modifier to the end of the chain.
     *
     * @param modifier The modifier to add
     */
    public void add(GoalModifier modifier) {
        modifiers.add(modifier);
    }

    /**
     * Add all modifiers from another chain to this chain.
     * The modifiers are copied, not shared, so each chain remains independent.
     *
     * @param other The chain whose modifiers to add
     */
    public void addAll(ModifierChain other) {
        modifiers.addAll(other.modifiers);
    }

    /**
     * Sort modifiers by a custom comparator.
     * This should be called during initialization, before the chain is used.
     * <p>
     * Example usage:
     * <pre>
     * chain.sortBy(Comparator.comparing(modifier -> modifier.getPriority()));
     * </pre>
     *
     * @param comparator Comparator to sort modifiers
     */
    public void sortBy(Comparator<GoalModifier> comparator) {
        modifiers.sort(comparator);
    }

    @Nullable
    @Override
    public Goal modify(Mob mob, Goal goal) {
        Goal current = goal;

        // Apply modifiers in sequence
        for (GoalModifier modifier : modifiers) {
            current = modifier.modify(mob, current);

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
     * Check if this chain is empty.
     *
     * @return true if no modifiers are registered
     */
    public boolean isEmpty() {
        return modifiers.isEmpty();
    }

    /**
     * Clear all modifiers from this chain.
     */
    public void clear() {
        modifiers.clear();
    }
}
