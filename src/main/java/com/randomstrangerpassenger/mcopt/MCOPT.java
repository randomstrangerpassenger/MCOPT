package com.randomstrangerpassenger.mcopt;

import com.randomstrangerpassenger.mcopt.client.MCOPTClient;
import com.randomstrangerpassenger.mcopt.common.EventRegistrar;
import com.randomstrangerpassenger.mcopt.common.FeatureInitializer;
import com.randomstrangerpassenger.mcopt.config.ConfigCacheManager;
import com.randomstrangerpassenger.mcopt.config.ConfigMigrationHandler;
import com.randomstrangerpassenger.mcopt.config.ConfigRegistrar;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import com.randomstrangerpassenger.mcopt.worldgen.MCOPTBiomeModifiers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for MCOPT (Minecraft Optimization).
 * <p>
 * This is a performance optimization mod for Neoforge, focused on improving
 * game logic and rendering performance to reduce lag. Primary focus is on
 * creating a smooth singleplayer experience with client-side optimizations.
 * </p>
 * <p>
 * Architecture: This class serves as a lightweight entry point, delegating
 * responsibilities to specialized registrar and initializer classes:
 * - {@link ConfigRegistrar}: Configuration file registration
 * - {@link EventRegistrar}: Server-side event handler registration
 * - {@link FeatureInitializer}: Feature initialization and logging
 * - {@link MCOPTClient}: Client-only event registration (Dist.CLIENT)
 * </p>
 */
@Mod(MCOPT.MOD_ID)
public class MCOPT {
    public static final String MOD_ID = "mcopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MCOPT(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("MCOPT initializing - Performance optimization mod for Minecraft");

        // Register worldgen content
        MCOPTBiomeModifiers.register(modEventBus);

        // Register configurations
        ConfigRegistrar.registerConfigs(modContainer);

        // Resolve feature toggles based on config and mod compatibility
        FeatureToggles.refreshFromConfig();
        modEventBus.addListener(FeatureToggles::onModConfigReloaded);

        // Register safety modules
        EventRegistrar.registerSafetyModules();

        // Register server-side event handlers
        EventRegistrar.registerServerHandlers();
        EventRegistrar.registerCommands();

        // Register config migration handlers
        modEventBus.addListener(ConfigMigrationHandler::onConfigLoad);
        modEventBus.addListener(ConfigMigrationHandler::onConfigReload);

        // Register config cache manager (Phase 5 refresh)
        modEventBus.addListener(ConfigCacheManager::onConfigLoad);
        modEventBus.addListener(ConfigCacheManager::onConfigReload);

        // Register setup event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("MCOPT initialization complete");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        FeatureInitializer.onCommonSetup(event);

        // Register safety handler event listeners after module initialization
        EventRegistrar.registerSafetyHandlers();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        FeatureInitializer.onClientSetup(event);

        // Register client-only handlers (Dist.CLIENT safe)
        MCOPTClient.registerClientHandlers();
    }
}
