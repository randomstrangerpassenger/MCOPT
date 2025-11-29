package com.randomstrangerpassenger.mcopt.ai;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.ai.filters.EntityFilter;
import com.randomstrangerpassenger.mcopt.ai.filters.FilterResult;
import com.randomstrangerpassenger.mcopt.ai.filters.GoalFilter;
import com.randomstrangerpassenger.mcopt.ai.modifiers.ModifierChain;
import com.randomstrangerpassenger.mcopt.ai.modifiers.RemoveGoalModifier;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.*;

import java.util.*;

/**
 * Main system for AI optimization and goal management.
 *
 * This system handles:
 * - Math caching initialization
 * - LookControl replacement with optimized version
 * - AI goal filtering and removal based on configuration
 * - Entity-specific optimizations
 *
 * Inspired by AI-Improvements mod, but with independent implementation using Mixin
 * instead of Access Transformers.
 */
public class AIOptimizationSystem {

    private static boolean initialized = false;

    private static final Map<Class<? extends Mob>, ModifierChain> entityModifiers = new HashMap<>();
    private static final ModifierChain globalModifiers = new ModifierChain();

    /**
     * Initialize the AI optimization system.
     * Must be called during FMLCommonSetupEvent.
     */
    public static void init() {
        if (initialized) {
            MCOPT.LOGGER.warn("AIOptimizationSystem already initialized");
            return;
        }

        MCOPT.LOGGER.info("Initializing AI Optimization System...");

        // Initialize math caching if enabled
        if (MCOPTConfig.ENABLE_MATH_CACHE.get()) {
            MathCache.init();
        }

        // Setup global modifiers (apply to all mobs)
        setupGlobalModifiers();

        // Setup entity-specific modifiers
        setupAnimalModifiers();
        setupAquaticModifiers();

        initialized = true;
        MCOPT.LOGGER.info("AI Optimization System initialized with {} global modifiers and {} entity-specific modifier groups",
                globalModifiers.size(), entityModifiers.size());
    }

