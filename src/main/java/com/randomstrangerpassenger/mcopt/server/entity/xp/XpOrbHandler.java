package com.randomstrangerpassenger.mcopt.server.entity.xp;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Handles experience orb merging logic.
 * Extracted from ExperienceOrbMixin to separate business logic from Mixin injection code.
 */
public final class XpOrbHandler {

    // Cache config values to avoid repeated .get() calls
    private static double cachedMergeRadius = MCOPTConfig.XP_ORB_MERGE_RADIUS.get();
    private static int cachedMergeDelay = MCOPTConfig.XP_ORB_MERGE_DELAY.get();

    private XpOrbHandler() {
        // Utility class
    }

    /**
     * Refreshes cached config values.
     * Call this when config is reloaded.
     */
    public static void refreshConfigCache() {
        cachedMergeRadius = MCOPTConfig.XP_ORB_MERGE_RADIUS.get();
        cachedMergeDelay = MCOPTConfig.XP_ORB_MERGE_DELAY.get();
    }

    /**
     * Attempts to merge nearby experience orbs into the given orb.
     *
     * @param targetOrb The orb that will absorb nearby orbs
     * @return The number of orbs that were merged
     */
    public static int mergeNearbyOrbs(ExperienceOrb targetOrb) {
        // Don't process if this orb is already being removed
        if (!targetOrb.isAlive() || targetOrb.isRemoved()) {
            return 0;
        }

        // Create bounding box for searching nearby orbs using cached radius
        AABB searchBox = targetOrb.getBoundingBox().inflate(cachedMergeRadius);

        // Find nearby experience orbs
        List<ExperienceOrb> nearbyOrbs = targetOrb.level().getEntitiesOfClass(
                ExperienceOrb.class,
                searchBox,
                orb -> orb != targetOrb && orb.isAlive() && !orb.isRemoved()
        );

        // Merge nearby orbs into the target one
        int mergedCount = 0;
        for (ExperienceOrb nearbyOrb : nearbyOrbs) {
            // Add the value of the nearby orb to the target
            int targetValue = targetOrb.getValue();
            int nearbyValue = nearbyOrb.getValue();
            targetOrb.value = targetValue + nearbyValue;

            // Remove the merged orb
            nearbyOrb.discard();
            mergedCount++;
        }

        // If we merged any orbs, reset the count to prevent premature despawn
        if (mergedCount > 0) {
            // The count field affects despawn timing, reset it after merging
            targetOrb.count = 0;
        }

        return mergedCount;
    }

    /**
     * Gets the configured merge check delay in ticks.
     *
     * @return The number of ticks to wait between merge checks
     */
    public static int getMergeCheckDelay() {
        return cachedMergeDelay;
    }
}
