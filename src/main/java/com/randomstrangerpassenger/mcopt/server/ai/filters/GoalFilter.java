package com.randomstrangerpassenger.mcopt.server.ai.filters;

import net.minecraft.world.entity.ai.goal.Goal;

/**
 * Functional interface for filtering AI goals.
 *
 * Goal filters determine which AI goals should be removed or modified.
 */
@FunctionalInterface
public interface GoalFilter {

    /**
     * Test if this goal should be affected by the filter.
     *
     * @param goal The goal to test
     * @return true if the goal matches this filter
     */
    boolean test(Goal goal);

    /**
     * Create a filter that matches goals by exact class.
     *
     * @param goalClass The goal class to match
     * @return Filter that matches the specified class
     */
    static GoalFilter matchClass(Class<? extends Goal> goalClass) {
        return goal -> goal.getClass() == goalClass;
    }

    /**
     * Create a filter that matches goals by class or subclass.
     *
     * @param goalClass The goal class to match (including subclasses)
     * @return Filter that matches the specified class hierarchy
     */
    static GoalFilter matchClassHierarchy(Class<? extends Goal> goalClass) {
        return goalClass::isInstance;
    }

    /**
     * Create a filter that matches goals by class name pattern.
     *
     * @param pattern The pattern to match in the class name
     * @return Filter that matches the pattern
     */
    static GoalFilter matchNamePattern(String pattern) {
        return goal -> goal.getClass().getSimpleName().contains(pattern);
    }

    /**
     * Combine this filter with another using AND logic.
     *
     * @param other The other filter
     * @return Combined filter
     */
    default GoalFilter and(GoalFilter other) {
        return goal -> this.test(goal) && other.test(goal);
    }

    /**
     * Combine this filter with another using OR logic.
     *
     * @param other The other filter
     * @return Combined filter
     */
    default GoalFilter or(GoalFilter other) {
        return goal -> this.test(goal) || other.test(goal);
    }

    /**
     * Negate this filter.
     *
     * @return Inverted filter
     */
    default GoalFilter negate() {
        return goal -> !this.test(goal);
    }
}
