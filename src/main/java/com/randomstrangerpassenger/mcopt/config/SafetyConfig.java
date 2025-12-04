package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

/**
 * Configuration options for safety features and entity management.
 * Includes action guard, clear lag, health stability, and attribute range
 * expansion.
 */
public class SafetyConfig {
        private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        public static final ModConfigSpec SPEC;

        // Safety Guard (dontDoThat-inspired)
        public static final ModConfigSpec.BooleanValue ENABLE_ACTION_GUARD;
        public static final ModConfigSpec.BooleanValue GUARD_PROTECT_TAMED_PETS;
        public static final ModConfigSpec.BooleanValue GUARD_PROTECT_VILLAGERS;
        public static final ModConfigSpec.BooleanValue GUARD_PROTECT_DECORATIONS;
        public static final ModConfigSpec.BooleanValue GUARD_ALLOW_SNEAK_BYPASS;

        // Clear Lag
        public static final ModConfigSpec.BooleanValue ENABLE_CLEAR_LAG;
        public static final ModConfigSpec.IntValue CLEAR_LAG_INTERVAL_TICKS;
        public static final ModConfigSpec.IntValue CLEAR_LAG_WARNING_TICKS;
        public static final ModConfigSpec.BooleanValue CLEAR_LAG_REMOVE_ITEMS;
        public static final ModConfigSpec.BooleanValue CLEAR_LAG_REMOVE_XP_ORBS;
        public static final ModConfigSpec.BooleanValue CLEAR_LAG_REMOVE_PROJECTILES;
        public static final ModConfigSpec.BooleanValue CLEAR_LAG_SKIP_NAMED_ITEMS;
        public static final ModConfigSpec.ConfigValue<List<? extends String>> CLEAR_LAG_ENTITY_WHITELIST;

        // Health Stability
        public static final ModConfigSpec.BooleanValue ENABLE_MAX_HEALTH_STABILITY;

        // Attribute Range Expansion
        public static final ModConfigSpec.BooleanValue ENABLE_ATTRIBUTE_RANGE_EXPANSION;
        public static final ModConfigSpec.DoubleValue ATTRIBUTE_MAX_LIMIT;

        // Dragon Fight Stabilizer
        public static final ModConfigSpec.BooleanValue ENABLE_DRAGON_FIGHT_STABILIZER;

        // Potion Limit Fix
        public static final ModConfigSpec.BooleanValue ENABLE_POTION_LIMIT_FIX;

        // Per-Chunk Entity Limiter
        public static final ModConfigSpec.BooleanValue ENABLE_PER_CHUNK_ENTITY_LIMIT;
        public static final ModConfigSpec.IntValue MAX_ENTITIES_PER_CHUNK;
        public static final ModConfigSpec.BooleanValue LIMIT_MONSTERS;
        public static final ModConfigSpec.BooleanValue LIMIT_ANIMALS;
        public static final ModConfigSpec.BooleanValue LIMIT_ITEMS;
        public static final ModConfigSpec.BooleanValue PREVENT_SPAWN_WHEN_FULL;

