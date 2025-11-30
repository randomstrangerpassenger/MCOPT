package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Main configuration orchestrator for MCOPT.
 * Delegates to domain-specific configuration classes for better organization.
 * <p>
 * Configuration Domains:
 * - {@link RenderingConfig}: Chunk rendering, entity culling, particles
 * - {@link PerformanceConfig}: Dynamic FPS, memory management, AI optimizations
 * - {@link GameplayConfig}: XP orb merging, weather, entity behaviors, portals
 * - {@link SafetyConfig}: Action guard, clear lag, health stability, attribute ranges
 */
public class MCOPTConfig {

    /**
     * Returns the configuration spec for rendering optimizations.
     */
    public static ModConfigSpec getRenderingSpec() {
        return RenderingConfig.SPEC;
    }

    /**
     * Returns the configuration spec for performance optimizations.
     */
    public static ModConfigSpec getPerformanceSpec() {
        return PerformanceConfig.SPEC;
    }

    /**
     * Returns the configuration spec for gameplay improvements.
     */
    public static ModConfigSpec getGameplaySpec() {
        return GameplayConfig.SPEC;
    }

    /**
     * Returns the configuration spec for safety features.
     */
    public static ModConfigSpec getSafetySpec() {
        return SafetyConfig.SPEC;
    }

    // Convenience accessors to delegate to domain-specific configs

    // ========== Rendering Configuration ==========

    public static ModConfigSpec.BooleanValue ENABLE_CHUNK_OPTIMIZATIONS = RenderingConfig.ENABLE_CHUNK_OPTIMIZATIONS;
    public static ModConfigSpec.IntValue CHUNK_UPDATE_LIMIT = RenderingConfig.CHUNK_UPDATE_LIMIT;
    public static ModConfigSpec.BooleanValue AGGRESSIVE_CHUNK_CULLING = RenderingConfig.AGGRESSIVE_CHUNK_CULLING;
    public static ModConfigSpec.BooleanValue ENABLE_ELLIPTICAL_RENDER_DISTANCE = RenderingConfig.ENABLE_ELLIPTICAL_RENDER_DISTANCE;
    public static ModConfigSpec.DoubleValue VERTICAL_RENDER_STRETCH = RenderingConfig.VERTICAL_RENDER_STRETCH;
    public static ModConfigSpec.DoubleValue HORIZONTAL_RENDER_STRETCH = RenderingConfig.HORIZONTAL_RENDER_STRETCH;
    public static ModConfigSpec.BooleanValue SHOW_CULLED_CHUNKS_DEBUG = RenderingConfig.SHOW_CULLED_CHUNKS_DEBUG;
    public static ModConfigSpec.BooleanValue ENABLE_ENTITY_CULLING = RenderingConfig.ENABLE_ENTITY_CULLING;
    public static ModConfigSpec.IntValue ENTITY_CULLING_DISTANCE = RenderingConfig.ENTITY_CULLING_DISTANCE;
    public static ModConfigSpec.BooleanValue CULL_ENTITIES_BEHIND_WALLS = RenderingConfig.CULL_ENTITIES_BEHIND_WALLS;
    public static ModConfigSpec.BooleanValue ENABLE_PARTICLE_OPTIMIZATIONS = RenderingConfig.ENABLE_PARTICLE_OPTIMIZATIONS;
    public static ModConfigSpec.IntValue MAX_PARTICLES_PER_FRAME = RenderingConfig.MAX_PARTICLES_PER_FRAME;
    public static ModConfigSpec.DoubleValue PARTICLE_SPAWN_REDUCTION = RenderingConfig.PARTICLE_SPAWN_REDUCTION;
    public static ModConfigSpec.BooleanValue ENABLE_PARTICLE_CULLING = RenderingConfig.ENABLE_PARTICLE_CULLING;
    public static ModConfigSpec.IntValue PARTICLE_OCCLUSION_CHECK_INTERVAL = RenderingConfig.PARTICLE_OCCLUSION_CHECK_INTERVAL;
    public static ModConfigSpec.DoubleValue PARTICLE_CULLING_RANGE = RenderingConfig.PARTICLE_CULLING_RANGE;

