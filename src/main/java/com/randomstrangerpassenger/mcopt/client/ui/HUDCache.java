package com.randomstrangerpassenger.mcopt.client.ui;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Caches HUD rendering state to avoid redundant calculations.
 * 
 * <p>
 * HUD elements (health bar, hunger bar, hotbar) are rendered every frame
 * but only change occasionally. This cache tracks changes and skips
 * redundant rendering work.
 * </p>
 * 
 * <p>
 * <strong>Cached Elements:</strong>
 * </p>
 * <ul>
 * <li>Health bar: Only update on health/max health change</li>
 * <li>Hunger bar: Only update on food level change</li>
 * <li>Hotbar: Only update on slot selection or item change</li>
 * <li>XP bar: Only update on XP change</li>
 * </ul>
 * 
 * <p>
 * <strong>Sodium Synergy:</strong> UI rendering is not covered by Sodium.
 * </p>
 */
public class HUDCache {

    // Cached config value
    private static boolean enabled = true;

    // Health bar cache
    private static float lastHealth = 0;
    private static float lastMaxHealth = 0;
    private static float lastAbsorption = 0;

    // Hunger bar cache
    private static int lastFoodLevel = 0;
    private static float lastSaturation = 0;

    // Hotbar cache
    private static int lastSelectedSlot = 0;
    private static final int[] lastItemHashes = new int[9];

    // XP bar cache
    private static int lastXpLevel = 0;
    private static float lastXpProgress = 0;

    // Air bar cache
    private static int lastAir = 0;
    private static int lastMaxAir = 0;

    // Armor bar cache
    private static int lastArmorValue = 0;

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_TEXT_CACHING.get();
    }

    /**
     * Check if HUD caching is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Check if health bar needs update.
     * 
     * @param player Current player
     * @return true if health bar should be redrawn
     */
    public static boolean needsHealthUpdate(@Nonnull Player player) {
        if (!enabled) {
            return true;
        }

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();

        if (health != lastHealth || maxHealth != lastMaxHealth || absorption != lastAbsorption) {
            lastHealth = health;
            lastMaxHealth = maxHealth;
            lastAbsorption = absorption;
            return true;
        }

        return false;
    }

    /**
     * Check if hunger bar needs update.
     * 
     * @param player Current player
     * @return true if hunger bar should be redrawn
     */
    public static boolean needsHungerUpdate(@Nonnull Player player) {
        if (!enabled) {
            return true;
        }

        int foodLevel = player.getFoodData().getFoodLevel();
        float saturation = player.getFoodData().getSaturationLevel();

        if (foodLevel != lastFoodLevel || saturation != lastSaturation) {
            lastFoodLevel = foodLevel;
            lastSaturation = saturation;
            return true;
        }

        return false;
    }

    /**
     * Check if hotbar needs update.
     * 
     * @param player Current player
     * @return true if hotbar should be redrawn
     */
    public static boolean needsHotbarUpdate(@Nonnull Player player) {
        if (!enabled) {
            return true;
        }

        // Get selected slot index by checking which hotbar slot matches the main hand
        // item
        int selectedSlot = 0;
        ItemStack mainHand = player.getMainHandItem();
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == mainHand) {
                selectedSlot = i;
                break;
            }
        }
        boolean needsUpdate = selectedSlot != lastSelectedSlot;
        lastSelectedSlot = selectedSlot;

        // Check if any hotbar item changed
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            int hash = getItemHash(stack);
            if (hash != lastItemHashes[i]) {
                lastItemHashes[i] = hash;
                needsUpdate = true;
            }
        }

        return needsUpdate;
    }

    /**
     * Check if XP bar needs update.
     * 
     * @param player Current player
     * @return true if XP bar should be redrawn
     */
    public static boolean needsXPUpdate(@Nonnull Player player) {
        if (!enabled) {
            return true;
        }

        int xpLevel = player.experienceLevel;
        float xpProgress = player.experienceProgress;

        if (xpLevel != lastXpLevel || xpProgress != lastXpProgress) {
            lastXpLevel = xpLevel;
            lastXpProgress = xpProgress;
            return true;
        }

        return false;
    }

    /**
     * Check if air bar needs update.
     * 
     * @param player Current player
     * @return true if air bar should be redrawn
     */
    public static boolean needsAirUpdate(@Nonnull Player player) {
        if (!enabled) {
            return true;
        }

        int air = player.getAirSupply();
        int maxAir = player.getMaxAirSupply();

        if (air != lastAir || maxAir != lastMaxAir) {
            lastAir = air;
            lastMaxAir = maxAir;
            return true;
        }

        return false;
    }

    /**
     * Check if armor bar needs update.
     * 
     * @param player Current player
     * @return true if armor bar should be redrawn
     */
    public static boolean needsArmorUpdate(@Nonnull Player player) {
        if (!enabled) {
            return true;
        }

        int armorValue = player.getArmorValue();

        if (armorValue != lastArmorValue) {
            lastArmorValue = armorValue;
            return true;
        }

        return false;
    }

    /**
     * Calculate hash for an item stack.
     */
    private static int getItemHash(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        int hash = 17;
        hash = 31 * hash + stack.getItem().hashCode();
        hash = 31 * hash + stack.getCount();
        hash = 31 * hash + (stack.isDamaged() ? stack.getDamageValue() : 0);
        return hash;
    }

    /**
     * Reset all cached HUD state.
     * Call when player changes or world loads.
     */
    public static void resetAll() {
        lastHealth = 0;
        lastMaxHealth = 0;
        lastAbsorption = 0;
        lastFoodLevel = 0;
        lastSaturation = 0;
        lastSelectedSlot = 0;
        for (int i = 0; i < 9; i++) {
            lastItemHashes[i] = 0;
        }
        lastXpLevel = 0;
        lastXpProgress = 0;
        lastAir = 0;
        lastMaxAir = 0;
        lastArmorValue = 0;

    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "HUDCache: enabled=" + enabled;
    }
}
