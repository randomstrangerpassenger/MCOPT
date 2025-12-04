package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration options for gameplay improvements and fixes.
 * Includes XP orb merging, weather, entity behaviors, portals, and
 * quality-of-life features.
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

        // Jukebox Fix
        public static final ModConfigSpec.BooleanValue ENABLE_JUKEBOX_FIX;

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

        // Item Data Sanitizer
        public static final ModConfigSpec.BooleanValue ENABLE_ITEM_NBT_SANITIZER;
        public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> ITEM_NBT_SANITIZER_BLACKLIST;

        // Damage Tilt Fix
        public static final ModConfigSpec.BooleanValue ENABLE_DAMAGE_TILT_FIX;

        // Login Timeout Fix
        public static final ModConfigSpec.BooleanValue ENABLE_LOGIN_TIMEOUT_FIX;
        public static final ModConfigSpec.IntValue LOGIN_TIMEOUT_SECONDS;

        // World Generation Fixes
        public static final ModConfigSpec.BooleanValue ENABLE_LAKE_CRASH_FIX;
        public static final ModConfigSpec.BooleanValue ENABLE_BASIN_FIX;

        // Interaction Fallthrough (RCF-style)
        public static final ModConfigSpec.BooleanValue ENABLE_RIGHT_CLICK_FALLTHROUGH;

        // Item Frame Silence Fix
        public static final ModConfigSpec.BooleanValue ENABLE_ITEM_FRAME_SILENCE;

        // Swim State Fix (MC-220390)
        public static final ModConfigSpec.BooleanValue ENABLE_SWIM_STATE_FIX;

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

                BUILDER.comment("Jukebox audio fixes")
                                .push("jukebox_fix");

                ENABLE_JUKEBOX_FIX = BUILDER
                                .comment("주크박스 재생 시 클라이언트와 서버의 데이터 불일치로 인해 음악이 끊기는 문제를 수정합니다",
                                                "리소스팩의 실제 오디오 길이를 사용하여 재생 시간을 보정합니다",
                                                "Fixes jukebox music stopping abruptly by using actual audio duration from resource packs")
                                .define("enableJukeboxFix", true);

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
                                                "Lightweight MCOPT implementation inspired by Redirected's reliability goal")
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

                BUILDER.comment("Item stack merging fixes")
                                .push("item_data_sanitizer");

                ENABLE_ITEM_NBT_SANITIZER = BUILDER
                                .comment("빈 NBT 태그를 자동으로 null로 정리하여 아이템 병합 버그를 수정합니다",
                                                "빈 태그({})를 가진 아이템과 태그가 없는 아이템이 병합되지 않는 문제를 해결합니다",
                                                "Fixes item stacking issues caused by empty NBT tags")
                                .define("enableItemNbtSanitizer", true);

                ITEM_NBT_SANITIZER_BLACKLIST = BUILDER
                                .comment("빈 NBT 태그 정리에서 제외할 아이템 ID 목록",
                                                "일부 아이템은 빈 태그가 식별자로 필요할 수 있습니다",
                                                "예: minecraft:player_head",
                                                "List of item IDs to exclude from empty NBT tag sanitization")
                                .define("itemBlacklist", java.util.List.of(),
                                                obj -> obj instanceof String);

                BUILDER.pop();

                BUILDER.comment("Damage tilt camera direction fix")
                                .push("damage_tilt");

                ENABLE_DAMAGE_TILT_FIX = BUILDER
                                .comment("피격 시 카메라 틸트 방향을 수정합니다",
                                                "유효하지 않은 방향 정보(Yaw=0 또는 NaN)를 감지하고 올바른 방향으로 수정합니다",
                                                "Fixes incorrect camera tilt direction when taking damage")
                                .define("enableDamageTiltFix", true);

                BUILDER.pop();

                BUILDER.comment("Login timeout prevention for heavy modpacks")
                                .push("login_timeout");

                ENABLE_LOGIN_TIMEOUT_FIX = BUILDER
                                .comment("무거운 모드팩에서 로그인 타임아웃을 방지합니다",
                                                "클라이언트가 서버에 접속할 때 로딩 시간이 오래 걸려 튕기는 문제를 해결합니다",
                                                "Prevents 'Timed Out' errors when joining servers with heavy modpacks")
                                .define("enableLoginTimeoutFix", true);

                LOGIN_TIMEOUT_SECONDS = BUILDER
                                .comment("로그인 핸드셰이크 타임아웃 시간 (초)",
                                                "바닐라 기본값: 30초",
                                                "권장값: 120-180초 (무거운 모드팩의 경우)",
                                                "Login handshake timeout in seconds (vanilla default: 30)")
                                .defineInRange("loginTimeoutSeconds", 120, 30, 600);

                BUILDER.pop();

                BUILDER.comment("World generation crash fixes")
                                .push("world_generation");

                ENABLE_LAKE_CRASH_FIX = BUILDER
                                .comment("커스텀 지형 생성 중 호수 기능으로 인한 크래시를 방지합니다",
                                                "로드되지 않은 청크의 바이옴을 확인할 때 발생하는 오류를 안전하게 처리합니다",
                                                "Prevents crashes caused by lake generation when checking biomes in unloaded chunks")
                                .define("enableLakeCrashFix", true);

                ENABLE_BASIN_FIX = BUILDER
                                .comment("오버월드 바이옴에 돌 디스크(Basin) 피처를 복원합니다",
                                                "Plains, Forest, Savanna 등의 바이옴에서 자연스러운 돌 지형 생성을 활성화합니다",
                                                "Restores stone disk (Basin) generation in overworld biomes like Plains, Forest, and Savanna")
                                .define("enableBasinFix", true);

                BUILDER.pop();

                BUILDER.comment("Interaction Fallthrough (RCF-style)")
                                .push("interaction_fallthrough");

                ENABLE_RIGHT_CLICK_FALLTHROUGH = BUILDER
                                .comment("오른손 아이템 사용이 실패(예: 공간 부족)했을 때, 자동으로 왼손 아이템 사용을 시도합니다",
                                                "예: 오른손에 블록이 있지만 설치할 공간이 없을 때 왼손의 블록을 사용",
                                                "When right-hand item use fails (e.g., no space to place), automatically tries using left-hand item")
                                .define("enableRightClickFallthrough", true);

                BUILDER.pop();

                BUILDER.comment("Item Frame Silence Fix (BugFixerUpper style)")
                                .push("item_frame_silence");

                ENABLE_ITEM_FRAME_SILENCE = BUILDER
                                .comment("청크 로딩/월드 생성 시 아이템 액자가 로드될 때 재생되는 불필요한 소리를 음소거합니다",
                                                "플레이어가 직접 아이템 액자를 설치할 때는 정상적으로 소리가 재생됩니다",
                                                "Silences unnecessary placement sounds when item frames are loaded during chunk loading or world generation")
                                .define("enableItemFrameSilence", true);

                BUILDER.pop();

                BUILDER.comment("Swim State Fix (MC-220390)")
                                .push("swim_state_fix");

                ENABLE_SWIM_STATE_FIX = BUILDER
                                .comment("물 속에서 수영 중 공격 시 발생하는 수영 상태 동기화 버그를 수정합니다 (MC-220390)",
                                                "수영 중 엔티티를 공격하면 서버가 수영 상태를 잘못 해제하여 히트박스 불일치가 발생하는 문제를 해결합니다",
                                                "해저 신전(Ocean Monument)에서 가디언과 싸울 때 특히 유용합니다",
                                                "Fixes swim state desync when attacking entities while swimming (MC-220390)")
                                .define("enableSwimStateFix", true);

                BUILDER.pop();
                BUILDER.pop();

                SPEC = BUILDER.build();
        }
}
