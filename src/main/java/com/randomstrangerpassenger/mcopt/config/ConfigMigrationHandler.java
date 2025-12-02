package com.randomstrangerpassenger.mcopt.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.migration.ConfigMigration;
import com.randomstrangerpassenger.mcopt.config.migration.ConfigMigrator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

/**
 * Handles automatic configuration migration when configs are loaded or reloaded.
 * <p>
 * This handler listens to NeoForge config events and automatically migrates
 * outdated configuration files to the current version.
 * <p>
 * Migration process:
 * 1. Config file is loaded by NeoForge
 * 2. Check if config version is older than current
 * 3. If yes, apply sequential migrations
 * 4. Save migrated config back to file
 * 5. Reload config values in memory
 * <p>
 * Thread-safety: Config events are fired on the main thread, no additional
 * synchronization needed beyond what ConfigMigrator already provides.
 */
@EventBusSubscriber(modid = MCOPT.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigMigrationHandler {

    /**
     * Handle config loading event.
     * Automatically migrates config if needed.
     */
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        handleConfigEvent(event, "loading");
    }

    /**
     * Handle config reloading event.
     * Automatically migrates config if needed.
     */
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        handleConfigEvent(event, "reloading");
    }

    /**
     * Common handler for both loading and reloading events.
     *
     * @param event Config event
     * @param action "loading" or "reloading" (for logging)
     */
    private static void handleConfigEvent(ModConfigEvent event, String action) {
        // Only handle our own configs
        if (!event.getConfig().getModId().equals(MCOPT.MOD_ID)) {
            return;
        }

        String configName = event.getConfig().getFileName();
        MCOPT.LOGGER.debug("Config {} {}: {}", action, configName, event.getConfig().getType());

        try {
            // Access the underlying config data
            CommentedConfig configData = event.getConfig().getConfigData();

            // Check if migration is needed
            if (!ConfigMigrator.needsMigration(configData)) {
                MCOPT.LOGGER.debug("Config {} is up to date, no migration needed", configName);
                return;
            }

            MCOPT.LOGGER.info("Config {} requires migration, starting migration process...", configName);

            // Perform migration
            boolean migrated = ConfigMigrator.migrate(configData);

            if (migrated) {
                // Save the migrated config
                event.getConfig().save();
                MCOPT.LOGGER.info("Config {} successfully migrated and saved", configName);

                // Note: NeoForge will automatically reload the config values after this event
            } else {
                MCOPT.LOGGER.debug("Config {} migration returned false (no changes)", configName);
            }

        } catch (ConfigMigration.ConfigMigrationException e) {
            // Log error but don't crash - config will use default values
            MCOPT.LOGGER.error("Failed to migrate config {}: {}",
                    configName, e.getMessage(), e);
            MCOPT.LOGGER.warn("Config {} will use default values. " +
                            "Manual intervention may be required.",
                    configName);

        } catch (Exception e) {
            // Catch-all for unexpected errors
            MCOPT.LOGGER.error("Unexpected error during config migration for {}: {}",
                    configName, e.getMessage(), e);
            MCOPT.LOGGER.warn("Config {} will use default values.", configName);
        }
    }

    /**
     * Optional: Handle config unloading event for cleanup.
     * Not strictly necessary for migration, but included for completeness.
     */
    @SubscribeEvent
    public static void onConfigUnload(ModConfigEvent.Unloading event) {
        if (!event.getConfig().getModId().equals(MCOPT.MOD_ID)) {
            return;
        }

        String configName = event.getConfig().getFileName();
        MCOPT.LOGGER.debug("Config unloading: {}", configName);

        // Perform any cleanup if needed
        // (Currently no cleanup required for migration system)
    }
}
