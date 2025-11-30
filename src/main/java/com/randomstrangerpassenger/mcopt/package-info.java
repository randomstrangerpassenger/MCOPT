/**
 * MCOPT - Minecraft Performance Optimization Mod for NeoForge 1.21.10
 *
 * <p>This mod provides comprehensive performance optimizations for Minecraft, focusing on
 * improving game logic and rendering performance to reduce lag and improve FPS.</p>
 *
 * <h2>Main Features</h2>
 * <ul>
 *   <li><b>Elliptical Render Distance:</b> Reduces rendered chunk sections by 10-35%</li>
 *   <li><b>Entity Culling:</b> Skips rendering distant or occluded entities</li>
 *   <li><b>Particle Optimizations:</b> Limits particle spawn rates and counts</li>
 *   <li><b>AI Optimizations:</b> Streamlines mob AI goal processing with configurable goal removal</li>
 *   <li><b>Math Caching:</b> Pre-calculated lookup tables for expensive trigonometric functions</li>
 *   <li><b>XP Orb Merging:</b> Combines nearby experience orbs to reduce entity count</li>
 *   <li><b>Memory Management:</b> Proactive resource cleanup and optional HUD monitoring</li>
 * </ul>
 *
 * <h2>Architecture</h2>
 * <p>The mod is organized into several key packages:</p>
 * <ul>
 *   <li>{@link com.randomstrangerpassenger.mcopt.server.ai} - AI optimization system with math caching</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.client} - Client-side rendering and memory tools</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.config} - Configuration system with 65+ options</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.mixin} - Mixin injections for vanilla optimization</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.util} - Utility classes and constants</li>
 * </ul>
 *
 * <h2>Compatibility</h2>
 * <p>MCOPT prioritizes mod compatibility by using:</p>
 * <ul>
 *   <li>Tag-based entity filtering instead of hardcoded class checks</li>
 *   <li>Non-invasive Mixin injection points</li>
 *   <li>Configurable optimization levels for each feature</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.MCOPT Main mod class
 * @see com.randomstrangerpassenger.mcopt.config.MCOPTConfig Configuration options
 */
package com.randomstrangerpassenger.mcopt;
