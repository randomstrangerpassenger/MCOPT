/**
 * Configuration system for MCOPT optimization features.
 *
 * <p>This package contains the centralized configuration system with 65+ options
 * for fine-tuning performance optimizations.</p>
 *
 * <h2>Main Configuration Class</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.config.MCOPTConfig}</h3>
 * <p>NeoForge ModConfigSpec-based configuration with the following categories:</p>
 *
 * <h3>1. Dynamic FPS Controller (5 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_DYNAMIC_FPS} - Enable adaptive FPS limits</li>
 *   <li>{@code ENABLE_BACKGROUND_THROTTLING} - Lower FPS when unfocused/minimized</li>
 *   <li>{@code MENU_FRAME_RATE_LIMIT} - FPS cap on menus</li>
 *   <li>{@code UNFOCUSED_FRAME_RATE_LIMIT} - FPS cap when window unfocused</li>
 *   <li>{@code MINIMIZED_FRAME_RATE_LIMIT} - FPS cap when minimized</li>
 * </ul>
 *
 * <h3>2. Chunk Rendering Optimizations (3 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_CHUNK_OPTIMIZATIONS} - Master switch</li>
 *   <li>{@code CHUNK_UPDATE_LIMIT} - Max chunk updates per frame (1-20, default: 6)</li>
 *   <li>{@code AGGRESSIVE_CHUNK_CULLING} - Extra culling for low-end systems</li>
 * </ul>
 *
 * <h3>3. Elliptical Render Distance (4 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_ELLIPTICAL_RENDER_DISTANCE} - Enable elliptical culling</li>
 *   <li>{@code VERTICAL_RENDER_STRETCH} - Vertical axis multiplier (0.1-3.0, default: 0.75)</li>
 *   <li>{@code HORIZONTAL_RENDER_STRETCH} - Horizontal axis multiplier (0.5-2.0, default: 1.0)</li>
 *   <li>{@code SHOW_CULLED_CHUNKS_DEBUG} - Debug overlay for culled chunks</li>
 * </ul>
 *
 * <h3>4. Entity Culling Settings (3 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_ENTITY_CULLING} - Master switch</li>
 *   <li>{@code ENTITY_CULLING_DISTANCE} - Max render distance (16-256 blocks, default: 64)</li>
 *   <li>{@code CULL_ENTITIES_BEHIND_WALLS} - Back-face culling for occluded entities</li>
 * </ul>
 *
 * <h3>5. Particle System Optimizations (3 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_PARTICLE_OPTIMIZATIONS} - Master switch</li>
 *   <li>{@code MAX_PARTICLES_PER_FRAME} - Particle cap (100-4000, default: 500)</li>
 *   <li>{@code PARTICLE_SPAWN_REDUCTION} - Spawn rate multiplier (0.0-0.9, default: 0.25)</li>
 * </ul>
 *
 * <h3>6. Memory Management (6 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_MEMORY_OPTIMIZATIONS} - Master switch</li>
 *   <li>{@code AGGRESSIVE_GC_PREVENTION} - Reduce automatic GC triggers</li>
 *   <li>{@code ENABLE_OBJECT_POOLING} - Object pooling (currently unused)</li>
 *   <li>{@code ENABLE_RESOURCE_CLEANUP} - Cleanup on world unload</li>
 *   <li>{@code SHOW_MEMORY_HUD} - Display memory usage HUD</li>
 *   <li>{@code LEAK_SAFE_CLEANUP} - Only sweep caches when client threads are idle</li>
 * </ul>
 *
 * <h3>7. Experience Orb Merging (3 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_XP_ORB_MERGING} - Master switch</li>
 *   <li>{@code XP_ORB_MERGE_RADIUS} - Merge distance (0.5-5.0 blocks, default: 1.5)</li>
 *   <li>{@code XP_ORB_MERGE_DELAY} - Ticks before merge (1-40, default: 10)</li>
 * </ul>
 *
 * <h3>8. AI Optimization Settings (3 options)</h3>
 * <ul>
 *   <li>{@code ENABLE_AI_OPTIMIZATIONS} - Master switch</li>
 *   <li>{@code ENABLE_MATH_CACHE} - Trigonometric lookup tables</li>
 *   <li>{@code ENABLE_OPTIMIZED_LOOK_CONTROL} - Cached rotation calculations</li>
 * </ul>
 *
 * <h3>9. AI Goal Removal (17 entity-specific options)</h3>
 * <ul>
 *   <li>Common: {@code REMOVE_LOOK_AT_PLAYER}, {@code REMOVE_RANDOM_LOOK_AROUND}</li>
 *   <li>Animals: {@code REMOVE_ANIMAL_FLOAT}, {@code REMOVE_ANIMAL_PANIC}, {@code REMOVE_ANIMAL_BREED}, etc.</li>
 *   <li>Sheep: {@code REMOVE_SHEEP_EAT_BLOCK}</li>
 *   <li>Aquatic: {@code REMOVE_FISH_SWIM}, {@code REMOVE_SQUID_RANDOM_MOVEMENT}, etc.</li>
 * </ul>
 *
 * <h2>Configuration File</h2>
 * <p>Configuration is stored in {@code config/mcopt-common.toml} and automatically
 * created on first run with sensible defaults.</p>
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Start with default settings and adjust based on performance monitoring</li>
 *   <li>Disable AI goals carefully - some affect gameplay (e.g., breeding, panic)</li>
 *   <li>Use elliptical render distance for best FPS improvement with minimal visual impact</li>
 *   <li>Adjust particle limits in areas with many effects (farms, redstone contraptions)</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.config.MCOPTConfig Main configuration class
 */
package com.randomstrangerpassenger.mcopt.config;
