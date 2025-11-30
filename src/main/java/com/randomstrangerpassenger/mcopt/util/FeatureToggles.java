package com.randomstrangerpassenger.mcopt.util;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * Centralized feature toggle evaluator with data-driven approach.
 * <p>
 * Resolves whether a feature should be active by combining user configuration
 * with compatibility checks against other mods.
 * <p>
 * Thread-safe: uses AtomicBoolean for all toggle states to ensure visibility
 * across threads without synchronization overhead on reads.
 */
public final class FeatureToggles {

    private static final Map<String, FeatureToggle> TOGGLES = new LinkedHashMap<>();

    static {
        // Register all feature toggles with their configuration and incompatible mods
        register("xpOrbMerging",
                MCOPTConfig.ENABLE_XP_ORB_MERGING::get,
                "XP Orb merging",
                "clumps");

        register("aiOptimizations",
                MCOPTConfig.ENABLE_AI_OPTIMIZATIONS::get,
                "AI optimization",
                "aiimprovements");

        register("leakGuard",
                MCOPTConfig.ENABLE_LEAK_GUARD::get,
                "Leak Guard",
                "alltheleaks", "memoryleakfix");

        register("dynamicFps",
                MCOPTConfig.ENABLE_DYNAMIC_FPS::get,
                "Dynamic FPS controller",
                "dynamic_fps", "fps_reducer");

        register("betterSnowLogic",
                () -> MCOPTConfig.ENABLE_SNOW_ACCUMULATION_FIX.get() && MCOPTConfig.ENABLE_BETTER_SNOW_LOGIC.get(),
                "Better Snow Logic");

        register("actionGuard",
                MCOPTConfig.ENABLE_ACTION_GUARD::get,
                "Action Guard",
                "dontdothat");
    }

    private FeatureToggles() {
    }

    /**
     * Register a feature toggle.
     *
     * @param key Unique identifier for the feature
     * @param configSupplier Supplier that returns the config value
     * @param displayName Human-readable name for logging
     * @param incompatibleMods Mod IDs that conflict with this feature
     */
    private static void register(String key, BooleanSupplier configSupplier, String displayName, String... incompatibleMods) {
        TOGGLES.put(key, new FeatureToggle(configSupplier, displayName, incompatibleMods));
    }

    /**
     * Refresh all feature toggles based on current configuration and loaded mods.
     * Called during initialization and when configuration is reloaded.
     */
    public static void refreshFromConfig() {
        ModList modList = ModList.get();

        for (Map.Entry<String, FeatureToggle> entry : TOGGLES.entrySet()) {
            String key = entry.getKey();
            FeatureToggle toggle = entry.getValue();

            boolean configEnabled = toggle.configSupplier.getAsBoolean();
            boolean hasIncompatibleMod = toggle.hasIncompatibleMod(modList);

            toggle.enabled.set(configEnabled && !hasIncompatibleMod);

            // Log when feature is disabled due to mod conflict
            if (configEnabled && hasIncompatibleMod) {
                String conflictingMods = String.join(", ", toggle.getLoadedIncompatibleMods(modList));
                MCOPT.LOGGER.info("{} 모드가 감지되었습니다. 충돌 방지를 위해 {}을(를) 비활성화합니다.",
                        conflictingMods, toggle.displayName);
            }
        }
    }

    // Public getters for backward compatibility

    public static boolean isXpOrbMergingEnabled() {
        return isEnabled("xpOrbMerging");
    }

    public static boolean isAiOptimizationsEnabled() {
        return isEnabled("aiOptimizations");
    }

    public static boolean isLeakGuardEnabled() {
        return isEnabled("leakGuard");
    }

    public static boolean isDynamicFpsEnabled() {
        return isEnabled("dynamicFps");
    }

    public static boolean isBetterSnowLogicEnabled() {
        return isEnabled("betterSnowLogic");
    }

    public static boolean isActionGuardEnabled() {
        return isEnabled("actionGuard");
    }

    /**
     * Check if a feature is enabled by its key.
     *
     * @param key Feature key
     * @return true if the feature is enabled, false otherwise
     */
    private static boolean isEnabled(String key) {
        FeatureToggle toggle = TOGGLES.get(key);
        return toggle != null && toggle.enabled.get();
    }

    public static void onModConfigReloaded(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getModId().equals(MCOPT.MOD_ID)) {
            refreshFromConfig();
        }
    }

    /**
     * Represents a single feature toggle with its configuration and compatibility rules.
     */
    private static class FeatureToggle {
        private final BooleanSupplier configSupplier;
        private final String displayName;
        private final Set<String> incompatibleMods;
        private final AtomicBoolean enabled;

        FeatureToggle(BooleanSupplier configSupplier, String displayName, String... incompatibleMods) {
            this.configSupplier = configSupplier;
            this.displayName = displayName;
            this.incompatibleMods = incompatibleMods.length > 0
                    ? Set.of(incompatibleMods)
                    : Collections.emptySet();
            this.enabled = new AtomicBoolean(false);
        }

        /**
         * Check if any incompatible mod is loaded.
         *
         * @param modList The mod list to check against
         * @return true if at least one incompatible mod is loaded
         */
        boolean hasIncompatibleMod(ModList modList) {
            return incompatibleMods.stream().anyMatch(modList::isLoaded);
        }

        /**
         * Get list of loaded incompatible mods.
         *
         * @param modList The mod list to check against
         * @return List of loaded incompatible mod IDs
         */
        List<String> getLoadedIncompatibleMods(ModList modList) {
            return incompatibleMods.stream()
                    .filter(modList::isLoaded)
                    .toList();
        }
    }
}
