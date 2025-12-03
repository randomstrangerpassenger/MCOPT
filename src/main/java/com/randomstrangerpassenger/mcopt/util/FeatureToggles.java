package com.randomstrangerpassenger.mcopt.util;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.neoforged.fml.ModList;

import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

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

    private static final Map<FeatureKey, FeatureToggle> TOGGLES = new EnumMap<>(FeatureKey.class);

    static {
        // Register all feature toggles with their configuration and incompatible mods
        register(FeatureKey.XP_ORB_MERGING,
                GameplayConfig.ENABLE_XP_ORB_MERGING::get,
                "clumps");

        register(FeatureKey.AI_OPTIMIZATIONS,
                PerformanceConfig.ENABLE_AI_OPTIMIZATIONS::get,
                "aiimprovements");

        register(FeatureKey.LEAK_GUARD,
                PerformanceConfig.ENABLE_LEAK_GUARD::get,
                "alltheleaks", "memoryleakfix");

        register(FeatureKey.DYNAMIC_FPS,
                PerformanceConfig.ENABLE_DYNAMIC_FPS::get,
                "dynamic_fps", "fps_reducer");

        register(FeatureKey.BETTER_SNOW_LOGIC,
                () -> GameplayConfig.ENABLE_SNOW_ACCUMULATION_FIX.get()
                        && GameplayConfig.ENABLE_BETTER_SNOW_LOGIC.get());

        register(FeatureKey.ACTION_GUARD,
                SafetyConfig.ENABLE_ACTION_GUARD::get,
                "dontdothat");

        register(FeatureKey.SMART_LEAVES,
                RenderingConfig.ENABLE_SMART_LEAVES::get,
                "cull-leaves", "moreculling", "optileaves", "cull-less-leaves");

        // Add other keys if they were present in incoming but missing here
        // Checked: Incoming had same set.
    }

    /**
     * Refresh all feature toggles based on current configuration and loaded mods.
     * Called during initialization and when configuration is reloaded.
     */
    public static void refreshFromConfig() {
        ModList modList = ModList.get();

        for (Map.Entry<FeatureKey, FeatureToggle> entry : TOGGLES.entrySet()) {
            FeatureToggle toggle = entry.getValue();

            boolean configEnabled = toggle.configSupplier.get();
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

    public static boolean isEnabled(FeatureKey key) {
        FeatureToggle toggle = TOGGLES.get(key);
        return toggle != null && toggle.enabled.get();
    }

    /**
     * Event handler for config reload events.
     *
     * @param event The config reload event
     */
    public static void onModConfigReloaded(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(MCOPT.MOD_ID)) {
            refreshFromConfig();
        }
    }

    private static void register(FeatureKey key, Supplier<Boolean> configSupplier, String... incompatibleMods) {
        TOGGLES.put(key, new FeatureToggle(configSupplier, key.getDisplayName(), incompatibleMods));
    }

    /**
     * Internal class to hold toggle state and metadata.
     */
    private static class FeatureToggle {
        final Supplier<Boolean> configSupplier;
        final String displayName;
        final Set<String> incompatibleMods;
        final AtomicBoolean enabled = new AtomicBoolean(false);

        FeatureToggle(Supplier<Boolean> configSupplier, String displayName, String... incompatibleMods) {
            this.configSupplier = configSupplier;
            this.displayName = displayName;
            this.incompatibleMods = new HashSet<>(Arrays.asList(incompatibleMods));
        }

        boolean hasIncompatibleMod(ModList modList) {
            for (String modId : incompatibleMods) {
                if (modList.isLoaded(modId)) {
                    return true;
                }
            }
            return false;
        }

        List<String> getLoadedIncompatibleMods(ModList modList) {
            List<String> loaded = new ArrayList<>();
            for (String modId : incompatibleMods) {
                if (modList.isLoaded(modId)) {
                    loaded.add(modId);
                }
            }
            return loaded;
        }
    }
}
