package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MCOPTConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Chunk Optimization Settings
    public static final ModConfigSpec.BooleanValue ENABLE_CHUNK_OPTIMIZATIONS;
    public static final ModConfigSpec.IntValue CHUNK_UPDATE_LIMIT;
    public static final ModConfigSpec.BooleanValue AGGRESSIVE_CHUNK_CULLING;

    // Dynamic FPS Settings
    public static final ModConfigSpec.BooleanValue ENABLE_DYNAMIC_FPS;
    public static final ModConfigSpec.IntValue MENU_FRAME_RATE_LIMIT;
    public static final ModConfigSpec.IntValue UNFOCUSED_FRAME_RATE_LIMIT;
    public static final ModConfigSpec.IntValue MINIMIZED_FRAME_RATE_LIMIT;

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
    public static final ModConfigSpec.BooleanValue ENABLE_OBJECT_POOLING;
    public static final ModConfigSpec.BooleanValue ENABLE_RESOURCE_CLEANUP;
    public static final ModConfigSpec.BooleanValue SHOW_MEMORY_HUD;
    public static final ModConfigSpec.BooleanValue ENABLE_LEAK_GUARD;
    public static final ModConfigSpec.IntValue LEAK_CHECK_DELAY_TICKS;
    public static final ModConfigSpec.IntValue LEAK_MEMORY_ALERT_MB;
    public static final ModConfigSpec.BooleanValue LEAK_GC_NUDGE;
    public static final ModConfigSpec.IntValue LEAK_WARNING_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue LEAK_MEMORY_ALERT_COOLDOWN_SECONDS;

    // Health Stability
    public static final ModConfigSpec.BooleanValue ENABLE_MAX_HEALTH_STABILITY;

    // Experience Orb Merging Settings
    public static final ModConfigSpec.BooleanValue ENABLE_XP_ORB_MERGING;
    public static final ModConfigSpec.DoubleValue XP_ORB_MERGE_RADIUS;
    public static final ModConfigSpec.IntValue XP_ORB_MERGE_DELAY;

    // AI Optimization Settings
    public static final ModConfigSpec.BooleanValue ENABLE_AI_OPTIMIZATIONS;
    public static final ModConfigSpec.BooleanValue ENABLE_MATH_CACHE;
    public static final ModConfigSpec.BooleanValue ENABLE_OPTIMIZED_LOOK_CONTROL;

    // AI Goal Removal - Common Settings
    public static final ModConfigSpec.BooleanValue REMOVE_LOOK_AT_PLAYER;
    public static final ModConfigSpec.BooleanValue REMOVE_RANDOM_LOOK_AROUND;

    // AI Goal Removal - Animals (Cow, Pig, Chicken, Sheep)
    public static final ModConfigSpec.BooleanValue REMOVE_ANIMAL_FLOAT;
    public static final ModConfigSpec.BooleanValue REMOVE_ANIMAL_PANIC;
    public static final ModConfigSpec.BooleanValue REMOVE_ANIMAL_BREED;
    public static final ModConfigSpec.BooleanValue REMOVE_ANIMAL_TEMPT;
    public static final ModConfigSpec.BooleanValue REMOVE_ANIMAL_FOLLOW_PARENT;
    public static final ModConfigSpec.BooleanValue REMOVE_ANIMAL_STROLL;

    // AI Goal Removal - Sheep Specific
    public static final ModConfigSpec.BooleanValue REMOVE_SHEEP_EAT_BLOCK;

    // AI Goal Removal - Aquatic Mobs (Fish, Squid)
    public static final ModConfigSpec.BooleanValue REMOVE_FISH_SWIM;
    public static final ModConfigSpec.BooleanValue REMOVE_FISH_PANIC;
    public static final ModConfigSpec.BooleanValue REMOVE_SQUID_RANDOM_MOVEMENT;
    public static final ModConfigSpec.BooleanValue REMOVE_SQUID_FLEE;

    // Weather Optimizations
    public static final ModConfigSpec.BooleanValue ENABLE_SNOW_ACCUMULATION_FIX;

    // Village Safety
    public static final ModConfigSpec.BooleanValue ENABLE_GOLEM_SPAWN_FIX;
    public static final ModConfigSpec.IntValue GOLEM_SPAWN_SEARCH_RANGE;

    // Portal Reliability
    public static final ModConfigSpec.BooleanValue ENABLE_PASSENGER_PORTAL_FIX;

    static {
        BUILDER.comment("MCOPT Client-Side Performance Configuration")
               .push("general");

        BUILDER.comment("Dynamic FPS controller")
               .push("dynamic_fps");

        ENABLE_DYNAMIC_FPS = BUILDER
                .comment("Enable adaptive FPS limits when the game is in the background or showing menus")
                .define("enableDynamicFps", true);

        MENU_FRAME_RATE_LIMIT = BUILDER
                .comment("FPS limit while any menu or pause screen is open")
                .defineInRange("menuFrameRateLimit", 30, 0, 240);

        UNFOCUSED_FRAME_RATE_LIMIT = BUILDER
                .comment("FPS limit while the Minecraft window is unfocused")
                .defineInRange("unfocusedFrameRateLimit", 15, 0, 240);

        MINIMIZED_FRAME_RATE_LIMIT = BUILDER
                .comment("FPS limit while the Minecraft window is minimized")
                .defineInRange("minimizedFrameRateLimit", 1, 0, 60);

        BUILDER.pop();

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

        ENABLE_OBJECT_POOLING = BUILDER
                .comment("Enable object pooling for Vec3 and BlockPos (Reduces GC pressure)")
                .define("enableObjectPooling", true);

        ENABLE_RESOURCE_CLEANUP = BUILDER
                .comment("Enable aggressive resource cleanup on world unload/disconnect")
                .define("enableResourceCleanup", true);

        SHOW_MEMORY_HUD = BUILDER
                .comment("Show memory usage HUD in top-left corner")
                .define("showMemoryHud", true);

        ENABLE_LEAK_GUARD = BUILDER
                .comment("Enable leak guard (AllTheLeaks-inspired) to watch for stuck client worlds")
                .define("enableLeakGuard", true);

        LEAK_CHECK_DELAY_TICKS = BUILDER
                .comment("Ticks to wait after unloading a level before reporting potential leaks")
                .defineInRange("leakCheckDelayTicks", 200, 20, 2400);

        LEAK_MEMORY_ALERT_MB = BUILDER
                .comment("Memory usage threshold (MB) that triggers leak guard warnings")
                .defineInRange("leakMemoryAlertMb", 4096, 512, 16384);

        LEAK_GC_NUDGE = BUILDER
                .comment("Attempt a single System.gc() if an old level is still referenced after the delay")
                .define("leakGcNudge", false);

        LEAK_WARNING_INTERVAL_TICKS = BUILDER
                .comment("Interval between leak guard warnings after the first alert (to avoid log spam)")
                .defineInRange("leakWarningIntervalTicks", 200, 40, 4800);

        LEAK_MEMORY_ALERT_COOLDOWN_SECONDS = BUILDER
                .comment("Minimum seconds between consecutive high-memory warnings")
                .defineInRange("leakMemoryAlertCooldownSeconds", 15, 1, 600);

        BUILDER.pop();

        BUILDER.comment("Weather & Snow Optimizations")
               .push("weather");

        ENABLE_SNOW_ACCUMULATION_FIX = BUILDER
                .comment("Skip redundant neighbor notifications when snow layers thicken during precipitation",
                        "Reduces chunk rebuild spam during heavy snowfall while keeping visuals identical")
                .define("enableSnowAccumulationFix", true);

        BUILDER.pop();

        BUILDER.comment("Village guard reliability")
               .push("villages");

        ENABLE_GOLEM_SPAWN_FIX = BUILDER
                .comment("Gently slides villager-summoned iron golems downward to the nearest safe block",
                        "Prevents golems from getting stuck on roofs or decorations when villagers trigger spawns")
                .define("enableGolemSpawnFix", true);

        GOLEM_SPAWN_SEARCH_RANGE = BUILDER
                .comment("How many blocks downward to search for a safe golem spawn surface",
                        "Smaller values stay closer to vanilla behavior; larger values help sky-platform farms")
                .defineInRange("golemSpawnSearchRange", 6, 1, 32);

        BUILDER.pop();

        BUILDER.comment("Portal behavior reliability")
               .push("portals");

        ENABLE_PASSENGER_PORTAL_FIX = BUILDER
                .comment("Carry passengers through Nether/End portals together with their vehicle",
                        "Prevents riders from being stranded when the mount or vehicle enters a portal first")
                .define("enablePassengerPortalFix", true);

        BUILDER.pop();

        BUILDER.comment("Health stability when max health changes")
               .push("health");

        ENABLE_MAX_HEALTH_STABILITY = BUILDER
                .comment("Preserve player health percentage when MAX_HEALTH changes",
                        "Prevents losing extra hearts after relogging or when temporary buffs expire")
                .define("enableMaxHealthStability", true);

        BUILDER.pop();

        BUILDER.comment("Experience Orb Merging Optimization")
               .push("xp_orb_merging");

        ENABLE_XP_ORB_MERGING = BUILDER
                .comment("Enable experience orb merging (Recommended: true)",
                        "Merges nearby experience orbs into single entities to reduce lag",
                        "Especially helpful when fighting mobs or breaking ore blocks")
                .define("enableXpOrbMerging", true);

        XP_ORB_MERGE_RADIUS = BUILDER
                .comment("Radius within which experience orbs will merge (in blocks)",
                        "Larger radius = more aggressive merging, better performance")
                .defineInRange("xpOrbMergeRadius", 1.5, 0.5, 5.0);

        XP_ORB_MERGE_DELAY = BUILDER
                .comment("How often to check for nearby orbs to merge (in ticks)",
                        "Lower = more frequent merging, Higher = less CPU usage",
                        "20 ticks = 1 second")
                .defineInRange("xpOrbMergeDelay", 10, 1, 40);

        BUILDER.pop();

        BUILDER.comment("Entity AI Optimizations")
               .push("ai_optimizations");

        ENABLE_AI_OPTIMIZATIONS = BUILDER
                .comment("Enable AI optimization system (Recommended: true)",
                        "Optimizes entity AI performance through math caching and selective AI goal removal",
                        "Based on concepts from AI-Improvements mod, but with independent implementation")
                .define("enableAiOptimizations", true);

        ENABLE_MATH_CACHE = BUILDER
                .comment("Enable math function caching (atan2, sin, cos)",
                        "Significantly improves AI rotation calculations with minimal memory cost",
                        "This was a major optimization in Minecraft 1.7-1.9, still provides small gains in 1.21+")
                .define("enableMathCache", true);

        ENABLE_OPTIMIZED_LOOK_CONTROL = BUILDER
                .comment("Replace mob LookControl with optimized version",
                        "Uses cached math functions for rotation calculations",
                        "Requires enableMathCache to be true for maximum benefit")
                .define("enableOptimizedLookControl", true);

        BUILDER.comment("Common AI Goal Removal")
               .push("common_goals");

        REMOVE_LOOK_AT_PLAYER = BUILDER
                .comment("Remove LookAtPlayerGoal from all mobs",
                        "Prevents mobs from looking at players (improves performance, affects aesthetics)")
                .define("removeLookAtPlayer", false);

        REMOVE_RANDOM_LOOK_AROUND = BUILDER
                .comment("Remove RandomLookAroundGoal from all mobs",
                        "Prevents mobs from randomly looking around (improves performance, affects aesthetics)")
                .define("removeRandomLookAround", false);

        BUILDER.pop();

        BUILDER.comment("Animal AI Goal Removal (applies to Cow, Pig, Chicken, Sheep)")
               .push("animal_goals");

        REMOVE_ANIMAL_FLOAT = BUILDER
                .comment("Remove FloatGoal from animals",
                        "WARNING: Animals may not swim properly if disabled!")
                .define("removeFloat", false);

        REMOVE_ANIMAL_PANIC = BUILDER
                .comment("Remove PanicGoal from animals",
                        "Animals won't run away when attacked (improves performance, affects gameplay)")
                .define("removePanic", false);

        REMOVE_ANIMAL_BREED = BUILDER
                .comment("Remove BreedGoal from animals",
                        "Disables animal breeding (major performance improvement if you don't breed animals)")
                .define("removeBreed", false);

        REMOVE_ANIMAL_TEMPT = BUILDER
                .comment("Remove TemptGoal from animals",
                        "Animals won't follow players holding food (improves performance)")
                .define("removeTempt", false);

        REMOVE_ANIMAL_FOLLOW_PARENT = BUILDER
                .comment("Remove FollowParentGoal from animals",
                        "Baby animals won't follow parents (improves performance)")
                .define("removeFollowParent", false);

        REMOVE_ANIMAL_STROLL = BUILDER
                .comment("Remove RandomStrollGoal from animals",
                        "Animals won't wander randomly (major performance improvement, makes animals static)")
                .define("removeStroll", false);

        BUILDER.pop();

        BUILDER.comment("Sheep-Specific AI Goal Removal")
               .push("sheep_goals");

        REMOVE_SHEEP_EAT_BLOCK = BUILDER
                .comment("Remove EatBlockGoal from sheep",
                        "Sheep won't eat grass to regrow wool (improves performance)")
                .define("removeEatBlock", false);

        BUILDER.pop();

        BUILDER.comment("Aquatic Mob AI Goal Removal (Fish and Squid)")
               .push("aquatic_goals");

        REMOVE_FISH_SWIM = BUILDER
                .comment("Remove RandomSwimmingGoal from fish",
                        "Fish won't swim randomly (improves performance in ocean biomes)")
                .define("removeFishSwim", false);

        REMOVE_FISH_PANIC = BUILDER
                .comment("Remove PanicGoal from fish",
                        "Fish won't flee when attacked (improves performance)")
                .define("removeFishPanic", false);

        REMOVE_SQUID_RANDOM_MOVEMENT = BUILDER
                .comment("Remove SquidRandomMovementGoal from squids",
                        "Squids won't move randomly (major performance improvement in ocean biomes)")
                .define("removeSquidRandomMovement", false);

        REMOVE_SQUID_FLEE = BUILDER
                .comment("Remove SquidFleeGoal from squids",
                        "Squids won't flee from players (improves performance)")
                .define("removeSquidFlee", false);

        BUILDER.pop();
        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
