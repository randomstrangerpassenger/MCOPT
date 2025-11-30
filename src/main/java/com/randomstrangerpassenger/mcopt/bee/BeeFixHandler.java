package com.randomstrangerpassenger.mcopt.bee;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

/**
 * Handles bee pathfinding stability logic.
 * Extracted from BeeStuckFixMixin to separate business logic from Mixin injection code.
 * <p>
 * Prevents bees from getting stuck trying to pathfind to unreachable hives.
 */
public final class BeeFixHandler {

    private BeeFixHandler() {
        // Utility class
    }

    /**
     * Checks if a bee is stuck trying to reach its hive and clears the stuck target if needed.
     *
     * @param bee The bee to check
     * @param pathingFailTicks The number of ticks the bee has failed to pathfind
     * @return The updated pathingFailTicks counter
     */
    public static int handleBeeStuckCheck(Bee bee, int pathingFailTicks) {
        BlockPos hive = bee.getHivePos();
        PathNavigation navigation = bee.getNavigation();

        if (hive == null || navigation == null) {
            return 0;
        }

        // Increment or reset fail counter based on navigation state
        int updatedFailTicks;
        if (navigation.isDone()) {
            updatedFailTicks = pathingFailTicks + 1;
        } else {
            updatedFailTicks = 0;
        }

        // Check if bee is stuck
        int timeout = MCOPTConfig.BEE_STUCK_TIMEOUT_TICKS.get();
        if (updatedFailTicks > timeout) {
            clearStuckHive(bee, timeout);
            return 0; // Reset counter after clearing
        }

        return updatedFailTicks;
    }

    /**
     * Clears a bee's stuck hive target and sets a cooldown.
     *
     * @param bee The bee to clear the hive target for
     * @param timeoutTicks The timeout that triggered this clear (for logging)
     */
    private static void clearStuckHive(Bee bee, int timeoutTicks) {
        int cooldown = MCOPTConfig.BEE_RELINK_COOLDOWN_TICKS.get();
        bee.setHivePos(null);
        bee.setStayOutOfHiveCountdown(cooldown);
        MCOPT.LOGGER.debug(
            "[BeeGuard] Cleared stalled hive target after {} ticks to prevent runaway pathing",
            timeoutTicks
        );
    }
}
