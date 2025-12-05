package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for rendering optimizations.
 * Includes chunk rendering, render distance, entity culling, and particle
 * systems.
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

        // Smart Leaves Settings
        public static final ModConfigSpec.BooleanValue ENABLE_SMART_LEAVES;
        public static final ModConfigSpec.IntValue LEAVES_CULLING_DEPTH;

        // Block Entity Culling Settings
        public static final ModConfigSpec.BooleanValue ENABLE_BLOCK_ENTITY_CULLING;
        public static final ModConfigSpec.IntValue BLOCK_ENTITY_CULLING_DISTANCE;
        public static final ModConfigSpec.BooleanValue CULL_BLOCK_ENTITIES_BEHIND_WALLS;

        // Smart Animation Settings
        public static final ModConfigSpec.BooleanValue ENABLE_SMART_ANIMATIONS;
        public static final ModConfigSpec.IntValue ANIMATION_UPDATE_DISTANCE;
        public static final ModConfigSpec.IntValue OFFSCREEN_ANIMATION_INTERVAL;

        // Visual Item Merging Settings
        public static final ModConfigSpec.BooleanValue ENABLE_VISUAL_ITEM_MERGING;
        public static final ModConfigSpec.DoubleValue ITEM_MERGE_RADIUS;
        public static final ModConfigSpec.IntValue MAX_ITEMS_PER_BATCH;

        // Animation LOD Settings
        public static final ModConfigSpec.BooleanValue ENABLE_ANIMATION_LOD;
        public static final ModConfigSpec.IntValue ANIMATION_LOD_NEAR_DISTANCE;
        public static final ModConfigSpec.IntValue ANIMATION_LOD_FAR_DISTANCE;

        // Text/UI Caching Settings
        public static final ModConfigSpec.BooleanValue ENABLE_TEXT_CACHING;
        public static final ModConfigSpec.IntValue SIGN_RENDER_DISTANCE_NEAR;
        public static final ModConfigSpec.IntValue SIGN_RENDER_DISTANCE_FAR;

        // Map Texture Throttling Settings
        public static final ModConfigSpec.BooleanValue ENABLE_MAP_THROTTLING;
        public static final ModConfigSpec.IntValue MAP_UPDATES_PER_TICK;
        public static final ModConfigSpec.IntValue MAP_UPDATE_DISTANCE;

        // Adaptive Chunk Upload Settings
        public static final ModConfigSpec.BooleanValue ENABLE_ADAPTIVE_CHUNK_UPLOAD;
        public static final ModConfigSpec.IntValue BASE_CHUNK_UPLOAD_LIMIT;

        // Input Fixes
        public static final ModConfigSpec.BooleanValue ENABLE_MOUSE_INPUT_FIX;

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
                                                "Particles beyond this range skip occlusion tests to reduce overhead.")
                                .defineInRange("particleCullingRange", 48.0, 8.0, 160.0);

                BUILDER.pop();

                BUILDER.comment("Smart Leaves Optimization")
                                .push("smart_leaves");

                ENABLE_SMART_LEAVES = BUILDER
                                .comment("Enable smart leaves culling (OptiLeaves-style optimization)",
                                                "Removes rendering of inner leaf blocks that are hidden by outer leaves",
                                                "Significantly improves FPS in forest biomes without visible quality loss",
                                                "Auto-disables if cull-leaves, moreculling, optileaves, or cull-less-leaves is detected")
                                .define("enableSmartLeaves", true);

                LEAVES_CULLING_DEPTH = BUILDER
                                .comment("Minimum depth of leaves before culling is applied",
                                                "Higher values = more aggressive culling but may make trees look hollow",
                                                "0 = cull all adjacent same-type leaves (most aggressive)",
                                                "2 = only cull leaves 2+ blocks deep from surface (recommended, Cull Less Leaves style)",
                                                "Set to 0 for maximum performance in dense forests")
                                .defineInRange("leavesCullingDepth", 2, 0, 5);

                BUILDER.pop();

                BUILDER.comment("Block Entity Culling Optimization")
                                .push("block_entity_culling");

                ENABLE_BLOCK_ENTITY_CULLING = BUILDER
                                .comment("Enable block entity culling (Recommended: true)",
                                                "Skips rendering block entities (chests, signs, heads, etc.) that are not visible",
                                                "Major FPS improvement in warehouses and storage rooms")
                                .define("enableBlockEntityCulling", true);

                BLOCK_ENTITY_CULLING_DISTANCE = BUILDER
                                .comment("Distance at which block entities are culled when not visible (in blocks)",
                                                "Smaller values = better performance, larger values = see block entities from farther")
                                .defineInRange("blockEntityCullingDistance", 64, 16, 256);

                CULL_BLOCK_ENTITIES_BEHIND_WALLS = BUILDER
                                .comment("Skip rendering block entities that are behind walls",
                                                "Helps with large storage rooms where most chests are hidden")
                                .define("cullBlockEntitiesBehindWalls", true);

                BUILDER.pop();

                BUILDER.comment("Smart Texture Animation Optimization",
                                "Skips animation updates for textures not visible on screen")
                                .push("smart_animations");

                ENABLE_SMART_ANIMATIONS = BUILDER
                                .comment("Enable smart animated texture optimization (Recommended: true)",
                                                "Pauses animation updates for water, lava, portals, etc. when not visible",
                                                "Preserves vanilla visuals while reducing CPU overhead")
                                .define("enableSmartAnimations", true);

                ANIMATION_UPDATE_DISTANCE = BUILDER
                                .comment("Maximum distance (in blocks) to update animated textures",
                                                "Animations beyond this distance will update less frequently")
                                .defineInRange("animationUpdateDistance", 32, 8, 128);

                OFFSCREEN_ANIMATION_INTERVAL = BUILDER
                                .comment("Tick interval for updating off-screen animations",
                                                "Higher values = more performance, but animations may appear to 'skip' when coming into view",
                                                "4 = update every 4 ticks (5 times/sec), 10 = update every 10 ticks (2 times/sec)")
                                .defineInRange("offscreenAnimationInterval", 8, 2, 20);

                BUILDER.pop();

                // Visual Item Merging settings
                BUILDER.comment("Visual Item Merging settings - Batch rendering of nearby item entities")
                                .push("visual_item_merging");

                ENABLE_VISUAL_ITEM_MERGING = BUILDER
                                .comment("Enable visual item merging optimization (Recommended: true)",
                                                "Renders nearby item entities as a single batch for better performance",
                                                "Server logic remains unchanged - only affects visual rendering")
                                .define("enableVisualItemMerging", true);

                ITEM_MERGE_RADIUS = BUILDER
                                .comment("Radius (in blocks) to merge nearby items for rendering",
                                                "Items within this radius will be rendered as a group")
                                .defineInRange("itemMergeRadius", 1.5, 0.5, 4.0);

                MAX_ITEMS_PER_BATCH = BUILDER
                                .comment("Maximum number of items to render in a single batch",
                                                "Higher = more aggressive batching, may affect visual accuracy")
                                .defineInRange("maxItemsPerBatch", 8, 2, 32);

                BUILDER.pop();

                // Animation LOD settings
                BUILDER.comment("Animation LOD (Level of Detail) - Optimize entity animations by distance")
                                .push("animation_lod");

                ENABLE_ANIMATION_LOD = BUILDER
                                .comment("Enable animation LOD optimization (Recommended: true)",
                                                "Reduces animation update frequency for distant entities",
                                                "Saves CPU cycles on skeletal bone matrix calculations")
                                .define("enableAnimationLod", true);

                ANIMATION_LOD_NEAR_DISTANCE = BUILDER
                                .comment("Distance (in blocks) for full animation updates",
                                                "Entities closer than this always get full animation")
                                .defineInRange("nearDistance", 16, 8, 64);

                ANIMATION_LOD_FAR_DISTANCE = BUILDER
                                .comment("Distance (in blocks) beyond which entities use static idle pose",
                                                "Between near and far distance, animations update every 2-3 frames")
                                .defineInRange("farDistance", 32, 16, 128);

                BUILDER.pop();

                // Text/UI Caching settings
                BUILDER.comment("Text/UI Caching - Reduce font rendering overhead")
                                .push("text_caching");

                ENABLE_TEXT_CACHING = BUILDER
                                .comment("Enable text/UI caching optimization (Recommended: true)",
                                                "Caches sign text and skips rendering for distant signs",
                                                "Sodium does not cover text/UI rendering")
                                .define("enableTextCaching", true);

                SIGN_RENDER_DISTANCE_NEAR = BUILDER
                                .comment("Distance (in blocks) for full sign text rendering")
                                .defineInRange("signNearDistance", 16, 4, 64);

                SIGN_RENDER_DISTANCE_FAR = BUILDER
                                .comment("Distance (in blocks) beyond which sign text is skipped entirely")
                                .defineInRange("signFarDistance", 32, 8, 128);

                BUILDER.pop();

                // Map Texture Throttling settings
                BUILDER.comment("Map Texture Throttling - Limit map texture updates")
                                .push("map_throttling");

                ENABLE_MAP_THROTTLING = BUILDER
                                .comment("Enable map texture update throttling (Recommended: true)",
                                                "Limits how many maps can update per tick")
                                .define("enableMapThrottling", true);

                MAP_UPDATES_PER_TICK = BUILDER
                                .comment("Maximum map texture updates per tick")
                                .defineInRange("updatesPerTick", 4, 1, 16);

                MAP_UPDATE_DISTANCE = BUILDER
                                .comment("Distance (in blocks) beyond which maps are deprioritized")
                                .defineInRange("updateDistance", 32, 8, 128);

                BUILDER.pop();

                // Adaptive Chunk Upload settings
                BUILDER.comment("Adaptive Chunk Upload - Frame-based chunk upload limiting")
                                .push("adaptive_chunk_upload");

                ENABLE_ADAPTIVE_CHUNK_UPLOAD = BUILDER
                                .comment("Enable adaptive chunk upload limiting (Recommended: true)",
                                                "Uses frame counter instead of milliseconds for better precision")
                                .define("enableAdaptiveChunkUpload", true);

                BASE_CHUNK_UPLOAD_LIMIT = BUILDER
                                .comment("Base limit for chunk uploads per frame")
                                .defineInRange("baseUploadLimit", 4, 1, 16);

                BUILDER.pop();

                BUILDER.comment("Input handling fixes")
                                .push("input_fixes");

                ENABLE_MOUSE_INPUT_FIX = BUILDER
                                .comment("마우스 버튼을 인벤토리 키(버리기, 메인/오프핸드 교환 등)에 매핑했을 때 작동하도록 합니다",
                                                "Allow mouse buttons mapped to inventory keys to work properly",
                                                "바닐라에서는 마우스 버튼 매핑이 인벤토리 화면에서 무시됩니다")
                                .define("enableMouseInputFix", true);

                BUILDER.pop();
                BUILDER.pop();

                SPEC = BUILDER.build();
        }
}
