package com.randomstrangerpassenger.mcopt.config;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.client.manager.AdaptiveLimitsManager;
import com.randomstrangerpassenger.mcopt.client.rendering.particle.ParticlePhysicsOptimizer;
import com.randomstrangerpassenger.mcopt.client.ui.HUDCache;
import com.randomstrangerpassenger.mcopt.client.ui.SignTextCache;
import com.randomstrangerpassenger.mcopt.common.cache.BiomeLookupCache;
import com.randomstrangerpassenger.mcopt.common.cache.RecipeLookupCache;
import com.randomstrangerpassenger.mcopt.common.cache.TagLookupCache;
import com.randomstrangerpassenger.mcopt.server.entity.ai.BrainOptimizer;
import com.randomstrangerpassenger.mcopt.server.entity.ai.EntitySleepManager;
import com.randomstrangerpassenger.mcopt.server.entity.ai.PathfindingCache;
import com.randomstrangerpassenger.mcopt.server.entity.xp.XpOrbHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Centralized config cache manager that refreshes all cached config values
 * when configuration is reloaded.
 * <p>
 * This ensures consistent cache invalidation strategy across all handlers
 * and eliminates the need for per-tick config value comparisons.
 */
public final class ConfigCacheManager {

    private ConfigCacheManager() {
        // Utility class
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        refreshAll(event);
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        refreshAll(event);
    }

    private static void refreshAll(ModConfigEvent event) {
        if (!event.getConfig().getModId().equals(MCOPT.MOD_ID)) {
            return;
        }

        MCOPT.LOGGER.info("Config loaded/reloaded, refreshing cached values...");

        // Phase 1: Entity & Physics
        XpOrbHandler.refreshConfigCache();
        ParticlePhysicsOptimizer.refreshConfigCache();

        // Phase 2: Rendering Cache
        SignTextCache.refreshConfigCache();
        HUDCache.refreshConfigCache();

        // Phase 3: Data Caching
        RecipeLookupCache.refreshConfigCache();
        TagLookupCache.refreshConfigCache();
        BiomeLookupCache.refreshConfigCache();

        // Phase 4: AI Optimization
        BrainOptimizer.refreshConfigCache();
        EntitySleepManager.refreshConfigCache();
        PathfindingCache.refreshConfigCache();

        // Phase 5: Adaptive Systems
        AdaptiveLimitsManager.refreshConfigCache();

        MCOPT.LOGGER.info("Config cache refresh complete");
    }
}
