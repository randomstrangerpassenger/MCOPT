package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for rendering optimizations.
 * Includes chunk rendering, render distance, entity culling, and particle systems.
 */
public class RenderingConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Chunk Optimization Settings
    public static final ModConfigSpec.BooleanValue ENABLE_CHUNK_OPTIMIZATIONS;
    public static final ModConfigSpec.IntValue CHUNK_UPDATE_LIMIT;
    public static final ModConfigSpec.BooleanValue AGGRESSIVE_CHUNK_CULLING;

    // Render Distance Optimization Settings
    public static final ModConfigSpec.BooleanValue ENABLE_ELLIPTICAL_RENDER_DISTANCE;
    public static final ModConfigSpec.DoubleValue VERTICAL_RENDER_STRETCH;
    public static final ModConfigSpec.DoubleValue HORIZONTAL_RENDER_STRETCH;
    public static final ModConfigSpec.BooleanValue SHOW_CULLED_CHUNKS_DEBUG;

    // Entity Culling Settings
    public static final ModConfigSpec.BooleanValue ENABLE_ENTITY_CULLING;
    public static final ModConfigSpec.IntValue ENTITY_CULLING_DISTANCE;
    public static final ModConfigSpec.BooleanValue CULL_ENTITIES_BEHIND_WALLS;

    // Particle System Settings
    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLE_OPTIMIZATIONS;
    public static final ModConfigSpec.IntValue MAX_PARTICLES_PER_FRAME;
    public static final ModConfigSpec.DoubleValue PARTICLE_SPAWN_REDUCTION;
    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLE_CULLING;
    public static final ModConfigSpec.IntValue PARTICLE_OCCLUSION_CHECK_INTERVAL;
    public static final ModConfigSpec.DoubleValue PARTICLE_CULLING_RANGE;

    static {
        BUILDER.comment("MCOPT Rendering Optimizations Configuration")
               .push("rendering");

        BUILDER.comment("Chunk Rendering Optimizations")
               .push("chunk_rendering");

        ENABLE_CHUNK_OPTIMIZATIONS = BUILDER
                .comment("Enable chunk rendering optimizations (Recommended: true)")
                .define("enableChunkOptimizations", true);

        CHUNK_UPDATE_LIMIT = BUILDER
                .comment("Maximum number of chunk updates per frame (Lower = better FPS, Higher = faster world updates)")
                .defineInRange("chunkUpdateLimit", 6, 1, 20);

        AGGRESSIVE_CHUNK_CULLING = BUILDER
                .comment("Enable aggressive chunk culling (May cause pop-in, but improves FPS)")
                .define("aggressiveChunkCulling", false);

        BUILDER.pop();

        BUILDER.comment("Elliptical Render Distance Optimization")
               .push("render_distance");

        ENABLE_ELLIPTICAL_RENDER_DISTANCE = BUILDER
                .comment("Enable elliptical render distance optimization (Recommended: true)",
                        "Renders chunks in an elliptical shape instead of square/cylinder",
                        "This reduces chunk sections by 10-35% for better FPS")
                .define("enableEllipticalRenderDistance", true);

        VERTICAL_RENDER_STRETCH = BUILDER
                .comment("Vertical stretch factor for render distance (Higher = taller render ellipsoid)",
                        "Controls how far up/down chunks are rendered relative to horizontal distance",
                        "Lower values = better performance, Higher values = see more vertically")
                .defineInRange("verticalRenderStretch", 0.75, 0.1, 3.0);

        HORIZONTAL_RENDER_STRETCH = BUILDER
                .comment("Horizontal stretch factor for render distance (Higher = wider render ellipsoid)",
                        "Controls horizontal rendering extent relative to configured render distance",
                        "Values > 1.0 extend render distance horizontally")
                .defineInRange("horizontalRenderStretch", 1.0, 0.5, 2.0);

        SHOW_CULLED_CHUNKS_DEBUG = BUILDER
                .comment("Show debug overlay with number of culled chunk sections (F3 debug screen)")
                .define("showCulledChunksDebug", false);

        BUILDER.pop();

        BUILDER.comment("Entity Culling Optimizations")
               .push("entity_culling");

        ENABLE_ENTITY_CULLING = BUILDER
                .comment("Enable entity culling optimizations (Recommended: true)")
                .define("enableEntityCulling", true);

        ENTITY_CULLING_DISTANCE = BUILDER
                .comment("Distance at which entities are culled when not visible (in blocks)")
                .defineInRange("entityCullingDistance", 64, 16, 256);

        CULL_ENTITIES_BEHIND_WALLS = BUILDER
                .comment("Skip rendering entities that are completely behind walls")
                .define("cullEntitiesBehindWalls", true);

        BUILDER.pop();

        BUILDER.comment("Particle System Optimizations")
               .push("particles");

        ENABLE_PARTICLE_OPTIMIZATIONS = BUILDER
                .comment("Enable particle system optimizations (Recommended: true)")
                .define("enableParticleOptimizations", true);

        MAX_PARTICLES_PER_FRAME = BUILDER
                .comment("Maximum particles to spawn per frame (Lower = better FPS)")
                .defineInRange("maxParticlesPerFrame", 500, 100, 4000);

        PARTICLE_SPAWN_REDUCTION = BUILDER
                .comment("Reduce particle spawn rate by this factor (0.0 = no reduction, 0.5 = 50% fewer particles)")
                .defineInRange("particleSpawnReduction", 0.25, 0.0, 0.9);

        ENABLE_PARTICLE_CULLING = BUILDER
                .comment("Enable occlusion-based particle culling (Recommended: true)")
                .define("enableParticleCulling", true);

        PARTICLE_OCCLUSION_CHECK_INTERVAL = BUILDER
                .comment("How many render calls to wait before re-checking if a particle is occluded")
                .defineInRange("particleOcclusionCheckInterval", 3, 1, 10);

        PARTICLE_CULLING_RANGE = BUILDER
                .comment(
                        "Maximum distance (in blocks) to run occlusion checks.",
                        "Particles beyond this range skip occlusion tests to reduce overhead."
                )
                .defineInRange("particleCullingRange", 48.0, 8.0, 160.0);

        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
