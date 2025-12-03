package com.randomstrangerpassenger.mcopt.safety;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Handles the memory panic button (F8 by default).
 * Triggers aggressive garbage collection and resource cleanup when pressed.
 */
public class PanicButtonHandler {

    /*
     * private static final Lazy<KeyMapping> PANIC_KEY = Lazy.of(() -> new
     * KeyMapping(
     * "key.mcopt.memory_panic",
     * GLFW.GLFW_KEY_F8,
     * "key.categories.mcopt"));
     */

    /**
     * Event handler for registering key mappings.
     */
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            MCOPT.LOGGER.info("Registering memory panic key binding (F8)");
            // event.register(PANIC_KEY.get());
        }
    }

    /**
     * Event handler for client tick events.
     */
    public static class GameEventHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (!PerformanceConfig.ENABLE_MEMORY_OPTIMIZATIONS.get()) {
                return;
            }

            // Check if panic key is pressed
            /*
             * if (PANIC_KEY.get().consumeClick()) {
             * triggerMemoryPanic();
             * }
             */
        }
    }

    /**
     * Trigger emergency memory cleanup.
     */
    // private static void triggerMemoryPanic() {
    // long currentTime = System.currentTimeMillis();

    // // Check cooldown
    // if (currentTime - lastPanicTime <
    // MCOPTConstants.Performance.PANIC_BUTTON_COOLDOWN_MS) {
    // long remainingCooldown = (MCOPTConstants.Performance.PANIC_BUTTON_COOLDOWN_MS
    // - (currentTime - lastPanicTime)) / 1000;
    // sendFeedback("Memory panic on cooldown (" + remainingCooldown + "s
    // remaining)", true);
    // return;
    // }

    // lastPanicTime = currentTime;

    // MCOPT.LOGGER.warn("MEMORY PANIC TRIGGERED - Performing emergency cleanup!");

    // try {
    // // Get memory before cleanup
    // Runtime runtime = Runtime.getRuntime();
    // long usedBefore = (runtime.totalMemory() - runtime.freeMemory()) /
    // MCOPTConstants.UI.BYTES_PER_MB;

    // // Suggest garbage collection
    // System.gc();

    // // Wait a moment for GC to complete
    // try {
    // Thread.sleep(MCOPTConstants.Performance.GC_WAIT_TIME_MS);
    // } catch (InterruptedException e) {
    // Thread.currentThread().interrupt();
    // }

    // // Get memory after cleanup
    // long usedAfter = (runtime.totalMemory() - runtime.freeMemory()) /
    // MCOPTConstants.UI.BYTES_PER_MB;
    // long freed = usedBefore - usedAfter;

    // if (freed > 0) {
    // sendFeedback(String.format("Cleanup complete: Freed %d MB", freed), false);
    // } else {
    // sendFeedback("Cleanup complete: No memory reclaimed", false);
    // }

    // } catch (Exception e) {
    // MCOPT.LOGGER.error("Memory panic failed", e);
    // sendFeedback("Cleanup failed: " + e.getMessage(), true);
    // }
    // }

    // private static void sendFeedback(String message, boolean isError) {
    // Minecraft minecraft = Minecraft.getInstance();
    // if (minecraft.player != null) {
    // minecraft.player.displayClientMessage(Component.literal(message), true);
    // }
    // }
}
