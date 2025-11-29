package com.randomstrangerpassenger.mcopt.ai;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.MCOPTEntityTypeTags;
import com.randomstrangerpassenger.mcopt.ai.filters.GoalFilter;
import com.randomstrangerpassenger.mcopt.ai.modifiers.ModifierChain;
import com.randomstrangerpassenger.mcopt.ai.modifiers.RemoveGoalModifier;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    // Thread-safe initialization flag (volatile ensures visibility across threads)
    private static volatile boolean initialized = false;

    // Tag-based modifiers for better mod compatibility and data pack support
    // Using ConcurrentHashMap for thread-safe access during multi-threaded entity processing
    private static final Map<TagKey<EntityType<?>>, ModifierChain> tagModifiers = new ConcurrentHashMap<>();
    private static final ModifierChain globalModifiers = new ModifierChain();

    /**
     * Initialize the AI optimization system.
     * Must be called during FMLCommonSetupEvent.
     * Thread-safe using double-checked locking pattern.
     */
    public static void init() {
        // Double-checked locking for thread-safe initialization
        if (initialized) {
            MCOPT.LOGGER.warn("AIOptimizationSystem already initialized, skipping");
            return;
        }

        synchronized (AIOptimizationSystem.class) {
            // Check again inside synchronized block
            if (initialized) {
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
            MCOPT.LOGGER.info("AI Optimization System initialized with {} global modifiers and {} tag-based modifier groups",
                    globalModifiers.size(), tagModifiers.size());
        }
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
     * Setup modifiers for farm animals using tag-based system.
     * This allows better mod compatibility and data pack customization.
     */
    private static void setupAnimalModifiers() {
        // Base farm animal modifiers (applies to all farm animals)
        ModifierChain farmAnimalChain = new ModifierChain();

        // Float goal
        farmAnimalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(FloatGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_FLOAT
        ));

        // Panic goal
        farmAnimalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(PanicGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_PANIC
        ));

        // Breed goal (class-based filtering for better version compatibility)
        farmAnimalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(BreedGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_BREED
        ));

        // Tempt goal (class-based filtering for better version compatibility)
        farmAnimalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(TemptGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_TEMPT
        ));

        // Follow parent goal (class-based filtering for better version compatibility)
        farmAnimalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(FollowParentGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_FOLLOW_PARENT
        ));

        // Random stroll goal (class-based, catches RandomStrollGoal and WaterAvoidingRandomStrollGoal)
        farmAnimalChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(RandomStrollGoal.class),
                MCOPTConfig.REMOVE_ANIMAL_STROLL
        ));

        // Apply to all farm animals via tag
        tagModifiers.put(MCOPTEntityTypeTags.FARM_ANIMALS, farmAnimalChain);

        // Wool-growing animals (Sheep): Additional modifiers for EatBlock behavior
        ModifierChain woolGrowingChain = new ModifierChain();
        woolGrowingChain.addAll(farmAnimalChain);  // Inherit all farm animal modifiers
        woolGrowingChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(EatBlockGoal.class),
                MCOPTConfig.REMOVE_SHEEP_EAT_BLOCK
        ));

        tagModifiers.put(MCOPTEntityTypeTags.WOOL_GROWING_ANIMALS, woolGrowingChain);
    }

    /**
     * Setup modifiers for aquatic mobs using tag-based system.
     * This allows better mod compatibility and data pack customization.
     */
    private static void setupAquaticModifiers() {
        // Fish modifiers
        ModifierChain fishChain = new ModifierChain();

        // Random swimming goal (class-based filtering for better version compatibility)
        fishChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(RandomSwimmingGoal.class),
                MCOPTConfig.REMOVE_FISH_SWIM
        ));

        fishChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(PanicGoal.class),
                MCOPTConfig.REMOVE_FISH_PANIC
        ));

        // Apply to all fish via tag
        tagModifiers.put(MCOPTEntityTypeTags.FISH, fishChain);

        // Squid modifiers
        ModifierChain squidChain = new ModifierChain();

        // Squid's RandomMovementGoal is an inner class, so we use pattern matching as fallback
        // This provides better compatibility across different Minecraft versions
        squidChain.add(new RemoveGoalModifier(
                GoalFilter.matchNamePattern("RandomMovement"),
                MCOPTConfig.REMOVE_SQUID_RANDOM_MOVEMENT
        ));

        // Flee goal (class-based filtering)
        squidChain.add(new RemoveGoalModifier(
                GoalFilter.matchClassHierarchy(FleeGoal.class),
                MCOPTConfig.REMOVE_SQUID_FLEE
        ));

        // Apply to all squid-like entities via tag
        tagModifiers.put(MCOPTEntityTypeTags.SQUID_LIKE, squidChain);
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

            // Apply tag-based modifiers if any
            if (goal != null) {
                goal = applyTagModifiers(mob, goal);
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
     * Apply tag-based modifiers to a goal.
     * Checks if the mob's entity type matches any registered tags.
     *
     * @param mob The mob to check tags for
     * @param goal The goal to modify
     * @return Modified goal (or null if removed)
     */
    private static Goal applyTagModifiers(Mob mob, Goal goal) {
        EntityType<?> entityType = mob.getType();
        Goal current = goal;

        // Apply modifiers for each matching tag
        for (Map.Entry<TagKey<EntityType<?>>, ModifierChain> entry : tagModifiers.entrySet()) {
            if (entityType.is(entry.getKey())) {
                current = entry.getValue().modify(mob, current);

                // If goal was removed, stop processing
                if (current == null) {
                    return null;
                }
            }
        }

        return current;
    }

    /**
     * Check if the system has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