    // ========== Performance Configuration ==========

    public static ModConfigSpec.BooleanValue ENABLE_DYNAMIC_FPS = PerformanceConfig.ENABLE_DYNAMIC_FPS;
    public static ModConfigSpec.BooleanValue ENABLE_BACKGROUND_THROTTLING = PerformanceConfig.ENABLE_BACKGROUND_THROTTLING;
    public static ModConfigSpec.IntValue MENU_FRAME_RATE_LIMIT = PerformanceConfig.MENU_FRAME_RATE_LIMIT;
    public static ModConfigSpec.IntValue UNFOCUSED_FRAME_RATE_LIMIT = PerformanceConfig.UNFOCUSED_FRAME_RATE_LIMIT;
    public static ModConfigSpec.IntValue MINIMIZED_FRAME_RATE_LIMIT = PerformanceConfig.MINIMIZED_FRAME_RATE_LIMIT;
    public static ModConfigSpec.BooleanValue ENABLE_IDLE_BOOST = PerformanceConfig.ENABLE_IDLE_BOOST;
    public static ModConfigSpec.IntValue IDLE_BOOST_INACTIVITY_SECONDS = PerformanceConfig.IDLE_BOOST_INACTIVITY_SECONDS;
    public static ModConfigSpec.IntValue IDLE_FRAME_RATE_LIMIT = PerformanceConfig.IDLE_FRAME_RATE_LIMIT;
    public static ModConfigSpec.BooleanValue ENABLE_MEMORY_OPTIMIZATIONS = PerformanceConfig.ENABLE_MEMORY_OPTIMIZATIONS;
    public static ModConfigSpec.BooleanValue AGGRESSIVE_GC_PREVENTION = PerformanceConfig.AGGRESSIVE_GC_PREVENTION;
    public static ModConfigSpec.BooleanValue ENABLE_OBJECT_POOLING = PerformanceConfig.ENABLE_OBJECT_POOLING;
    public static ModConfigSpec.BooleanValue ENABLE_RESOURCE_CLEANUP = PerformanceConfig.ENABLE_RESOURCE_CLEANUP;
    public static ModConfigSpec.BooleanValue SHOW_MEMORY_HUD = PerformanceConfig.SHOW_MEMORY_HUD;
    public static ModConfigSpec.BooleanValue ENABLE_LEAK_GUARD = PerformanceConfig.ENABLE_LEAK_GUARD;
    public static ModConfigSpec.BooleanValue LEAK_SAFE_CLEANUP = PerformanceConfig.LEAK_SAFE_CLEANUP;
    public static ModConfigSpec.IntValue LEAK_CHECK_DELAY_TICKS = PerformanceConfig.LEAK_CHECK_DELAY_TICKS;
    public static ModConfigSpec.IntValue LEAK_MEMORY_ALERT_MB = PerformanceConfig.LEAK_MEMORY_ALERT_MB;
    public static ModConfigSpec.BooleanValue LEAK_GC_NUDGE = PerformanceConfig.LEAK_GC_NUDGE;
    public static ModConfigSpec.IntValue LEAK_WARNING_INTERVAL_TICKS = PerformanceConfig.LEAK_WARNING_INTERVAL_TICKS;
    public static ModConfigSpec.IntValue LEAK_MEMORY_ALERT_COOLDOWN_SECONDS = PerformanceConfig.LEAK_MEMORY_ALERT_COOLDOWN_SECONDS;
    public static ModConfigSpec.BooleanValue ENABLE_AI_OPTIMIZATIONS = PerformanceConfig.ENABLE_AI_OPTIMIZATIONS;
    public static ModConfigSpec.BooleanValue ENABLE_MATH_CACHE = PerformanceConfig.ENABLE_MATH_CACHE;
    public static ModConfigSpec.BooleanValue ENABLE_OPTIMIZED_LOOK_CONTROL = PerformanceConfig.ENABLE_OPTIMIZED_LOOK_CONTROL;
    public static ModConfigSpec.BooleanValue REMOVE_LOOK_AT_PLAYER = PerformanceConfig.REMOVE_LOOK_AT_PLAYER;
    public static ModConfigSpec.BooleanValue REMOVE_RANDOM_LOOK_AROUND = PerformanceConfig.REMOVE_RANDOM_LOOK_AROUND;
    public static ModConfigSpec.BooleanValue REMOVE_ANIMAL_FLOAT = PerformanceConfig.REMOVE_ANIMAL_FLOAT;
    public static ModConfigSpec.BooleanValue REMOVE_ANIMAL_PANIC = PerformanceConfig.REMOVE_ANIMAL_PANIC;
    public static ModConfigSpec.BooleanValue REMOVE_ANIMAL_BREED = PerformanceConfig.REMOVE_ANIMAL_BREED;
    public static ModConfigSpec.BooleanValue REMOVE_ANIMAL_TEMPT = PerformanceConfig.REMOVE_ANIMAL_TEMPT;
    public static ModConfigSpec.BooleanValue REMOVE_ANIMAL_FOLLOW_PARENT = PerformanceConfig.REMOVE_ANIMAL_FOLLOW_PARENT;
    public static ModConfigSpec.BooleanValue REMOVE_ANIMAL_STROLL = PerformanceConfig.REMOVE_ANIMAL_STROLL;
    public static ModConfigSpec.BooleanValue REMOVE_SHEEP_EAT_BLOCK = PerformanceConfig.REMOVE_SHEEP_EAT_BLOCK;
    public static ModConfigSpec.BooleanValue REMOVE_FISH_SWIM = PerformanceConfig.REMOVE_FISH_SWIM;
    public static ModConfigSpec.BooleanValue REMOVE_FISH_PANIC = PerformanceConfig.REMOVE_FISH_PANIC;
    public static ModConfigSpec.BooleanValue REMOVE_SQUID_RANDOM_MOVEMENT = PerformanceConfig.REMOVE_SQUID_RANDOM_MOVEMENT;
    public static ModConfigSpec.BooleanValue REMOVE_SQUID_FLEE = PerformanceConfig.REMOVE_SQUID_FLEE;

