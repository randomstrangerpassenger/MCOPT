package com.randomstrangerpassenger.mcopt.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.memory.BlockPosPool;
import com.randomstrangerpassenger.mcopt.memory.Vec3Pool;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

/**
 * Handles the memory panic button (F8 by default).
 * Triggers aggressive garbage collection and resource cleanup when pressed.
 */
public class PanicButtonHandler {

    private static final Lazy<KeyMapping> PANIC_KEY = Lazy.of(() ->
        new KeyMapping(
            "key.mcopt.memory_panic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "key.categories.mcopt"
        )
    );

    private static long lastPanicTime = 0;
    private static final long PANIC_COOLDOWN_MS = 5000; // 5 seconds cooldown

    /**
     * Event handler for registering key mappings.
     */
    @EventBusSubscriber(modid = MCOPT.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            MCOPT.LOGGER.info("Registering memory panic key binding (F8)");
            event.register(PANIC_KEY.get());
        }
    }

    /**
     * Event handler for client tick events.
     */
    @EventBusSubscriber(modid = MCOPT.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEventHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (!MCOPTConfig.ENABLE_MEMORY_OPTIMIZATIONS.get()) {
                return;
            }

            // Check if panic key is pressed
            if (PANIC_KEY.get().consumeClick()) {
                triggerMemoryPanic();
            }
        }
    }

    /**
     * Trigger emergency memory cleanup.
     */
    private static void triggerMemoryPanic() {
        long currentTime = System.currentTimeMillis();

        // Check cooldown
        if (currentTime - lastPanicTime < PANIC_COOLDOWN_MS) {
            long remainingCooldown = (PANIC_COOLDOWN_MS - (currentTime - lastPanicTime)) / 1000;
            sendFeedback("Memory panic on cooldown (" + remainingCooldown + "s remaining)", true);
            return;
        }

        lastPanicTime = currentTime;

        MCOPT.LOGGER.warn("MEMORY PANIC TRIGGERED - Performing emergency cleanup!");

        try {
            // Get memory before cleanup
            Runtime runtime = Runtime.getRuntime();
            long usedBefore = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

            // Clear object pools
            Vec3Pool.clear();
            BlockPosPool.clear();

            // Suggest garbage collection
            System.gc();

            // Wait a moment for GC to complete
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Get memory after cleanup
            long usedAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long freed = usedBefore - usedAfter;

            // Send feedback to player
            String message = String.format("Memory cleanup complete! Freed ~%d MB", Math.max(0, freed));
            sendFeedback(message, false);

            MCOPT.LOGGER.info("Memory panic completed: {} MB -> {} MB (freed {} MB)",
                usedBefore, usedAfter, freed);

        } catch (Exception e) {
            MCOPT.LOGGER.error("Error during memory panic", e);
            sendFeedback("Memory cleanup failed - check logs", true);
        }
    }

    /**
     * Send feedback message to the player.
     */
    private static void sendFeedback(String message, boolean isError) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            Component component = Component.literal("[MCOPT] " + message);
            minecraft.player.displayClientMessage(component, false);
        }
    }
}
