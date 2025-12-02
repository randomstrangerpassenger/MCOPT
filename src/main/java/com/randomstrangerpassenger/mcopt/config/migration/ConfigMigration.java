package com.randomstrangerpassenger.mcopt.config.migration;

import com.electronwill.nightconfig.core.CommentedConfig;

/**
 * Interface for a single configuration migration step.
 * <p>
 * Each migration transforms a configuration from one version to the next.
 * Migrations form a chain: v1 -> v2 -> v3 -> v4, etc.
 * <p>
 * Implementation guidelines:
 * - Be idempotent: Running twice should have same effect as running once
 * - Handle missing fields gracefully
 * - Add comments explaining new fields
 * - Preserve user-modified values when possible
 * - Log migration actions for debugging
 */
public interface ConfigMigration {

    /**
     * Get the source version this migration upgrades from.
     *
     * @return Source version
     */
    ConfigVersion fromVersion();

    /**
     * Get the target version this migration upgrades to.
     *
     * @return Target version
     */
    ConfigVersion toVersion();

    /**
     * Perform the migration on the given configuration.
     * <p>
     * The config object is mutable and should be modified in-place.
     * If migration fails, throw an exception with a descriptive message.
     *
     * @param config Configuration to migrate
     * @throws ConfigMigrationException if migration fails
     */
    void migrate(CommentedConfig config) throws ConfigMigrationException;

    /**
     * Get a human-readable description of what this migration does.
     * Used for logging and debugging.
     *
     * @return Migration description
     */
    String getDescription();

    /**
     * Exception thrown when configuration migration fails.
     */
    class ConfigMigrationException extends Exception {
        public ConfigMigrationException(String message) {
            super(message);
        }

        public ConfigMigrationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
