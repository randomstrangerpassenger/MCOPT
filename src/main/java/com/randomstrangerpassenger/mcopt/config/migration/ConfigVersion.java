package com.randomstrangerpassenger.mcopt.config.migration;

import java.util.Objects;

/**
 * Represents a configuration version for migration purposes.
 * <p>
 * Configuration versions follow semantic versioning principles:
 * - Major: Breaking changes requiring migration
 * - Minor: New features added (backward compatible)
 * - Patch: Bug fixes (no structural changes)
 * <p>
 * For config migration, we primarily care about major and minor versions,
 * as patch versions don't typically require config changes.
 */
public final class ConfigVersion implements Comparable<ConfigVersion> {

    /**
     * Current configuration version.
     * Update this when adding new config fields or changing existing ones.
     */
    public static final ConfigVersion CURRENT = new ConfigVersion(1, 0, 0);

    /**
     * Initial configuration version (when migration system was introduced).
     */
    public static final ConfigVersion INITIAL = new ConfigVersion(1, 0, 0);

    private final int major;
    private final int minor;
    private final int patch;

    public ConfigVersion(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version numbers cannot be negative");
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Parse a version string in format "major.minor.patch".
     *
     * @param version Version string (e.g., "1.0.0")
     * @return Parsed ConfigVersion
     * @throws IllegalArgumentException if format is invalid
     */
    public static ConfigVersion parse(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }

        String[] parts = version.trim().split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid version format: " + version + ". Expected format: major.minor.patch");
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = Integer.parseInt(parts[2]);
            return new ConfigVersion(major, minor, patch);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid version format: " + version, e);
        }
    }

    /**
     * Check if this version is older than another version.
     *
     * @param other Version to compare against
     * @return true if this version is older
     */
    public boolean isOlderThan(ConfigVersion other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Check if this version is newer than another version.
     *
     * @param other Version to compare against
     * @return true if this version is newer
     */
    public boolean isNewerThan(ConfigVersion other) {
        return this.compareTo(other) > 0;
    }

    /**
     * Check if this version requires migration to reach target version.
     *
     * @param target Target version
     * @return true if migration is needed
     */
    public boolean requiresMigrationTo(ConfigVersion target) {
        // Migration needed if current version is older
        return isOlderThan(target);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(ConfigVersion other) {
        if (this.major != other.major) {
            return Integer.compare(this.major, other.major);
        }
        if (this.minor != other.minor) {
            return Integer.compare(this.minor, other.minor);
        }
        return Integer.compare(this.patch, other.patch);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConfigVersion)) return false;
        ConfigVersion other = (ConfigVersion) obj;
        return this.major == other.major
                && this.minor == other.minor
                && this.patch == other.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
