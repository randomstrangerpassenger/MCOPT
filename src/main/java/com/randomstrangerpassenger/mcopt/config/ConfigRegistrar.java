package com.randomstrangerpassenger.mcopt.config;

import com.randomstrangerpassenger.mcopt.MCOPT;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

/**
 * Centralized configuration registration handler.
 * <p>
 * Responsible for registering all MCOPT domain-specific configuration files
 * with the mod container. This separates config registration logic from the
 * main mod class.
 * </p>
 */
public class ConfigRegistrar {

        /**
         * Register all MCOPT configuration files.
         * <p>
         * Registers four separate config files for different domains:
         * - Rendering optimizations (mcopt-rendering.toml)
         * - Performance optimizations (mcopt-performance.toml)
         * - Gameplay modifications (mcopt-gameplay.toml)
         * - Safety features (mcopt-safety.toml)
         * </p>
         *
         * @param modContainer the mod container to register configs with
         */
        public static void registerConfigs(ModContainer modContainer) {
                MCOPT.LOGGER.info("Registering MCOPT configuration files");

                // Register domain-specific configurations
                modContainer.registerConfig(ModConfig.Type.CLIENT,
                                MCOPTConfig.getRenderingSpec(),
                                "mcopt-rendering.toml");

                modContainer.registerConfig(ModConfig.Type.CLIENT,
                                MCOPTConfig.getPerformanceSpec(),
                                "mcopt-performance.toml");

                modContainer.registerConfig(ModConfig.Type.CLIENT,
                                MCOPTConfig.getGameplaySpec(),
                                "mcopt-gameplay.toml");

                modContainer.registerConfig(ModConfig.Type.CLIENT,
                                MCOPTConfig.getSafetySpec(),
                                "mcopt-safety.toml");

                modContainer.registerConfig(ModConfig.Type.CLIENT,
                                MCOPTConfig.getSoundSpec(),
                                "mcopt-sound.toml");

                modContainer.registerConfig(ModConfig.Type.CLIENT,
                                MCOPTConfig.getEntitySpec(),
                                "mcopt-entity.toml");

                MCOPT.LOGGER.info("Configuration files registered: 6 domain-specific configs");
        }
}
