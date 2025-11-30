package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for performance optimizations.
 * Includes dynamic FPS, memory management, and AI optimizations.
 */
public class PerformanceConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Dynamic FPS Settings
    public static final ModConfigSpec.BooleanValue ENABLE_DYNAMIC_FPS;
    public static final ModConfigSpec.BooleanValue ENABLE_BACKGROUND_THROTTLING;
    public static final ModConfigSpec.IntValue MENU_FRAME_RATE_LIMIT;
    public static final ModConfigSpec.IntValue UNFOCUSED_FRAME_RATE_LIMIT;
    public static final ModConfigSpec.IntValue MINIMIZED_FRAME_RATE_LIMIT;
    public static final ModConfigSpec.BooleanValue ENABLE_IDLE_BOOST;
    public static final ModConfigSpec.IntValue IDLE_BOOST_INACTIVITY_SECONDS;
    public static final ModConfigSpec.IntValue IDLE_FRAME_RATE_LIMIT;

    // Memory Management
    public static final ModConfigSpec.BooleanValue ENABLE_MEMORY_OPTIMIZATIONS;
    public static final ModConfigSpec.BooleanValue AGGRESSIVE_GC_PREVENTION;
    public static final ModConfigSpec.BooleanValue ENABLE_OBJECT_POOLING;
    public static final ModConfigSpec.BooleanValue ENABLE_RESOURCE_CLEANUP;
    public static final ModConfigSpec.BooleanValue SHOW_MEMORY_HUD;
    public static final ModConfigSpec.BooleanValue ENABLE_LEAK_GUARD;
    public static final ModConfigSpec.BooleanValue LEAK_SAFE_CLEANUP;
    public static final ModConfigSpec.IntValue LEAK_CHECK_DELAY_TICKS;
    public static final ModConfigSpec.IntValue LEAK_MEMORY_ALERT_MB;
    public static final ModConfigSpec.BooleanValue LEAK_GC_NUDGE;
    public static final ModConfigSpec.IntValue LEAK_WARNING_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue LEAK_MEMORY_ALERT_COOLDOWN_SECONDS;

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

    static {
        BUILDER.comment("MCOPT Performance Optimizations Configuration")
               .push("performance");

        BUILDER.comment("Dynamic FPS controller")
               .push("dynamic_fps");

        ENABLE_DYNAMIC_FPS = BUILDER
                .comment("Enable adaptive FPS limits when the game is in the background or showing menus")
                .define("enableDynamicFps", true);

        ENABLE_BACKGROUND_THROTTLING = BUILDER
                .comment("Lower FPS caps when the window is unfocused or minimized",
                        "Disable to keep full-speed rendering in the background (useful for capture/recording)")
                .define("enableBackgroundThrottling", true);

        MENU_FRAME_RATE_LIMIT = BUILDER
                .comment("FPS limit while any menu or pause screen is open")
                .defineInRange("menuFrameRateLimit", 30, 0, 240);

        UNFOCUSED_FRAME_RATE_LIMIT = BUILDER
                .comment("FPS limit while the Minecraft window is unfocused")
                .defineInRange("unfocusedFrameRateLimit", 15, 0, 240);

        MINIMIZED_FRAME_RATE_LIMIT = BUILDER
                .comment("FPS limit while the Minecraft window is minimized")
                .defineInRange("minimizedFrameRateLimit", 1, 0, 60);

        ENABLE_IDLE_BOOST = BUILDER
                .comment("게임 플레이 중 입력이 없을 때 프레임레이트를 낮춰 자원 사용을 줄입니다")
                .define("enableIdleBoost", true);

        IDLE_BOOST_INACTIVITY_SECONDS = BUILDER
                .comment("얼마 동안 입력이 없으면 유휴 상태로 간주할지 설정합니다")
                .defineInRange("idleInactivitySeconds", 20, 5, 120);

        IDLE_FRAME_RATE_LIMIT = BUILDER
                .comment("유휴 상태로 전환되었을 때 적용할 FPS 제한 (0은 무제한)")
                .defineInRange("idleFrameRateLimit", 10, 0, 120);

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

        LEAK_SAFE_CLEANUP = BUILDER
                .comment("Attempt gentle cache cleanup only when client threads are idle",
                        "Prevents aggressive sweeps that can crash active threads")
                .define("leakSafeCleanup", true);

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