    // ========== Gameplay Configuration ==========

    public static ModConfigSpec.BooleanValue ENABLE_XP_ORB_MERGING = GameplayConfig.ENABLE_XP_ORB_MERGING;
    public static ModConfigSpec.DoubleValue XP_ORB_MERGE_RADIUS = GameplayConfig.XP_ORB_MERGE_RADIUS;
    public static ModConfigSpec.IntValue XP_ORB_MERGE_DELAY = GameplayConfig.XP_ORB_MERGE_DELAY;
    public static ModConfigSpec.BooleanValue ENABLE_SNOW_ACCUMULATION_FIX = GameplayConfig.ENABLE_SNOW_ACCUMULATION_FIX;
    public static ModConfigSpec.BooleanValue ENABLE_BETTER_SNOW_LOGIC = GameplayConfig.ENABLE_BETTER_SNOW_LOGIC;
    public static ModConfigSpec.BooleanValue ENABLE_BEE_STUCK_FIX = GameplayConfig.ENABLE_BEE_STUCK_FIX;
    public static ModConfigSpec.IntValue BEE_STUCK_TIMEOUT_TICKS = GameplayConfig.BEE_STUCK_TIMEOUT_TICKS;
    public static ModConfigSpec.IntValue BEE_RELINK_COOLDOWN_TICKS = GameplayConfig.BEE_RELINK_COOLDOWN_TICKS;
    public static ModConfigSpec.BooleanValue ENABLE_GOLEM_SPAWN_FIX = GameplayConfig.ENABLE_GOLEM_SPAWN_FIX;
    public static ModConfigSpec.IntValue GOLEM_SPAWN_SEARCH_RANGE = GameplayConfig.GOLEM_SPAWN_SEARCH_RANGE;
    public static ModConfigSpec.BooleanValue ENABLE_PASSENGER_PORTAL_FIX = GameplayConfig.ENABLE_PASSENGER_PORTAL_FIX;
    public static ModConfigSpec.BooleanValue ENABLE_PORTAL_REDIRECT = GameplayConfig.ENABLE_PORTAL_REDIRECT;
    public static ModConfigSpec.BooleanValue ENABLE_BUCKET_PREVIEW = GameplayConfig.ENABLE_BUCKET_PREVIEW;
    public static ModConfigSpec.BooleanValue ENABLE_FISHING_ROD_FIX = GameplayConfig.ENABLE_FISHING_ROD_FIX;
    public static ModConfigSpec.BooleanValue FIX_ENCHANTMENT_RNG = GameplayConfig.FIX_ENCHANTMENT_RNG;

