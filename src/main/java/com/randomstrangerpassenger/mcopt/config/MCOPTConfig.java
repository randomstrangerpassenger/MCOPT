package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Main configuration orchestrator for MCOPT.
 * Provides access to domain-specific configuration classes.
 * <p>
 * Configuration Domains:
 * - {@link RenderingConfig}: Chunk rendering, entity culling, particles
 * - {@link PerformanceConfig}: Dynamic FPS, memory management, AI optimizations
 * - {@link GameplayConfig}: XP orb merging, weather, entity behaviors, portals
 * - {@link SafetyConfig}: Action guard, clear lag, health stability, attribute
 * ranges
 * <p>
 * <b>Usage:</b> Instead of accessing config values through this class,
 * use the domain-specific config classes directly:
 * 
 * <pre>{@code
 * // Old way (deprecated):
 * if (MCOPTConfig.ENABLE_CHUNK_OPTIMIZATIONS.get()) { ... }
 *
 * // New way (recommended):
 * if (RenderingConfig.ENABLE_CHUNK_OPTIMIZATIONS.get()) { ... }
 * }</pre>
 */
public class MCOPTConfig {

     /**
      * Returns the configuration spec for rendering optimizations.
      *
      * @return ModConfigSpec for rendering settings
      */
     public static ModConfigSpec getRenderingSpec() {
          return RenderingConfig.SPEC;
     }

     /**
      * Returns the configuration spec for performance optimizations.
      *
      * @return ModConfigSpec for performance settings
      */
     public static ModConfigSpec getPerformanceSpec() {
          return PerformanceConfig.SPEC;
     }

     /**
      * Returns the configuration spec for gameplay improvements.
      *
      * @return ModConfigSpec for gameplay settings
      */
     public static ModConfigSpec getGameplaySpec() {
          return GameplayConfig.SPEC;
     }

     /**
      * Returns the configuration spec for safety features.
      *
      * @return ModConfigSpec for safety settings
      */
     public static ModConfigSpec getSafetySpec() {
          return SafetyConfig.SPEC;
     }
}
