package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Handles visual batching of nearby item entities for improved rendering
 * performance.
 * 
 * <p>
 * This optimizer groups nearby item entities and renders them as a single
 * batch,
 * reducing draw calls while keeping server-side entity logic unchanged.
 * </p>
 * 
 * <p>
 * <strong>Immersion First Philosophy:</strong>
 * </p>
 * <ul>
 * <li>Server entity count remains unchanged</li>
 * <li>Physics and pickup behavior unaffected</li>
 * <li>Only visual rendering is optimized</li>
 * </ul>
 */
@SuppressWarnings("null")
public class ItemEntityBatcher {

    private static boolean enabled = true;
    private static double mergeRadius = 1.5;
    private static int maxItemsPerBatch = 8;

    // Cache for batched item groups - cleared each frame
    private static final Map<ItemEntity, List<ItemEntity>> batchGroups = new WeakHashMap<>();
    private static final Set<ItemEntity> processedItems = Collections.newSetFromMap(new WeakHashMap<>());
    private static long lastFrameTime = 0;

    /**
     * Represents a batch of item entities to be rendered together.
     */
    public record ItemBatch(
            @Nonnull ItemEntity primaryItem,
            @Nonnull List<ItemEntity> groupedItems,
            int totalCount,
            @Nonnull Vec3 centerPosition) {
        /**
         * Check if this batch contains multiple items.
         */
        public boolean isBatched() {
            return groupedItems.size() > 1;
        }
    }

    /**
     * Refresh configuration cache from config values.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_VISUAL_ITEM_MERGING.get();
        mergeRadius = RenderingConfig.ITEM_MERGE_RADIUS.get();
        maxItemsPerBatch = RenderingConfig.MAX_ITEMS_PER_BATCH.get();
    }

    /**
     * Check if visual item merging is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Determine if this item should be rendered or skipped (because it's batched
     * with another).
     * 
     * @param item The item entity being rendered
     * @return true if this item should be rendered, false if it's part of another
     *         batch
     */
    public static boolean shouldRenderItem(@Nonnull ItemEntity item) {
        if (!enabled) {
            return true;
        }

        // Clear cache if this is a new frame
        clearCacheIfNewFrame();

        // If this item has already been processed, check if it's a primary or grouped
        if (processedItems.contains(item)) {
            return batchGroups.containsKey(item);
        }

        // Process this item and nearby items
        processBatchForItem(item);

        return true; // Primary items are always rendered
    }

    /**
     * Get the batch information for a primary item entity.
     * 
     * @param item The primary item entity
     * @return ItemBatch containing grouped items, or null if not batched
     */
    public static ItemBatch getBatchForItem(@Nonnull ItemEntity item) {
        if (!enabled || !batchGroups.containsKey(item)) {
            return null;
        }

        List<ItemEntity> grouped = batchGroups.get(item);
        if (grouped == null || grouped.size() <= 1) {
            return null;
        }

        Vec3 center = calculateBatchCenter(grouped);
        return new ItemBatch(item, grouped, grouped.size(), center);
    }

    /**
     * Get the count of items in a batch for stack display purposes.
     * 
     * @param item The primary item entity
     * @return Number of items in the batch, or 1 if not batched
     */
    public static int getBatchedItemCount(@Nonnull ItemEntity item) {
        if (!enabled) {
            return 1;
        }

        List<ItemEntity> grouped = batchGroups.get(item);
        if (grouped == null) {
            return 1;
        }

        // Sum up the actual stack counts
        int total = 0;
        for (ItemEntity groupedItem : grouped) {
            total += groupedItem.getItem().getCount();
        }
        return total;
    }

    /**
     * Clear cache if this is a new frame.
     */
    private static void clearCacheIfNewFrame() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        long currentTime = mc.level.getGameTime();
        if (currentTime != lastFrameTime) {
            batchGroups.clear();
            processedItems.clear();
            lastFrameTime = currentTime;
        }
    }

    /**
     * Process batching for a specific item entity.
     */
    private static void processBatchForItem(@Nonnull ItemEntity primaryItem) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            processedItems.add(primaryItem);
            return;
        }

        // Find nearby items
        Vec3 pos = primaryItem.position();
        AABB searchBox = new AABB(
                pos.x - mergeRadius, pos.y - mergeRadius, pos.z - mergeRadius,
                pos.x + mergeRadius, pos.y + mergeRadius, pos.z + mergeRadius);

        List<ItemEntity> nearbyItems = mc.level.getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                item -> !processedItems.contains(item) && item.isAlive());

        // Sort by distance to primary
        nearbyItems.sort(Comparator.comparingDouble(
                item -> item.position().distanceToSqr(pos)));

        // Limit batch size
        List<ItemEntity> batch = new ArrayList<>();
        for (ItemEntity item : nearbyItems) {
            if (batch.size() >= maxItemsPerBatch) {
                break;
            }
            batch.add(item);
            processedItems.add(item);
        }

        // Register batch with primary item
        if (!batch.isEmpty()) {
            batchGroups.put(primaryItem, batch);
        }
    }

    /**
     * Calculate the center position of a batch.
     */
    @Nonnull
    private static Vec3 calculateBatchCenter(@Nonnull List<ItemEntity> items) {
        if (items.isEmpty()) {
            return Vec3.ZERO;
        }

        double x = 0, y = 0, z = 0;
        for (ItemEntity item : items) {
            Vec3 pos = item.position();
            x += pos.x;
            y += pos.y;
            z += pos.z;
        }

        int count = items.size();
        return new Vec3(x / count, y / count, z / count);
    }

    /**
     * Get debug statistics about current batching.
     */
    @Nonnull
    public static String getDebugStats() {
        int totalBatches = batchGroups.size();
        int totalItems = processedItems.size();
        int savedRenderCalls = totalItems - totalBatches;

        return String.format("ItemBatcher: %d batches, %d items, %d render calls saved",
                totalBatches, totalItems, savedRenderCalls);
    }
}
