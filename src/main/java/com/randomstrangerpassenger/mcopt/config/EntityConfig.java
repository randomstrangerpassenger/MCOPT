package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for entity optimization.
 * Includes interpolation throttling and visibility-based logic skip.
 */
public class EntityConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Entity Interpolation Throttling Settings
    public static final ModConfigSpec.BooleanValue ENABLE_ENTITY_INTERPOLATION;
    public static final ModConfigSpec.IntValue NEAR_DISTANCE;
    public static final ModConfigSpec.IntValue MID_DISTANCE;
    public static final ModConfigSpec.IntValue NEAR_TICK_INTERVAL;
    public static final ModConfigSpec.IntValue MID_TICK_INTERVAL;
    public static final ModConfigSpec.IntValue FAR_TICK_INTERVAL;

    // Visibility-based Logic Skip Settings
    public static final ModConfigSpec.BooleanValue ENABLE_VISIBILITY_SKIP;
    public static final ModConfigSpec.IntValue VISIBILITY_CHECK_INTERVAL;
    public static final ModConfigSpec.IntValue VISIBILITY_CACHE_DURATION;

    static {
        BUILDER.comment("Entity optimization settings for Phase 2: Entity LOD")
                .push("entity_optimization");

        // === Entity Interpolation Throttling ===
        BUILDER.comment("Entity Interpolation Throttling - reduces tick rate for distant entities")
                .push("interpolation_throttling");

        ENABLE_ENTITY_INTERPOLATION = BUILDER
                .comment("Enable entity interpolation throttling (Recommended: true)",
                        "Reduces tick rate for distant entities while smoothly interpolating movement",
                        "AI logic is NOT affected - only visual updates are throttled")
                .define("enableEntityInterpolation", true);

        NEAR_DISTANCE = BUILDER
                .comment("Distance (in blocks) for near entities (full tick rate)")
                .defineInRange("nearDistance", 32, 8, 64);

        MID_DISTANCE = BUILDER
                .comment("Distance (in blocks) for mid-range entities (reduced tick rate)")
                .defineInRange("midDistance", 64, 32, 128);

        NEAR_TICK_INTERVAL = BUILDER
                .comment("Tick interval for near entities (1 = every tick)")
                .defineInRange("nearTickInterval", 1, 1, 4);

        MID_TICK_INTERVAL = BUILDER
                .comment("Tick interval for mid-range entities")
                .defineInRange("midTickInterval", 2, 1, 8);

        FAR_TICK_INTERVAL = BUILDER
                .comment("Tick interval for far entities")
                .defineInRange("farTickInterval", 4, 2, 16);

        BUILDER.pop();

        // === Visibility-based Logic Skip ===
        BUILDER.comment("Visibility-based Logic Skip - skips visual logic for non-visible entities")
                .push("visibility_skip");

        ENABLE_VISIBILITY_SKIP = BUILDER
                .comment("Enable visibility-based logic skip (Recommended: true)",
                        "Skips LookAtPlayer and similar visual goals for entities behind walls",
                        "AI pathfinding and combat is NOT affected")
                .define("enableVisibilitySkip", true);

        VISIBILITY_CHECK_INTERVAL = BUILDER
                .comment("How often to check entity visibility (in ticks)",
                        "Lower = more accurate but higher CPU cost")
                .defineInRange("visibilityCheckInterval", 5, 1, 20);

        VISIBILITY_CACHE_DURATION = BUILDER
                .comment("How long to cache visibility results (in ticks)")
                .defineInRange("visibilityCacheDuration", 10, 5, 40);

        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
