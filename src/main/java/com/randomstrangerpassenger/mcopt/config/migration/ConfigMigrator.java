package com.randomstrangerpassenger.mcopt.config.migration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.randomstrangerpassenger.mcopt.MCOPT;

import java.util.*;

/**
 * Orchestrates configuration migrations from old versions to current version.
 * <p>
 * Migration process:
 * 1. Read config version from file
 * 2. If version is missing or older than current, apply migrations
 * 3. Migrations are applied sequentially in version order
 * 4. Update version field to current after successful migration
 * <p>
 * Thread-safe: Migrations are synchronized to prevent concurrent modifications.
 */
public class ConfigMigrator {

    /**
     * Configuration key for storing version information.
     */
    public static final String VERSION_KEY = "config_version";

    /**
     * Registry of all available migrations, sorted by version.
     */
    private static final List<ConfigMigration> MIGRATIONS = new ArrayList<>();

    static {
        // Register migrations here in chronological order
        // Example:
        // MIGRATIONS.add(new MigrationV1ToV2());
        // MIGRATIONS.add(new MigrationV2ToV3());

        // Sort migrations by source version to ensure correct order
        MIGRATIONS.sort(Comparator.comparing(ConfigMigration::fromVersion));

        // Validate migration chain integrity
        validateMigrationChain();
    }

    /**
     * Validate that migrations form a continuous chain without gaps.
     *
     * @throws IllegalStateException if migration chain is invalid
     */
    private static void validateMigrationChain() {
        if (MIGRATIONS.isEmpty()) {
            return; // No migrations registered yet
        }

        ConfigVersion expectedVersion = MIGRATIONS.get(0).fromVersion();

        for (ConfigMigration migration : MIGRATIONS) {
            if (!migration.fromVersion().equals(expectedVersion)) {
                throw new IllegalStateException(
                        String.format("Migration chain gap: expected migration from %s, found %s",
                                expectedVersion, migration.fromVersion()));
            }
            expectedVersion = migration.toVersion();
        }

        // Verify chain ends at current version
        if (!expectedVersion.equals(ConfigVersion.CURRENT)) {
            MCOPT.LOGGER.warn("Migration chain ends at {} but current version is {}. " +
                            "This may indicate missing migrations.",
                    expectedVersion, ConfigVersion.CURRENT);
        }
    }

    /**
     * Migrate a configuration from its current version to the latest version.
     *
     * @param config Configuration to migrate
     * @return true if migration was performed, false if config was already current
     * @throws ConfigMigration.ConfigMigrationException if migration fails
     */
    public static synchronized boolean migrate(CommentedConfig config)
            throws ConfigMigration.ConfigMigrationException {

        // Determine current config version
        ConfigVersion currentVersion = getConfigVersion(config);

        MCOPT.LOGGER.debug("Config version: {}, Current version: {}",
                currentVersion, ConfigVersion.CURRENT);

        // Check if migration is needed
        if (!currentVersion.requiresMigrationTo(ConfigVersion.CURRENT)) {
            MCOPT.LOGGER.debug("Config is up to date, no migration needed");
            return false;
        }

        MCOPT.LOGGER.info("Migrating config from {} to {}",
                currentVersion, ConfigVersion.CURRENT);

        // Apply migrations sequentially
        List<ConfigMigration> applicableMigrations = getApplicableMigrations(currentVersion);

        if (applicableMigrations.isEmpty()) {
            MCOPT.LOGGER.warn("Config version {} is older than current {}, but no migrations found",
                    currentVersion, ConfigVersion.CURRENT);
            // Set to current version anyway to avoid repeated warnings
            setConfigVersion(config, ConfigVersion.CURRENT);
            return false;
        }

        // Apply each migration in sequence
        for (ConfigMigration migration : applicableMigrations) {
            MCOPT.LOGGER.info("Applying migration: {} -> {} ({})",
                    migration.fromVersion(),
                    migration.toVersion(),
                    migration.getDescription());

            try {
                migration.migrate(config);
                MCOPT.LOGGER.info("Successfully migrated to {}", migration.toVersion());
            } catch (Exception e) {
                String errorMsg = String.format("Failed to migrate config from %s to %s: %s",
                        migration.fromVersion(),
                        migration.toVersion(),
                        e.getMessage());
                throw new ConfigMigration.ConfigMigrationException(errorMsg, e);
            }
        }

        // Update version to current
        setConfigVersion(config, ConfigVersion.CURRENT);
        MCOPT.LOGGER.info("Config migration completed successfully");

        return true;
    }

    /**
     * Get the version of a configuration file.
     * If version is missing, returns INITIAL version.
     *
     * @param config Configuration to check
     * @return Configuration version
     */
    public static ConfigVersion getConfigVersion(CommentedConfig config) {
        Object versionObj = config.get(VERSION_KEY);

        if (versionObj == null) {
            // No version field - treat as initial version
            return ConfigVersion.INITIAL;
        }

        if (versionObj instanceof String) {
            try {
                return ConfigVersion.parse((String) versionObj);
            } catch (IllegalArgumentException e) {
                MCOPT.LOGGER.warn("Invalid config version format: {}, treating as initial version",
                        versionObj, e);
                return ConfigVersion.INITIAL;
            }
        }

        MCOPT.LOGGER.warn("Unexpected config version type: {}, treating as initial version",
                versionObj.getClass());
        return ConfigVersion.INITIAL;
    }

    /**
     * Set the version field in a configuration.
     *
     * @param config Configuration to update
     * @param version Version to set
     */
    public static void setConfigVersion(CommentedConfig config, ConfigVersion version) {
        config.set(VERSION_KEY, version.toString());
        config.setComment(VERSION_KEY,
                "Configuration version for migration tracking (DO NOT MODIFY)");
    }

    /**
     * Get list of migrations that should be applied to upgrade from given version.
     *
     * @param fromVersion Starting version
     * @return List of migrations to apply, in order
     */
    private static List<ConfigMigration> getApplicableMigrations(ConfigVersion fromVersion) {
        List<ConfigMigration> applicable = new ArrayList<>();

        for (ConfigMigration migration : MIGRATIONS) {
            // Include migration if its source version >= our current version
            // and its source version < target version
            if (migration.fromVersion().compareTo(fromVersion) >= 0
                    && migration.fromVersion().isOlderThan(ConfigVersion.CURRENT)) {
                applicable.add(migration);
            }
        }

        return applicable;
    }

    /**
     * Check if a configuration needs migration.
     *
     * @param config Configuration to check
     * @return true if migration is needed
     */
    public static boolean needsMigration(CommentedConfig config) {
        ConfigVersion currentVersion = getConfigVersion(config);
        return currentVersion.requiresMigrationTo(ConfigVersion.CURRENT);
    }

    /**
     * Get list of all registered migrations.
     * For testing and debugging purposes.
     *
     * @return Unmodifiable list of migrations
     */
    public static List<ConfigMigration> getRegisteredMigrations() {
        return Collections.unmodifiableList(MIGRATIONS);
    }
}
