package com.randomstrangerpassenger.mcopt.client.ui;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Optimizes inventory rendering by caching slot states.
 * 
 * <p>
 * This optimizer tracks which inventory slots have changed and only
 * triggers re-rendering for modified slots.
 * </p>
 * 
 * <p>
 * <strong>Immersion First Philosophy:</strong>
 * </p>
 * <ul>
 * <li>No visual changes to inventory appearance</li>
 * <li>Reduces frame drops when opening inventories</li>
 * <li>Caches slot item hashes to detect changes</li>
 * </ul>
 */
public class InventoryOptimizer {

    private static boolean enabled = true;

    // Cache slot states for change detection
    private static final Map<Integer, Integer> slotHashCache = new HashMap<>();
    private static long lastCacheClearTime = 0;
    private static final long CACHE_CLEAR_INTERVAL_MS = 5000; // Clear cache every 5 seconds

    // Track render statistics
    private static int totalSlotsChecked = 0;
    private static int slotsSkipped = 0;

    /**
     * Check if inventory optimization is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable inventory optimization.
     */
    public static void setEnabled(boolean enable) {
        enabled = enable;
        if (!enable) {
            clearCache();
        }
    }

    /**
     * Check if a slot needs to be re-rendered.
     * 
     * @param slotId The slot ID
     * @param stack  The current ItemStack in the slot
     * @return true if the slot needs rendering, false if it can be skipped
     */
    public static boolean needsRender(int slotId, @Nonnull ItemStack stack) {
        if (!enabled) {
            return true;
        }

        // Periodic cache clear to prevent memory issues
        maybeClearCache();

        totalSlotsChecked++;

        int currentHash = computeStackHash(stack);
        Integer cachedHash = slotHashCache.get(slotId);

        if (cachedHash != null && cachedHash == currentHash) {
            slotsSkipped++;
            return false; // No change, can skip rendering
        }

        // Update cache
        slotHashCache.put(slotId, currentHash);
        return true;
    }

    /**
     * Mark a slot as needing re-render on next check.
     * Call this when an item is moved or modified.
     */
    public static void invalidateSlot(int slotId) {
        slotHashCache.remove(slotId);
    }

    /**
     * Clear all cached slot data.
     * Call this when the inventory is closed.
     */
    public static void clearCache() {
        slotHashCache.clear();
        totalSlotsChecked = 0;
        slotsSkipped = 0;
    }

    /**
     * Periodically clear cache to prevent memory buildup.
     */
    private static void maybeClearCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheClearTime > CACHE_CLEAR_INTERVAL_MS) {
            clearCache();
            lastCacheClearTime = currentTime;
        }
    }

    /**
     * Compute a hash for an ItemStack to detect changes.
     */
    private static int computeStackHash(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        // Combine item type, count, and damage for a quick hash
        int hash = stack.getItem().hashCode();
        hash = 31 * hash + stack.getCount();
        hash = 31 * hash + stack.getDamageValue();

        // Include component hash for items with NBT
        hash = 31 * hash + stack.getComponents().hashCode();

        return hash;
    }

    /**
     * Get optimization statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        if (totalSlotsChecked == 0) {
            return "InventoryOptimizer: No slots checked yet";
        }

        float skipRate = (float) slotsSkipped / totalSlotsChecked * 100;
        return "InventoryOptimizer: " + slotsSkipped + "/" + totalSlotsChecked +
                " slots skipped (" + String.format("%.1f", skipRate) + "%), cache size: " + slotHashCache.size();
    }
}
