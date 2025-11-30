package com.randomstrangerpassenger.mcopt.util;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Centralized feature toggle evaluator.
 * <p>
 * Resolves whether a feature should be active by combining user configuration
 * with compatibility checks against other mods.
 * <p>
 * Thread-safe: uses AtomicBoolean for all toggle states to ensure visibility
 * across threads without synchronization overhead on reads.
 */
public final class FeatureToggles {

    private static final AtomicBoolean xpOrbMergingEnabled = new AtomicBoolean(false);
    private static final AtomicBoolean aiOptimizationsEnabled = new AtomicBoolean(false);
    private static final AtomicBoolean leakGuardEnabled = new AtomicBoolean(false);
    private static final AtomicBoolean dynamicFpsEnabled = new AtomicBoolean(false);
    private static final AtomicBoolean betterSnowLogicEnabled = new AtomicBoolean(false);
    private static final AtomicBoolean actionGuardEnabled = new AtomicBoolean(false);

    private FeatureToggles() {
    }

    public static void refreshFromConfig() {
        ModList modList = ModList.get();

        boolean clumpsLoaded = modList.isLoaded("clumps");
        xpOrbMergingEnabled.set(MCOPTConfig.ENABLE_XP_ORB_MERGING.get() && !clumpsLoaded);
        if (clumpsLoaded && MCOPTConfig.ENABLE_XP_ORB_MERGING.get()) {
            MCOPT.LOGGER.info("Clumps가 감지되었습니다. 충돌 방지를 위해 내장된 경험치 최적화 기능을 비활성화합니다.");
        }

        boolean aiImprovementsLoaded = modList.isLoaded("aiimprovements");
        aiOptimizationsEnabled.set(MCOPTConfig.ENABLE_AI_OPTIMIZATIONS.get() && !aiImprovementsLoaded);
        if (aiImprovementsLoaded && MCOPTConfig.ENABLE_AI_OPTIMIZATIONS.get()) {
            MCOPT.LOGGER.info("AI-Improvements가 감지되었습니다. 중복 적용을 피하기 위해 AI 최적화 기능을 비활성화합니다.");
        }

        boolean leakModLoaded = modList.isLoaded("alltheleaks") || modList.isLoaded("memoryleakfix");
        leakGuardEnabled.set(MCOPTConfig.ENABLE_LEAK_GUARD.get() && !leakModLoaded);
        if (leakModLoaded && MCOPTConfig.ENABLE_LEAK_GUARD.get()) {
            MCOPT.LOGGER.info("AllTheLeaks/MemoryLeakFix 모드가 감지되었습니다. 충돌을 피하기 위해 Leak Guard를 비활성화합니다.");
        }

        boolean dynamicFpsModLoaded = modList.isLoaded("dynamic_fps") || modList.isLoaded("fps_reducer");
        dynamicFpsEnabled.set(MCOPTConfig.ENABLE_DYNAMIC_FPS.get() && !dynamicFpsModLoaded);
        if (dynamicFpsModLoaded && MCOPTConfig.ENABLE_DYNAMIC_FPS.get()) {
            MCOPT.LOGGER.info("Dynamic FPS/FPS Reducer 모드가 감지되었습니다. 내장 동적 FPS 컨트롤러를 비활성화합니다.");
        }

        betterSnowLogicEnabled.set(MCOPTConfig.ENABLE_SNOW_ACCUMULATION_FIX.get()
                && MCOPTConfig.ENABLE_BETTER_SNOW_LOGIC.get());

        boolean dontDoThatLoaded = modList.isLoaded("dontdothat");
        actionGuardEnabled.set(MCOPTConfig.ENABLE_ACTION_GUARD.get() && !dontDoThatLoaded);
        if (dontDoThatLoaded && MCOPTConfig.ENABLE_ACTION_GUARD.get()) {
            MCOPT.LOGGER.info("dontDoThat 모드가 감지되었습니다. 중복 적용을 피하기 위해 MCOPT 액션 가드를 비활성화합니다.");
        }
    }

    public static boolean isXpOrbMergingEnabled() {
        return xpOrbMergingEnabled.get();
    }

    public static boolean isAiOptimizationsEnabled() {
        return aiOptimizationsEnabled.get();
    }

    public static boolean isLeakGuardEnabled() {
        return leakGuardEnabled.get();
    }

    public static boolean isDynamicFpsEnabled() {
        return dynamicFpsEnabled.get();
    }

    public static boolean isBetterSnowLogicEnabled() {
        return betterSnowLogicEnabled.get();
    }

    public static boolean isActionGuardEnabled() {
        return actionGuardEnabled.get();
    }

    public static void onModConfigReloaded(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getModId().equals(MCOPT.MOD_ID)) {
            refreshFromConfig();
        }
    }
}
