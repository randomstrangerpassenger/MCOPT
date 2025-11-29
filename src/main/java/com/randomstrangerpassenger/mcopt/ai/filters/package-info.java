/**
 * Goal filtering system for AI optimization.
 *
 * <p>This package provides a flexible filtering framework for identifying and selecting
 * specific AI goals based on various criteria.</p>
 *
 * <h2>Core Interface</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.ai.filters.GoalFilter}</h3>
 * <p>Functional interface for creating goal filters with several factory methods:</p>
 * <ul>
 *   <li><b>matchClassHierarchy:</b> Matches goals by class or any superclass/interface</li>
 *   <li><b>matchExactClass:</b> Matches goals by exact class only</li>
 *   <li><b>matchNamePattern:</b> Matches goals by class name substring (fallback for inner classes)</li>
 *   <li><b>matchMultiple:</b> Combines multiple filters with OR logic</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Match any type of stroll goal (RandomStrollGoal and subclasses)
 * GoalFilter filter = GoalFilter.matchClassHierarchy(RandomStrollGoal.class);
 *
 * // Match exact panic goals only
 * GoalFilter filter = GoalFilter.matchExactClass(PanicGoal.class);
 *
 * // Match inner classes by name pattern
 * GoalFilter filter = GoalFilter.matchNamePattern("RandomMovement");
 *
 * // Combine multiple filters
 * GoalFilter filter = GoalFilter.matchMultiple(
 *     GoalFilter.matchClassHierarchy(LookAtPlayerGoal.class),
 *     GoalFilter.matchClassHierarchy(RandomLookAroundGoal.class)
 * );
 * }</pre>
 *
 * <h2>Design Philosophy</h2>
 * <p>Filters are designed to be:</p>
 * <ul>
 *   <li><b>Version-resilient:</b> Class hierarchy matching handles Minecraft updates</li>
 *   <li><b>Composable:</b> Filters can be combined using default methods</li>
 *   <li><b>Efficient:</b> Simple boolean logic with minimal overhead</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.ai.filters.GoalFilter Main filtering interface
 * @see com.randomstrangerpassenger.mcopt.ai.modifiers.RemoveGoalModifier Uses filters to remove goals
 */
package com.randomstrangerpassenger.mcopt.ai.filters;
