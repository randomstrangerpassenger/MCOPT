package com.randomstrangerpassenger.mcopt.ai.modifiers;

import com.randomstrangerpassenger.mcopt.ai.filters.GoalFilter;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * Goal modifier that removes goals matching a specific filter.
 *
 * Can be configured to only remove goals when a config option is enabled.
 */
public class RemoveGoalModifier implements GoalModifier {

    private final GoalFilter filter;
    private final ModConfigSpec.BooleanValue configValue;

    /**
     * Create a remove modifier with a config-based enable/disable option.
     *
     * @param filter The filter to match goals for removal
     * @param configValue The config value that enables/disables this modifier
     */
    public RemoveGoalModifier(GoalFilter filter, ModConfigSpec.BooleanValue configValue) {
        this.filter = filter;
        this.configValue = configValue;
    }

    /**
     * Create a remove modifier that is always active.
     *
     * @param filter The filter to match goals for removal
     */
    public RemoveGoalModifier(GoalFilter filter) {
        this(filter, null);
    }

    @Nullable
    @Override
    public Goal modify(Mob mob, Goal goal) {
        // Check if this modifier is enabled
        if (configValue != null && !configValue.get()) {
            return goal;  // Keep goal unchanged
        }

        // Check if goal matches filter
        if (filter.test(goal)) {
            return null;  // Remove goal
        }

        return goal;  // Keep goal unchanged
    }

    /**
     * Create a modifier that removes goals of a specific class when config is enabled.
     *
     * @param goalClass The goal class to remove
     * @param configValue The config value controlling this removal
     * @return Configured RemoveGoalModifier
     */
    public static RemoveGoalModifier forClass(Class<? extends Goal> goalClass,
                                              ModConfigSpec.BooleanValue configValue) {
        return new RemoveGoalModifier(GoalFilter.matchClass(goalClass), configValue);
    }

    /**
     * Create a modifier that removes goals by class hierarchy when config is enabled.
     *
     * @param goalClass The goal class (including subclasses) to remove
     * @param configValue The config value controlling this removal
     * @return Configured RemoveGoalModifier
     */
    public static RemoveGoalModifier forClassHierarchy(Class<? extends Goal> goalClass,
                                                       ModConfigSpec.BooleanValue configValue) {
        return new RemoveGoalModifier(GoalFilter.matchClassHierarchy(goalClass), configValue);
    }
}
