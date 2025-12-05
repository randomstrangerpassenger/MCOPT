package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for sound optimizations.
 * <p>
 * Includes volume-based culling and duplicate sound limiting to improve
 * performance without affecting audible experience.
 * </p>
 */
public class SoundConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Sound Culling Settings
    public static final ModConfigSpec.BooleanValue ENABLE_SOUND_OPTIMIZATIONS;
    public static final ModConfigSpec.DoubleValue MINIMUM_AUDIBLE_VOLUME;
    public static final ModConfigSpec.IntValue MAX_DUPLICATE_SOUNDS;
    public static final ModConfigSpec.DoubleValue DUPLICATE_SOUND_RADIUS;
    public static final ModConfigSpec.IntValue DUPLICATE_CHECK_INTERVAL_TICKS;

    static {
        BUILDER.comment("MCOPT Sound Optimizations Configuration")
                .push("sound");

        BUILDER.comment("Volume-Based Sound Culling")
                .push("volume_culling");

        ENABLE_SOUND_OPTIMIZATIONS = BUILDER
                .comment("Enable sound optimization features (Recommended: true)",
                        "Reduces sound processing overhead without affecting audible experience")
                .define("enableSoundOptimizations", true);

        MINIMUM_AUDIBLE_VOLUME = BUILDER
                .comment("Minimum volume threshold for sound playback (0.0 - 1.0)",
                        "Sounds with volume below this will not be played",
                        "0.01 = 1% volume, practically inaudible",
                        "Lower values = more sounds processed, higher values = more culling")
                .defineInRange("minimumAudibleVolume", 0.01, 0.001, 0.1);

        BUILDER.pop();

        BUILDER.comment("Duplicate Sound Limiting",
                "Prevents performance issues from many identical sounds playing simultaneously",
                "e.g., 100 cows mooing at once")
                .push("duplicate_limiting");

        MAX_DUPLICATE_SOUNDS = BUILDER
                .comment("Maximum number of identical sounds to play simultaneously",
                        "When exceeded, only the closest sounds are played",
                        "3-4 is optimal for realistic audio without performance impact")
                .defineInRange("maxDuplicateSounds", 4, 1, 16);

        DUPLICATE_SOUND_RADIUS = BUILDER
                .comment("Radius (in blocks) to check for duplicate sounds",
                        "Larger radius = more aggressive duplicate detection")
                .defineInRange("duplicateSoundRadius", 16.0, 4.0, 64.0);

        DUPLICATE_CHECK_INTERVAL_TICKS = BUILDER
                .comment("How often to clean up duplicate sound tracking (in ticks)",
                        "20 ticks = 1 second")
                .defineInRange("duplicateCheckIntervalTicks", 10, 5, 40);

        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
