/**
 * Configuration migration system for MCOPT.
 * <p>
 * This package provides infrastructure for migrating configuration files
 * between different versions of the mod, ensuring backward compatibility
 * and smooth upgrades for users.
 * <p>
 * <h2>Architecture Overview</h2>
 * <ul>
 *   <li>{@link com.randomstrangerpassenger.mcopt.config.migration.ConfigVersion} - Version representation and comparison</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.config.migration.ConfigMigration} - Interface for individual migration steps</li>
 *   <li>{@link com.randomstrangerpassenger.mcopt.config.migration.ConfigMigrator} - Orchestrates migration process</li>
 * </ul>
 * <p>
 * <h2>Usage Example</h2>
 * <p>
 * To create a new migration:
 * <pre>{@code
 * public class MigrationV1ToV2 implements ConfigMigration {
 *     @Override
 *     public ConfigVersion fromVersion() {
 *         return new ConfigVersion(1, 0, 0);
 *     }
 *
 *     @Override
 *     public ConfigVersion toVersion() {
 *         return new ConfigVersion(2, 0, 0);
 *     }
 *
 *     @Override
 *     public void migrate(CommentedConfig config) {
 *         // Rename a config key
 *         if (config.contains("oldKey")) {
 *             Object value = config.get("oldKey");
 *             config.remove("oldKey");
 *             config.set("newKey", value);
 *         }
 *
 *         // Add a new field with default value
 *         if (!config.contains("newFeature")) {
 *             config.set("newFeature", true);
 *             config.setComment("newFeature", "Enable new feature (added in v2.0)");
 *         }
 *     }
 *
 *     @Override
 *     public String getDescription() {
 *         return "Rename 'oldKey' to 'newKey' and add 'newFeature' field";
 *     }
 * }
 * }</pre>
 * <p>
 * Then register it in {@link com.randomstrangerpassenger.mcopt.config.migration.ConfigMigrator}'s static initializer.
 * <p>
 * <h2>Integration with NeoForge Config System</h2>
 * <p>
 * NeoForge uses {@link net.neoforged.neoforge.common.ModConfigSpec} which doesn't
 * directly expose the underlying {@link com.electronwill.nightconfig.core.CommentedConfig}.
 * To integrate migration:
 * <ol>
 *   <li>Listen to {@link net.neoforged.fml.event.config.ModConfigEvent.Loading} event</li>
 *   <li>Access the raw config file via {@code event.getConfig().getConfigData()}</li>
 *   <li>Call {@link com.randomstrangerpassenger.mcopt.config.migration.ConfigMigrator#migrate(CommentedConfig)}</li>
 *   <li>Save changes if migration occurred</li>
 * </ol>
 * <p>
 * Example integration in mod main class:
 * <pre>{@code
 * @EventBusSubscriber(modid = MCOPT.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
 * public class ConfigMigrationHandler {
 *     @SubscribeEvent
 *     public static void onConfigLoad(ModConfigEvent.Loading event) {
 *         if (!event.getConfig().getModId().equals(MCOPT.MOD_ID)) {
 *             return;
 *         }
 *
 *         try {
 *             CommentedConfig config = event.getConfig().getConfigData();
 *             boolean migrated = ConfigMigrator.migrate(config);
 *
 *             if (migrated) {
 *                 event.getConfig().save();
 *                 MCOPT.LOGGER.info("Configuration migrated and saved");
 *             }
 *         } catch (ConfigMigration.ConfigMigrationException e) {
 *             MCOPT.LOGGER.error("Failed to migrate configuration", e);
 *         }
 *     }
 * }
 * }</pre>
 * <p>
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Always increment version when adding/removing/renaming config fields</li>
 *   <li>Write migrations that are idempotent (safe to run multiple times)</li>
 *   <li>Test migrations with actual config files from previous versions</li>
 *   <li>Document breaking changes in migration descriptions</li>
 *   <li>Keep old migration code even after release (users may skip versions)</li>
 * </ul>
 * <p>
 * <h2>Version Numbering Guidelines</h2>
 * <ul>
 *   <li><b>Major</b>: Breaking changes requiring migration (remove/rename fields)</li>
 *   <li><b>Minor</b>: New features added (backward compatible, add fields)</li>
 *   <li><b>Patch</b>: Bug fixes (no structural changes)</li>
 * </ul>
 */
package com.randomstrangerpassenger.mcopt.config.migration;