    // ========== Safety Configuration ==========

    public static ModConfigSpec.BooleanValue ENABLE_ACTION_GUARD = SafetyConfig.ENABLE_ACTION_GUARD;
    public static ModConfigSpec.BooleanValue GUARD_PROTECT_TAMED_PETS = SafetyConfig.GUARD_PROTECT_TAMED_PETS;
    public static ModConfigSpec.BooleanValue GUARD_PROTECT_VILLAGERS = SafetyConfig.GUARD_PROTECT_VILLAGERS;
    public static ModConfigSpec.BooleanValue GUARD_PROTECT_DECORATIONS = SafetyConfig.GUARD_PROTECT_DECORATIONS;
    public static ModConfigSpec.BooleanValue GUARD_ALLOW_SNEAK_BYPASS = SafetyConfig.GUARD_ALLOW_SNEAK_BYPASS;
    public static ModConfigSpec.BooleanValue ENABLE_CLEAR_LAG = SafetyConfig.ENABLE_CLEAR_LAG;
    public static ModConfigSpec.IntValue CLEAR_LAG_INTERVAL_TICKS = SafetyConfig.CLEAR_LAG_INTERVAL_TICKS;
    public static ModConfigSpec.IntValue CLEAR_LAG_WARNING_TICKS = SafetyConfig.CLEAR_LAG_WARNING_TICKS;
    public static ModConfigSpec.BooleanValue CLEAR_LAG_REMOVE_ITEMS = SafetyConfig.CLEAR_LAG_REMOVE_ITEMS;
    public static ModConfigSpec.BooleanValue CLEAR_LAG_REMOVE_XP_ORBS = SafetyConfig.CLEAR_LAG_REMOVE_XP_ORBS;
    public static ModConfigSpec.BooleanValue CLEAR_LAG_REMOVE_PROJECTILES = SafetyConfig.CLEAR_LAG_REMOVE_PROJECTILES;
    public static ModConfigSpec.BooleanValue CLEAR_LAG_SKIP_NAMED_ITEMS = SafetyConfig.CLEAR_LAG_SKIP_NAMED_ITEMS;
    public static ModConfigSpec.ConfigValue<java.util.List<? extends String>> CLEAR_LAG_ENTITY_WHITELIST = SafetyConfig.CLEAR_LAG_ENTITY_WHITELIST;
    public static ModConfigSpec.BooleanValue ENABLE_MAX_HEALTH_STABILITY = SafetyConfig.ENABLE_MAX_HEALTH_STABILITY;
    public static ModConfigSpec.BooleanValue ENABLE_ATTRIBUTE_RANGE_EXPANSION = SafetyConfig.ENABLE_ATTRIBUTE_RANGE_EXPANSION;
    public static ModConfigSpec.DoubleValue ATTRIBUTE_MAX_LIMIT = SafetyConfig.ATTRIBUTE_MAX_LIMIT;
}
