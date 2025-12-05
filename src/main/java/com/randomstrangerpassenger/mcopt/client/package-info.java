/**
 * Client-side rendering optimizations and memory management.
 *
 * <p>
 * Contains client-only features for rendering performance and memory
 * monitoring.
 * </p>
 *
 * <h2>Rendering</h2>
 * <ul>
 * <li>{@link com.randomstrangerpassenger.mcopt.client.rendering.RenderFrameCache}
 * - Per-frame cache</li>
 * <li>{@link com.randomstrangerpassenger.mcopt.client.rendering.AnimatedTextureOptimizer}
 * - Smart texture animation updates</li>
 * <li>{@link com.randomstrangerpassenger.mcopt.client.rendering.ItemEntityBatcher}
 * - Visual item merging</li>
 * </ul>
 *
 * <h2>Memory Management</h2>
 * <ul>
 * <li>{@link com.randomstrangerpassenger.mcopt.client.hud.MemoryHudRenderer} -
 * RAM HUD display</li>
 * <li>{@link com.randomstrangerpassenger.mcopt.safety.PanicButtonHandler} - F8
 * emergency cleanup</li>
 * </ul>
 *
 * <h2>Dynamic FPS</h2>
 * <ul>
 * <li>{@link com.randomstrangerpassenger.mcopt.client.fps.DynamicFpsManager} -
 * Adaptive FPS control</li>
 * <li>{@link com.randomstrangerpassenger.mcopt.client.fps.FpsLimitResolver} -
 * FPS limit calculation</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.config.RenderingConfig
 * @see com.randomstrangerpassenger.mcopt.config.PerformanceConfig
 */
package com.randomstrangerpassenger.mcopt.client;
