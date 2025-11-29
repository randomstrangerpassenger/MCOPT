/**
 * AI optimization system for improving mob behavior performance.
 *
 * <p>This package contains the core AI optimization framework that reduces CPU usage
 * from mob AI processing through several strategies:</p>
 *
 * <h2>Components</h2>
 *
 * <h3>Math Caching ({@link com.randomstrangerpassenger.mcopt.ai.MathCache})</h3>
 * <p>Provides pre-calculated lookup tables for expensive trigonometric operations:</p>
 * <ul>
 *   <li><b>atan2:</b> 256-entry lookup table (16x16 grid) for fast angle calculations</li>
 *   <li><b>sin/cos:</b> 4096-entry tables for precise trigonometric values</li>
 *   <li>Used by AI path calculations, look controllers, and rotation logic</li>
 * </ul>
 *
 * <h3>AI Goal Management ({@link com.randomstrangerpassenger.mcopt.ai.AIOptimizationSystem})</h3>
 * <p>Central system for processing and optimizing mob AI goals:</p>
 * <ul>
 *   <li>Tag-based entity classification for mod compatibility</li>
 *   <li>Configurable goal removal (LookAtPlayer, RandomLookAround, Panic, etc.)</li>
 *   <li>Entity-specific optimizations (farm animals, aquatic mobs, sheep)</li>
 *   <li>Thread-safe initialization with double-checked locking</li>
 * </ul>
 *
 * <h3>Optimized Controllers ({@link com.randomstrangerpassenger.mcopt.ai.OptimizedLookControl})</h3>
 * <p>Replacement controllers that use cached math for better performance:</p>
 * <ul>
 *   <li>Drop-in replacement for vanilla LookControl</li>
 *   <li>Uses MathCache.atan2() instead of Math.atan2()</li>
 *   <li>Most effective with 100+ mobs simultaneously active</li>
 * </ul>
 *
 * <h2>Subpackages</h2>
 * <ul>
 *   <li>{@link com.randomstrangerpassenger.mcopt.ai.filters} - Goal filtering interfaces and implementations</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.ai.modifiers} - Goal modification chain system</li>
 * </ul>
 *
 * <h2>Performance Impact</h2>
 * <p>Typical performance improvements:</p>
 * <ul>
 *   <li>5-15% reduction in mob AI tick time</li>
 *   <li>More noticeable with large mob counts (farms, mob grinders)</li>
 *   <li>Configurable trade-offs between realism and performance</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.ai.AIOptimizationSystem Main orchestrator
 * @see com.randomstrangerpassenger.mcopt.ai.MathCache Math lookup tables
 * @see com.randomstrangerpassenger.mcopt.config.MCOPTConfig AI-related configuration options
 */
package com.randomstrangerpassenger.mcopt.ai;
