package com.randomstrangerpassenger.mcopt.config.migration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.randomstrangerpassenger.mcopt.MCOPT;

/**
 * Example migration demonstrating how to upgrade config from one version to another.
 * <p>
 * This is a template/example and should be replaced with actual migrations
 * when config changes are made.
 * <p>
 * To use this as a template:
 * 1. Copy this file and rename to describe the migration (e.g., MigrationV1ToV2)
 * 2. Update fromVersion() and toVersion() to match your versions
 * 3. Implement the actual migration logic in migrate()
 * 4. Register in ConfigMigrator's static initializer
 */
public class ExampleMigration implements ConfigMigration {

    @Override
    public ConfigVersion fromVersion() {
        // This migration upgrades from version 1.0.0
        return new ConfigVersion(1, 0, 0);
    }

    @Override
    public ConfigVersion toVersion() {
        // To version 1.1.0 (minor version bump for new features)
        return new ConfigVersion(1, 1, 0);
    }

    @Override
    public void migrate(CommentedConfig config) throws ConfigMigrationException {
        MCOPT.LOGGER.info("Running example migration (v1.0.0 -> v1.1.0)");

        try {
            // Example 1: Rename a configuration key
            migrateRenamedKey(config,
                    "oldKeyName",
                    "newKeyName",
                    "Old config key was renamed for clarity");

            // Example 2: Add a new configuration field with default value
            addNewField(config,
                    "newFeatureEnabled",
                    true,
                    "Enable new feature introduced in v1.1.0");

            // Example 3: Update value based on new constraints
            updateFieldConstraints(config,
                    "maxValue",
                    100,  // new max
                    50);  // default if current value exceeds max

            // Example 4: Migrate nested configuration
            migrateNestedConfig(config);

        } catch (Exception e) {
            throw new ConfigMigrationException(
                    "Failed to migrate config from v1.0.0 to v1.1.0", e);
        }
    }

    @Override
    public String getDescription() {
        return "Example migration: Demonstrates config key renaming, new fields, and constraint updates";
    }

    // ========== Helper Methods ==========

    /**
     * Rename a configuration key, preserving the user's value.
     */
    private void migrateRenamedKey(CommentedConfig config,
                                   String oldKey,
                                   String newKey,
                                   String reason) {
        if (config.contains(oldKey)) {
            Object value = config.get(oldKey);
            config.remove(oldKey);
            config.set(newKey, value);
            config.setComment(newKey, reason);
            MCOPT.LOGGER.debug("Migrated config key: {} -> {} (value: {})",
                    oldKey, newKey, value);
        }
    }

    /**
     * Add a new configuration field with default value if it doesn't exist.
     */
    private void addNewField(CommentedConfig config,
                            String key,
                            Object defaultValue,
                            String comment) {
        if (!config.contains(key)) {
            config.set(key, defaultValue);
            config.setComment(key, comment);
            MCOPT.LOGGER.debug("Added new config field: {} = {}", key, defaultValue);
        }
    }

    /**
     * Update a field's value if it exceeds new constraints.
     */
    private void updateFieldConstraints(CommentedConfig config,
                                       String key,
                                       int maxValue,
                                       int defaultValue) {
        if (config.contains(key)) {
            Object valueObj = config.get(key);
            if (valueObj instanceof Number) {
                int currentValue = ((Number) valueObj).intValue();
                if (currentValue > maxValue) {
                    config.set(key, defaultValue);
                    MCOPT.LOGGER.info("Updated {} from {} to {} (exceeded new max: {})",
                            key, currentValue, defaultValue, maxValue);
                }
            }
        }
    }

    /**
     * Example of migrating nested configuration sections.
     */
    private void migrateNestedConfig(CommentedConfig config) {
        // Access nested config section
        if (config.contains("rendering")) {
            CommentedConfig rendering = config.get("rendering");

            // Migrate nested values
            if (rendering.contains("oldRenderSetting")) {
                Object value = rendering.get("oldRenderSetting");
                rendering.remove("oldRenderSetting");
                rendering.set("newRenderSetting", value);
                MCOPT.LOGGER.debug("Migrated nested config: rendering.oldRenderSetting -> newRenderSetting");
            }
        }
    }
}