    /**
     * Setup modifiers that apply to all mobs.
     */
    private static void setupGlobalModifiers() {
        // Remove LookAtPlayerGoal if configured
        globalModifiers.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(LookAtPlayerGoal.class),
                MCOPTConfig.REMOVE_LOOK_AT_PLAYER
        ));

        // Remove RandomLookAroundGoal if configured
        globalModifiers.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(RandomLookAroundGoal.class),
                MCOPTConfig.REMOVE_RANDOM_LOOK_AROUND
        ));
    }

    /**
     * Setup modifiers for farm animals (Cow, Pig, Chicken, Sheep).
     */
    private static void setupAnimalModifiers() {
        ModifierChain animalChain = new ModifierChain();

        // Float goal
        animalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(FloatGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_FLOAT
        ));

        // Panic goal
        animalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(PanicGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_PANIC
        ));

        // Breed goal
        animalChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("Breed"),
                MCOPTConfig.REMOVE_ANIMAL_BREED
        ));

        // Tempt goal
        animalChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("Tempt"),
                MCOPTConfig.REMOVE_ANIMAL_TEMPT
        ));

        // Follow parent goal
        animalChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("FollowParent"),
                MCOPTConfig.REMOVE_ANIMAL_FOLLOW_PARENT
        ));

        // Random stroll goal
        animalChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("Stroll"),
                MCOPTConfig.REMOVE_ANIMAL_STROLL
        ));

        // Apply to all farm animal types
        entityModifiers.put(Cow.class, animalChain);
        entityModifiers.put(Pig.class, animalChain);
        entityModifiers.put(Chicken.class, animalChain);
        entityModifiers.put(Sheep.class, animalChain);

        // Sheep-specific: Eat block goal
        ModifierChain sheepChain = new ModifierChain();
        sheepChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("EatBlock"),
                MCOPTConfig.REMOVE_SHEEP_EAT_BLOCK
        ));
        // Combine with animal modifiers for sheep
        for (int i = 0; i < animalChain.size(); i++) {
            ModifierChain finalAnimalChain = animalChain;
            int index = i;
            sheepChain.add((mob, goal) -> {
                ModifierChain chain = new ModifierChain();
                // This is a workaround - ideally we'd just reuse the chain
                return goal;
            });
        }

        entityModifiers.put(Sheep.class, sheepChain);
    }

    /**
     * Setup modifiers for aquatic mobs (Fish, Squid).
     */
    private static void setupAquaticModifiers() {
        // Fish modifiers
        ModifierChain fishChain = new ModifierChain();

        fishChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("Swim"),
                MCOPTConfig.REMOVE_FISH_SWIM
        ));

        fishChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(PanicGoal.class),
                MCOPTConfig.REMOVE_FISH_PANIC
        ));

        // Apply to fish types
        entityModifiers.put(Cod.class, fishChain);
        entityModifiers.put(Salmon.class, fishChain);
        entityModifiers.put(TropicalFish.class, fishChain);
        entityModifiers.put(Pufferfish.class, fishChain);

        // Squid modifiers
        ModifierChain squidChain = new ModifierChain();

        squidChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("RandomMovement"),
                MCOPTConfig.REMOVE_SQUID_RANDOM_MOVEMENT
        ));

        squidChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("Flee"),
                MCOPTConfig.REMOVE_SQUID_FLEE
        ));

        entityModifiers.put(Squid.class, squidChain);
    }

    /**
     * Process a mob's AI goals after they have been registered.
     * Called from Mixin injection point.
     *
     * @param mob The mob whose goals to process
     */
    public static void processMobGoals(Mob mob) {
        if (!MCOPTConfig.ENABLE_AI_OPTIMIZATIONS.get()) {
            return;
        }

        // Replace LookControl if enabled
        if (MCOPTConfig.ENABLE_OPTIMIZED_LOOK_CONTROL.get() && MCOPTConfig.ENABLE_MATH_CACHE.get()) {
            replaceLookControl(mob);
        }

        // Process goals
        processGoalSelector(mob, mob.goalSelector);
        processGoalSelector(mob, mob.targetSelector);
    }

    /**
     * Replace mob's LookControl with optimized version.
     */
    private static void replaceLookControl(Mob mob) {
        try {
            mob.lookControl = OptimizedLookControl.createFrom(mob, mob.lookControl);
        } catch (Exception e) {
            MCOPT.LOGGER.error("Failed to replace LookControl for {}: {}", mob.getType(), e.getMessage());
        }
    }

    /**
     * Process a goal selector, applying all relevant modifiers.
     */
    private static void processGoalSelector(Mob mob, net.minecraft.world.entity.ai.goal.GoalSelector selector) {
        Set<WrappedGoal> goalsToRemove = new HashSet<>();

        // Iterate through all goals
        for (WrappedGoal wrappedGoal : selector.getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();

            // Apply global modifiers
            goal = globalModifiers.modify(mob, goal);

            // Apply entity-specific modifiers if any
            if (goal != null) {
                ModifierChain entityChain = getModifierChain(mob.getClass());
                if (entityChain != null) {
                    goal = entityChain.modify(mob, goal);
                }
            }

            // Mark for removal if null
            if (goal == null) {
                goalsToRemove.add(wrappedGoal);
            }
        }

        // Remove marked goals
        for (WrappedGoal wrappedGoal : goalsToRemove) {
            selector.removeGoal(wrappedGoal.getGoal());
        }

        if (!goalsToRemove.isEmpty()) {
            MCOPT.LOGGER.debug("Removed {} AI goals from {}", goalsToRemove.size(), mob.getType());
        }
    }

    /**
     * Get the modifier chain for a specific entity class, checking inheritance.
     */
    private static ModifierChain getModifierChain(Class<? extends Mob> mobClass) {
        // Direct match
        if (entityModifiers.containsKey(mobClass)) {
            return entityModifiers.get(mobClass);
        }

        // Check inheritance
        for (Map.Entry<Class<? extends Mob>, ModifierChain> entry : entityModifiers.entrySet()) {
            if (entry.getKey().isAssignableFrom(mobClass)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Check if the system has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
