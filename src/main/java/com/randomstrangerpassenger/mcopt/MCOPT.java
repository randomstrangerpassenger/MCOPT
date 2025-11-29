package com.randomstrangerpassenger.mcopt;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MCOPT.MOD_ID)
public class MCOPT {
    public static final String MOD_ID = "mcopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MCOPT(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("MCOPT initializing - Performance optimization mod for Minecraft");

        // Register configuration
        modContainer.registerConfig(ModConfig.Type.CLIENT, MCOPTConfig.SPEC);

        // Register setup handlers
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("MCOPT initialization complete");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MCOPT common setup");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("MCOPT client setup - Loading client-side optimizations");

        if (MCOPTConfig.ENABLE_CHUNK_OPTIMIZATIONS.get()) {
            LOGGER.info("Chunk rendering optimizations: ENABLED");
        }

        if (MCOPTConfig.ENABLE_ENTITY_CULLING.get()) {
            LOGGER.info("Entity culling optimizations: ENABLED");
        }

        if (MCOPTConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get()) {
            LOGGER.info("Particle system optimizations: ENABLED");
        }
    }
}
