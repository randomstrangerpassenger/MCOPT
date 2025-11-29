/**
 * Goal modification chain system for transforming mob AI behaviors.
 *
 * <p>This package implements a chain-of-responsibility pattern for processing AI goals,
 * allowing multiple modifications to be applied in sequence with automatic optimization.</p>
 *
 * <h2>Core Components</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.ai.modifiers.GoalModifier}</h3>
 * <p>Base interface for goal transformation operations:</p>
 * <ul>
 *   <li>Receives a goal and returns: same goal (keep), new goal (replace), or null (remove)</li>
 *   <li>Supports chaining via {@code andThen()} for composable transformations</li>
 *   <li>Functional interface for lambda usage</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.ai.modifiers.ModifierChain}</h3>
 * <p>Container for multiple modifiers with performance optimization:</p>
 * <ul>
 *   <li>Executes modifiers in sequence, stopping if any returns null</li>
 *   <li>Tracks call frequency and bubbles frequently-matched modifiers to front</li>
 *   <li>Supports inheritance via {@code addAll()} for shared modifier sets</li>
 *   <li>Optional optimization can be disabled for deterministic ordering</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.ai.modifiers.RemoveGoalModifier}</h3>
 * <p>Concrete implementation for conditionally removing goals:</p>
 * <ul>
 *   <li>Combines a filter (what to match) with a config option (when to remove)</li>
 *   <li>Only removes goals when configuration is enabled</li>
 *   <li>Most commonly used modifier in the system</li>
 * </ul>
 *
 * <h2>Usage Pattern</h2>
 * <pre>{@code
 * // Create a modifier chain for farm animals
 * ModifierChain farmAnimalChain = new ModifierChain();
 *
 * // Add goal removal modifiers
 * farmAnimalChain.add(new RemoveGoalModifier(
 *     GoalFilter.matchClassHierarchy(PanicGoal.class),
 *     MCOPTConfig.REMOVE_ANIMAL_PANIC
 * ));
 *
 * farmAnimalChain.add(new RemoveGoalModifier(
 *     GoalFilter.matchClassHierarchy(BreedGoal.class),
 *     MCOPTConfig.REMOVE_ANIMAL_BREED
 * ));
 *
 * // Apply to all goals in a selector
 * for (WrappedGoal wrapped : selector.getAvailableGoals()) {
 *     Goal modified = farmAnimalChain.modify(mob, wrapped.getGoal());
 *     if (modified == null) {
 *         selector.removeGoal(wrapped.getGoal());
 *     }
 * }
 * }</pre>
 *
 * <h2>Performance Optimization</h2>
 * <p>The chain automatically optimizes itself by:</p>
 * <ul>
 *   <li>Tracking how often each modifier matches goals</li>
 *   <li>Moving frequently-matching modifiers toward the front</li>
 *   <li>Reducing average traversal depth over time</li>
 *   <li>This optimization is particularly effective with large modifier sets</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.ai.modifiers.ModifierChain Main chain implementation
 * @see com.randomstrangerpassenger.mcopt.ai.modifiers.RemoveGoalModifier Goal removal modifier
 * @see com.randomstrangerpassenger.mcopt.ai.AIOptimizationSystem Uses modifier chains
 */
package com.randomstrangerpassenger.mcopt.ai.modifiers;
