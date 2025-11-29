/**
 * Mixin injections for vanilla Minecraft optimizations.
 *
 * <p>This package contains all Mixin classes that modify vanilla Minecraft behavior
 * to improve performance. Mixins are carefully designed to be non-invasive and
 * compatible with other mods.</p>
 *
 * <h2>Rendering Mixins</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.LevelRendererMixin}</h3>
 * <p>Implements elliptical render distance culling:</p>
 * <ul>
 *   <li>Injects into {@code renderLevel()} to initialize per-frame tracking</li>
 *   <li>Calculates elliptical distance with configurable vertical/horizontal stretch</li>
 *   <li>Updates {@link com.randomstrangerpassenger.mcopt.client.RenderFrameCache} once per frame</li>
 *   <li>Reduces rendered chunk sections by 10-35% with minimal visual difference</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.RenderSectionMixin}</h3>
 * <p>Per-chunk culling decision point:</p>
 * <ul>
 *   <li>Injects into {@code hasAllNeighbors()} to intercept chunk visibility checks</li>
 *   <li>Uses cached frame data from RenderFrameCache for efficiency</li>
 *   <li>Calculates chunk center once and checks against ellipsoid bounds</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.ChunkRenderDispatcherMixin}</h3>
 * <p>Chunk update rate limiting:</p>
 * <ul>
 *   <li>Limits chunk rebuilds per frame (configurable 1-20, default 6)</li>
 *   <li>Prevents FPS drops during world loading or fast movement</li>
 *   <li>Automatically resets counter each frame</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.EntityRenderDispatcherMixin}</h3>
 * <p>Entity culling optimizations:</p>
 * <ul>
 *   <li>Distance-based culling (configurable max distance)</li>
 *   <li>Back-face culling for entities behind camera</li>
 *   <li>Never culls vehicles or passengers for gameplay correctness</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.ParticleEngineMixin}</h3>
 * <p>Particle system optimizations:</p>
 * <ul>
 *   <li>Limits maximum particles per frame (configurable)</li>
 *   <li>Reduces spawn rate by configurable percentage</li>
 *   <li>Particularly effective in areas with many effects (farms, combat)</li>
 * </ul>
 *
 * <h2>Entity/Gameplay Mixins</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.ExperienceOrbMixin}</h3>
 * <p>XP orb merging system:</p>
 * <ul>
 *   <li>Merges nearby XP orbs to reduce entity count</li>
 *   <li>Configurable merge radius and delay</li>
 *   <li>Preserves total XP value exactly</li>
 *   <li>Significantly improves performance in mob grinders</li>
 * </ul>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.mixin.MobAIMixin}</h3>
 * <p>AI goal processing hook:</p>
 * <ul>
 *   <li>Injects into mob AI initialization</li>
 *   <li>Allows {@link com.randomstrangerpassenger.mcopt.ai.AIOptimizationSystem} to process goals</li>
 *   <li>Removes configured goals and replaces controllers</li>
 * </ul>
 *
 * <h2>Mixin Design Principles</h2>
 * <ul>
 *   <li><b>Minimal Invasiveness:</b> Use HEAD/TAIL injection when possible</li>
 *   <li><b>Cancellable:</b> Allow early returns without breaking other mods</li>
 *   <li><b>Configurable:</b> All mixins respect configuration flags</li>
 *   <li><b>Preserving Vanilla:</b> Never change core game mechanics</li>
 *   <li><b>Unique Naming:</b> Use {@code mcopt$} prefix for all mixin members</li>
 * </ul>
 *
 * <h2>Compatibility Notes</h2>
 * <p>All mixins are designed to coexist with other performance mods:</p>
 * <ul>
 *   <li>No @Redirect or @Overwrite - only @Inject</li>
 *   <li>Respects other mods' cancellations</li>
 *   <li>Uses unique member names to avoid conflicts</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.config.MCOPTConfig Configuration for all mixins
 * @see com.randomstrangerpassenger.mcopt.client Client utilities used by rendering mixins
 */
package com.randomstrangerpassenger.mcopt.mixin;
