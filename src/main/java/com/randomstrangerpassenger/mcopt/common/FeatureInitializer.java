package com.randomstrangerpassenger.mcopt.common;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import com.randomstrangerpassenger.mcopt.safety.SafetyModuleRegistry;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import com.randomstrangerpassenger.mcopt.util.FeatureKey;

import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Centralized feature initialization handler.
 * <p>
 * Handles common and client setup logic that was previously in MCOPT.java.
 * Responsible for initializing features and logging their status.
 * </p>
 */
public class FeatureInitializer {

    /**
     * Common setup - runs on both client and server.
     * <p>
     * Initializes features that work in both environments, such as safety features.
     * </p>
     *
     * @param event the common setup event
     */
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        MCOPT.LOGGER.info("MCOPT common setup");

        // Initialize safety modules
        SafetyModuleRegistry.initializeAll();

        // Log safety feature status
        if (SafetyConfig.ENABLE_MAX_HEALTH_STABILITY.get()) {
            MCOPT.LOGGER.info("Max health stability fix: ENABLED");
        }

        if (SafetyConfig.ENABLE_ATTRIBUTE_RANGE_EXPANSION.get()) {
            MCOPT.LOGGER.info("Attribute cap expansion: ENABLED (max {})",
                    SafetyConfig.ATTRIBUTE_MAX_LIMIT.get());
        }
    }

    /**
     * Client setup - runs only on the client.
     * <p>
     * Logs the status of client-only features like rendering optimizations.
     * Actual client handler registration happens in MCOPTClient.
     * </p>
     *
     * @param event the client setup event
     */
    public static void onClientSetup(final FMLClientSetupEvent event) {
        MCOPT.LOGGER.info("MCOPT client setup - Loading client-side optimizations");

        // Log gameplay feature status
        if (FeatureToggles.isEnabled(FeatureKey.XP_ORB_MERGING)) {
            MCOPT.LOGGER.info("Experience orb merging optimizations: ENABLED");
        }

        // Log smart animations status
        if (RenderingConfig.ENABLE_SMART_ANIMATIONS.get()) {
            MCOPT.LOGGER.info("Smart texture animations: ENABLED");
        }

        // Log visual item merging status
        if (RenderingConfig.ENABLE_VISUAL_ITEM_MERGING.get()) {
            MCOPT.LOGGER.info("Visual item merging: ENABLED");
        }

        MCOPT.LOGGER.info("Client setup complete");
    }
}
