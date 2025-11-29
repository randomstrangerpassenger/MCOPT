package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MCOPTConfig {
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

    // Memory Management
    public static final ModConfigSpec.BooleanValue ENABLE_MEMORY_OPTIMIZATIONS;
    public static final ModConfigSpec.BooleanValue AGGRESSIVE_GC_PREVENTION;

    static {
        BUILDER.comment("MCOPT Client-Side Performance Configuration")
               .push("general");

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

        BUILDER.pop();

        BUILDER.comment("Memory Management Optimizations")
               .push("memory");

        ENABLE_MEMORY_OPTIMIZATIONS = BUILDER
                .comment("Enable memory management optimizations (Recommended: true)")
                .define("enableMemoryOptimizations", true);

        AGGRESSIVE_GC_PREVENTION = BUILDER
                .comment("Prevent garbage collection during critical rendering phases")
                .define("aggressiveGCPrevention", true);

        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