        static {
                BUILDER.comment("MCOPT Safety Features and Entity Management Configuration")
                                .push("safety");

                BUILDER.comment("Accidental-grief guard inspired by dontDoThat")
                                .push("action_guard");

                ENABLE_ACTION_GUARD = BUILDER
                                .comment("우호적인 엔티티를 잘못 공격하는 것을 막는 안전장치를 활성화합니다",
                                                "dontDoThat 모드와 동일한 자리에 동작하지만 MCOPT만으로도 독립적으로 사용 가능합니다")
                                .define("enableActionGuard", true);

                GUARD_PROTECT_TAMED_PETS = BUILDER
                                .comment("길들여진 늑대/고양이/앵무새 등을 실수로 때리지 못하도록 막습니다")
                                .define("protectTamedPets", true);

                GUARD_PROTECT_VILLAGERS = BUILDER
                                .comment("주민/행상인/좀비 주민을 공격하려고 할 때 한 번 더 확인합니다")
                                .define("protectVillagers", true);

                GUARD_PROTECT_DECORATIONS = BUILDER
                                .comment("아이템 액자, 그림 등 장식용 행잉 엔티티를 한 번에 부수지 못하도록 방지합니다")
                                .define("protectDecorations", true);

                GUARD_ALLOW_SNEAK_BYPASS = BUILDER
                                .comment("웅크린 상태로 공격하면 보호된 엔티티에도 피해를 줄 수 있도록 예외를 둡니다")
                                .define("allowSneakBypass", true);

                BUILDER.pop();

                BUILDER.comment("Custom clear-lag scheduler")
                                .push("clear_lag");

                ENABLE_CLEAR_LAG = BUILDER
                                .comment("지상에 남은 아이템/투사체/경험치를 주기적으로 정리해 서버 부하를 줄입니다.",
                                                "기본적으로 비활성화되어 있으며, 필요한 경우에만 켭니다.")
                                .define("enableClearLag", false);

                CLEAR_LAG_INTERVAL_TICKS = BUILDER
                                .comment("정리 주기 (틱 단위). 20틱 = 1초",
                                                "값이 낮을수록 자주 정리하지만 빈번한 메시지로 인해 체감 부하가 있을 수 있습니다.")
                                .defineInRange("clearLagIntervalTicks", 6_000, 200, 72_000);

                CLEAR_LAG_WARNING_TICKS = BUILDER
                                .comment("정리 직전 경고를 보낼 남은 시간 (틱). 0으로 두면 경고를 보내지 않습니다.")
                                .defineInRange("clearLagWarningTicks", 200, 0, 6_000);

                CLEAR_LAG_REMOVE_ITEMS = BUILDER
                                .comment("바닥에 떨어진 아이템 엔티티를 제거합니다.")
                                .define("clearLagRemoveItems", true);

                CLEAR_LAG_REMOVE_XP_ORBS = BUILDER
                                .comment("경험치 오브를 제거합니다.")
                                .define("clearLagRemoveXpOrbs", true);

                CLEAR_LAG_REMOVE_PROJECTILES = BUILDER
                                .comment("화살/삼지창/눈덩이 등 투사체를 제거합니다.")
                                .define("clearLagRemoveProjectiles", true);

                CLEAR_LAG_SKIP_NAMED_ITEMS = BUILDER
                                .comment("이름표가 붙은 아이템 엔티티는 보호합니다.")
                                .define("clearLagSkipNamedItems", true);

                CLEAR_LAG_ENTITY_WHITELIST = BUILDER
                                .comment("제거 대상에서 제외할 엔티티 ID 목록 (예: minecraft:armor_stand)")
                                .define("clearLagEntityWhitelist", List.of(), entry -> entry instanceof String);

                BUILDER.pop();

                BUILDER.comment("Health stability when max health changes")
                                .push("health");

                ENABLE_MAX_HEALTH_STABILITY = BUILDER
                                .comment("Preserve player health percentage when MAX_HEALTH changes",
                                                "Prevents losing extra hearts after relogging or when temporary buffs expire")
                                .define("enableMaxHealthStability", true);

                BUILDER.pop();

                BUILDER.comment("Attribute range safety for heavily-modded equipment")
                                .push("attributes");

                ENABLE_ATTRIBUTE_RANGE_EXPANSION = BUILDER
                                .comment("Allow attribute values to exceed vanilla caps (1024) without being clamped",
                                                "Prevents modded items from losing stats when max health, attack damage, or other attributes go high",
                                                "Keep enabled unless another mod intentionally enforces lower maximums")
                                .define("enableAttributeRangeExpansion", true);

                ATTRIBUTE_MAX_LIMIT = BUILDER
                                .comment("Maximum value used when clamping RangedAttributes",
                                                "Defaults to 1,000,000,000 to cover extreme modded gear while avoiding overflow",
                                                "Set lower if another mod expects tighter bounds")
                                .defineInRange("attributeMaxLimit", 1_000_000_000D, 1.0D, Double.MAX_VALUE);

                BUILDER.pop();

                BUILDER.comment("Dragon Fight stability fixes")
                                .push("dragon_fight");

                ENABLE_DRAGON_FIGHT_STABILIZER = BUILDER
                                .comment("엔드 드래곤 리스폰 도중 서버 재시작 시 리스폰이 중단되는 버그를 수정합니다",
                                                "Fixes End Dragon respawn getting stuck after server restart",
                                                "드래곤 리스폰 상태를 정확히 저장하고 복구합니다")
                                .define("enableDragonFightStabilizer", true);

                BUILDER.pop();

                BUILDER.comment("Potion effect amplifier overflow fix")
                                .push("potion_fix");

                ENABLE_POTION_LIMIT_FIX = BUILDER
                                .comment("포션 효과 레벨(Amplifier)이 byte 범위(127)를 초과해도 정상 작동하도록 수정합니다",
                                                "바닐라 Minecraft는 내부적으로 int를 사용하지만 NBT 저장 시 byte로 변환되어 128+ 레벨에서 오버플로우 발생",
                                                "예: Haste 128레벨이 역효과를 내거나, Levitation이 중력을 증가시키는 버그 방지",
                                                "모드로 극한 포션 효과를 사용하는 경우 활성화 권장")
                                .define("enablePotionLimitFix", true);

                BUILDER.pop();

                BUILDER.comment("Per-chunk entity limiting to prevent localized lag")
                                .push("per_chunk_entity_limit");

                ENABLE_PER_CHUNK_ENTITY_LIMIT = BUILDER
                                .comment("특정 청크에 엔티티가 과도하게 밀집되는 것을 방지합니다",
                                                "몹 타워나 자동화 공장 등에서 발생하는 국지적인 렉을 줄입니다",
                                                "Clear Lag와 달리 특정 청크만 실시간으로 보호합니다")
                                .define("enablePerChunkEntityLimit", false);

                MAX_ENTITIES_PER_CHUNK = BUILDER
                                .comment("청크 당 최대 엔티티 수 (초과 시 오래된 엔티티부터 제거)",
                                                "권장값: 50-100 (너무 낮으면 몹 농장이 작동하지 않을 수 있음)")
                                .defineInRange("maxEntitiesPerChunk", 50, 10, 500);

                LIMIT_MONSTERS = BUILDER
                                .comment("몬스터(좀비, 크리퍼, 스켈레톤 등)를 제한 대상에 포함")
                                .define("limitMonsters", true);

                LIMIT_ANIMALS = BUILDER
                                .comment("동물(소, 돼지, 닭 등)을 제한 대상에 포함")
                                .define("limitAnimals", true);

                LIMIT_ITEMS = BUILDER
                                .comment("아이템 엔티티를 제한 대상에 포함")
                                .define("limitItems", true);

                PREVENT_SPAWN_WHEN_FULL = BUILDER
                                .comment("청크가 꽉 찼을 때 새로운 스폰을 막습니다 (제거 대신 예방)",
                                                "true: 스폰 차단 (더 부드럽지만 몹 농장 효율 저하 가능)",
                                                "false: 초과분 제거 (더 공격적)")
                                .define("preventSpawnWhenFull", false);

                BUILDER.pop();

                BUILDER.pop();

                SPEC = BUILDER.build();
        }
}
