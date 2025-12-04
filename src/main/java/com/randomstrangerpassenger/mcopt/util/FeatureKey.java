package com.randomstrangerpassenger.mcopt.util;

/**
 * Type-safe enumeration of all feature toggles in MCOPT.
 * <p>
 * Provides compile-time safety for feature toggle operations,
 * replacing error-prone string-based keys.
 */
public enum FeatureKey {
    /**
     * XP orb merging optimization
     * Merges nearby experience orbs to reduce entity count
     */
    XP_ORB_MERGING("xpOrbMerging", "XP Orb merging"),

    /**
     * AI optimization system
     * Optimizes mob AI pathfinding and goal execution
     */
    AI_OPTIMIZATIONS("aiOptimizations", "AI optimization"),

    /**
     * Memory leak guard
     * Prevents common memory leaks in client-side rendering
     */
    LEAK_GUARD("leakGuard", "Leak Guard"),

    /**
     * Dynamic FPS controller
     * Reduces FPS when window is unfocused
     */
    DYNAMIC_FPS("dynamicFps", "Dynamic FPS controller"),

    /**
     * Better snow logic
     * Improves snow accumulation mechanics
     */
    BETTER_SNOW_LOGIC("betterSnowLogic", "Better Snow Logic"),

    /**
     * Action guard
     * Prevents dangerous player actions
     */
    ACTION_GUARD("actionGuard", "Action Guard"),

    /**
     * Item NBT sanitizer
     * Fixes item stacking issues by removing empty NBT tags
     */
    ITEM_NBT_SANITIZER("itemNbtSanitizer", "Item NBT Sanitizer"),

    /**
     * Damage tilt fix
     * Fixes incorrect camera tilt direction when taking damage
     */
    DAMAGE_TILT_FIX("damageTiltFix", "Damage Tilt Fix"),

    /**
     * Smart leaves culling
     * Removes rendering of inner leaf blocks hidden by outer leaves
     * OptiLeaves-style optimization for forest biomes
     */
    SMART_LEAVES("smartLeaves", "Smart Leaves Culling"),

    /**
     * Right-click fallthrough (RCF-style)
     * When right-hand item use fails, automatically tries using left-hand item
     */
    RIGHT_CLICK_FALLTHROUGH("rightClickFallthrough", "Right-Click Fallthrough");

    private final String key;
    private final String displayName;

    FeatureKey(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    /**
     * Get the string key for this feature (for backwards compatibility)
     */
    public String getKey() {
        return key;
    }

    /**
     * Get the human-readable display name for logging
     */
    public String getDisplayName() {
        return displayName;
    }
}
