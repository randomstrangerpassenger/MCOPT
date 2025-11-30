package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for gameplay improvements and fixes.
 * Includes XP orb merging, weather, entity behaviors, portals, and quality-of-life features.
 */
public class GameplayConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    // Experience Orb Merging Settings
    public static final ModConfigSpec.BooleanValue ENABLE_XP_ORB_MERGING;
    public static final ModConfigSpec.DoubleValue XP_ORB_MERGE_RADIUS;
    public static final ModConfigSpec.IntValue XP_ORB_MERGE_DELAY;

    // Weather Optimizations
    public static final ModConfigSpec.BooleanValue ENABLE_SNOW_ACCUMULATION_FIX;
    public static final ModConfigSpec.BooleanValue ENABLE_BETTER_SNOW_LOGIC;

    // Bee Pathfinding Stability
    public static final ModConfigSpec.BooleanValue ENABLE_BEE_STUCK_FIX;
    public static final ModConfigSpec.IntValue BEE_STUCK_TIMEOUT_TICKS;
    public static final ModConfigSpec.IntValue BEE_RELINK_COOLDOWN_TICKS;

    // Village Safety
    public static final ModConfigSpec.BooleanValue ENABLE_GOLEM_SPAWN_FIX;
    public static final ModConfigSpec.IntValue GOLEM_SPAWN_SEARCH_RANGE;

    // Portal Reliability
    public static final ModConfigSpec.BooleanValue ENABLE_PASSENGER_PORTAL_FIX;
    public static final ModConfigSpec.BooleanValue ENABLE_PORTAL_REDIRECT;

    // Bucket Introspection
    public static final ModConfigSpec.BooleanValue ENABLE_BUCKET_PREVIEW;

    // Fishing Reliability
    public static final ModConfigSpec.BooleanValue ENABLE_FISHING_ROD_FIX;

    // Enchanting Fixes
    public static final ModConfigSpec.BooleanValue FIX_ENCHANTMENT_RNG;

    static {
        BUILDER.comment("MCOPT Gameplay Improvements and Fixes Configuration")
               .push("gameplay");

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

        BUILDER.comment("Weather & Snow Optimizations")
               .push("weather");

        ENABLE_SNOW_ACCUMULATION_FIX = BUILDER
                .comment("Skip redundant neighbor notifications when snow layers thicken during precipitation",
                        "Reduces chunk rebuild spam during heavy snowfall while keeping visuals identical")
                .define("enableSnowAccumulationFix", true);

        ENABLE_BETTER_SNOW_LOGIC = BUILDER
                .comment("Enable MCOPT's improved snow accumulation logic (disable for vanilla behaviour)")
                .define("enableBetterSnowLogic", true);

        BUILDER.pop();

        BUILDER.comment("Bee pathfinding stability improvements")
               .push("bee_stability");

        ENABLE_BEE_STUCK_FIX = BUILDER
                .comment("Automatically detect and recover bees stuck trying to path to unreachable hives",
                        "Prevents bees from indefinitely attempting to reach blocked or invalid hive positions")
                .define("enableBeeStuckFix", true);

        BEE_STUCK_TIMEOUT_TICKS = BUILDER
                .comment("Number of consecutive ticks with failed pathfinding before considering the bee stuck")
                .defineInRange("beeStuckTimeoutTicks", 200, 40, 1200);

        BEE_RELINK_COOLDOWN_TICKS = BUILDER
                .comment("Cooldown period in ticks before a bee can attempt to find a new hive after clearing a stuck target")
                .defineInRange("beeRelinkCooldownTicks", 200, 0, 600);

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

        ENABLE_PORTAL_REDIRECT = BUILDER
                .comment(
                        "Remember the last used portal in each dimension and redirect returns to it to avoid unwanted portal swapping",
                        "Lightweight MCOPT implementation inspired by Redirected's reliability goal"
                )
                .define("enablePortalRedirect", true);

        BUILDER.pop();

        BUILDER.comment("Bucket content introspection")
               .push("buckets");

        ENABLE_BUCKET_PREVIEW = BUILDER
                .comment("Show detailed tooltips for buckets to reveal stored fluids or entities",
                        "Helps identify tropical fish variants, axolotl colors, and modded fluids",
                        "Purely client-side and compatible with other mods")
                .define("enableBucketPreview", true);

        BUILDER.pop();

        BUILDER.comment("Fishing rod reliability")
               .push("fishing");

        ENABLE_FISHING_ROD_FIX = BUILDER
                .comment("낚싯찌 참조가 어긋나 낚싯대가 먹통이 되는 상황을 자동으로 정리합니다.",
                        "플레이어가 차원을 이동하거나 낚싯대를 치웠을 때 남아 있는 찌를 안전하게 제거합니다.")
                .define("enableFishingRodFix", true);

        BUILDER.pop();

        BUILDER.comment("Enchanting behavior fixes")
               .push("enchanting");

        FIX_ENCHANTMENT_RNG = BUILDER
                .comment("재료나 라피스가 바뀔 때마다 마법 부여 테이블의 난수 시드를 갱신합니다",
                        "플레이어 고유 시드로 고정된 기존 방식보다 실제 무작위에 가깝게 만들어 예측을 어렵게 합니다")
                .define("fixEnchantmentRNG", true);

        BUILDER.pop();
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
