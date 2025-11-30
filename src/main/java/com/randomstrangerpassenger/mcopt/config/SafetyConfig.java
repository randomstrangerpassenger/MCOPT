package com.randomstrangerpassenger.mcopt.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

/**
 * Configuration options for safety features and entity management.
 * Includes action guard, clear lag, health stability, and attribute range expansion.
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
                .defineListAllowEmpty("clearLagEntityWhitelist", List::of, entry -> entry instanceof String);

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
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
