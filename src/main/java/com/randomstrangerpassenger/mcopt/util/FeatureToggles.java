package com.randomstrangerpassenger.mcopt.util;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Centralized feature toggle evaluator.
 * <p>
 * Resolves whether a feature should be active by combining user configuration
 * with compatibility checks against other mods.
 */
public final class FeatureToggles {

    private static boolean xpOrbMergingEnabled;
    private static boolean aiOptimizationsEnabled;
    private static boolean leakGuardEnabled;
    private static boolean dynamicFpsEnabled;
    private static boolean betterSnowLogicEnabled;
    private static boolean actionGuardEnabled;

    private FeatureToggles() {
    }

    public static void refreshFromConfig() {
        ModList modList = ModList.get();

        boolean clumpsLoaded = modList.isLoaded("clumps");
        xpOrbMergingEnabled = MCOPTConfig.ENABLE_XP_ORB_MERGING.get() && !clumpsLoaded;
        if (clumpsLoaded && MCOPTConfig.ENABLE_XP_ORB_MERGING.get()) {
            MCOPT.LOGGER.info("Clumps가 감지되었습니다. 충돌 방지를 위해 내장된 경험치 최적화 기능을 비활성화합니다.");
        }

        boolean aiImprovementsLoaded = modList.isLoaded("aiimprovements");
        aiOptimizationsEnabled = MCOPTConfig.ENABLE_AI_OPTIMIZATIONS.get() && !aiImprovementsLoaded;
        if (aiImprovementsLoaded && MCOPTConfig.ENABLE_AI_OPTIMIZATIONS.get()) {
            MCOPT.LOGGER.info("AI-Improvements가 감지되었습니다. 중복 적용을 피하기 위해 AI 최적화 기능을 비활성화합니다.");
        }

        boolean leakModLoaded = modList.isLoaded("alltheleaks") || modList.isLoaded("memoryleakfix");
        leakGuardEnabled = MCOPTConfig.ENABLE_LEAK_GUARD.get() && !leakModLoaded;
        if (leakModLoaded && MCOPTConfig.ENABLE_LEAK_GUARD.get()) {
            MCOPT.LOGGER.info("AllTheLeaks/MemoryLeakFix 모드가 감지되었습니다. 충돌을 피하기 위해 Leak Guard를 비활성화합니다.");
        }

        boolean dynamicFpsModLoaded = modList.isLoaded("dynamic_fps") || modList.isLoaded("fps_reducer");
        dynamicFpsEnabled = MCOPTConfig.ENABLE_DYNAMIC_FPS.get() && !dynamicFpsModLoaded;
        if (dynamicFpsModLoaded && MCOPTConfig.ENABLE_DYNAMIC_FPS.get()) {
            MCOPT.LOGGER.info("Dynamic FPS/FPS Reducer 모드가 감지되었습니다. 내장 동적 FPS 컨트롤러를 비활성화합니다.");
        }

        betterSnowLogicEnabled = MCOPTConfig.ENABLE_SNOW_ACCUMULATION_FIX.get()
                && MCOPTConfig.ENABLE_BETTER_SNOW_LOGIC.get();

        boolean dontDoThatLoaded = modList.isLoaded("dontdothat");
        actionGuardEnabled = MCOPTConfig.ENABLE_ACTION_GUARD.get() && !dontDoThatLoaded;
        if (dontDoThatLoaded && MCOPTConfig.ENABLE_ACTION_GUARD.get()) {
            MCOPT.LOGGER.info("dontDoThat 모드가 감지되었습니다. 중복 적용을 피하기 위해 MCOPT 액션 가드를 비활성화합니다.");
        }
    }

    public static boolean isXpOrbMergingEnabled() {
        return xpOrbMergingEnabled;
    }

    public static boolean isAiOptimizationsEnabled() {
        return aiOptimizationsEnabled;
    }

    public static boolean isLeakGuardEnabled() {
        return leakGuardEnabled;
    }

    public static boolean isDynamicFpsEnabled() {
        return dynamicFpsEnabled;
    }

    public static boolean isBetterSnowLogicEnabled() {
        return betterSnowLogicEnabled;
    }

    public static boolean isActionGuardEnabled() {
        return actionGuardEnabled;
    }

    public static void onModConfigReloaded(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getModId().equals(MCOPT.MOD_ID)) {
            refreshFromConfig();
        }
    }
}
