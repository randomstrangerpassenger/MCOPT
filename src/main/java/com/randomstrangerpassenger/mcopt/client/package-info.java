/**
 * Client-side rendering optimizations and memory management tools.
 *
 * <p>This package contains client-only features that improve rendering performance
 * and provide memory monitoring capabilities.</p>
 *
 * <h2>Rendering Optimizations</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.client.rendering.RenderFrameCache}</h3>
 * <p>Per-frame caching system for render calculations:</p>
 * <ul>
 *   <li>Caches camera position, render distance, and stretch factors once per frame</li>
 *   <li>Prevents redundant calculations across thousands of chunk sections</li>
 *   <li>Used by elliptical render distance culling system</li>
 *   <li>Automatically invalidates cache each frame for correctness</li>
 * </ul>
 *
 * <h2>Memory Management</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.client.hud.MemoryHudRenderer}</h3>
 * <p>Real-time memory usage display:</p>
 * <ul>
 *   <li>Shows RAM usage in top-left corner (configurable)</li>
 *   <li>Updates every 1 second to minimize overhead</li>
 *   <li>Displays used/max memory in MB and percentage</li>
 *   <li>Automatically hides when F3 debug screen is open</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.safety.PanicButtonHandler}</h3>
 * <p>Emergency memory cleanup trigger (F8 key by default):</p>
 * <ul>
 *   <li>Suggests garbage collection on demand</li>
 *   <li>Shows before/after memory usage and freed amount</li>
 *   <li>5-second cooldown to prevent spam</li>
 *   <li>Useful for debugging memory issues or clearing memory spikes</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.safety.ResourceCleanupHandler}</h3>
 * <p>Automatic cleanup on world change or logout:</p>
 * <ul>
 *   <li>Triggers cleanup when client disconnects from server/world</li>
 *   <li>Clears particle engine cached data</li>
 *   <li>Helps prevent memory accumulation across multiple sessions</li>
 * </ul>
 *
 * <h2>NeoForge 1.21.10 Integration</h2>
 * <p>All client features use modern NeoForge APIs:</p>
 * <ul>
 *   <li>GUI Layer system for HUD rendering (not overlays)</li>
 *   <li>Event bus subscription with {@code @EventBusSubscriber}</li>
 *   <li>Client-only distribution enforcement with {@code Dist.CLIENT}</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>All features can be toggled in {@link com.randomstrangerpassenger.mcopt.config.MCOPTConfig}:</p>
 * <ul>
 *   <li>{@code ENABLE_MEMORY_OPTIMIZATIONS} - Master switch for memory features</li>
 *   <li>{@code SHOW_MEMORY_HUD} - Toggle HUD visibility</li>
 *   <li>{@code ENABLE_RESOURCE_CLEANUP} - Enable automatic cleanup</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.client.rendering.RenderFrameCache Frame-level caching
 * @see com.randomstrangerpassenger.mcopt.client.hud.MemoryHudRenderer Memory HUD
 * @see com.randomstrangerpassenger.mcopt.mixin Rendering mixins that use these tools
 */
package com.randomstrangerpassenger.mcopt.client;
