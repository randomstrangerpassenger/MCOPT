package com.randomstrangerpassenger.mcopt.client.hud;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Renders memory usage HUD using the modern NeoForge 1.21.10 GUI Layer system.
 * Displays RAM usage in the top-left corner of the screen.
 */
@EventBusSubscriber(modid = MCOPT.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MemoryHudRenderer {

    private static long lastUpdateTime = 0;
    private static String cachedMemoryText = "";

    /**
     * Register the memory HUD as a GUI layer.
     * This is the modern 1.21.10 approach using RegisterGuiLayersEvent.
     */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        MCOPT.LOGGER.info("Registering Memory HUD GUI layer");

        // Register our HUD layer above the vanilla debug layer
        event.registerAbove(
            VanillaGuiLayers.DEBUG_TEXT,
            MCOPT.MOD_ID + ":memory_hud",
            MemoryHudRenderer::renderMemoryHud
        );
    }

    /**
     * Render the memory HUD.
     * This method is called every frame by the GUI layer system.
     */
    private static void renderMemoryHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!MCOPTConfig.ENABLE_MEMORY_OPTIMIZATIONS.get() || !MCOPTConfig.SHOW_MEMORY_HUD.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        // Don't render in certain screens or when F3 is open
        if (minecraft.options.renderDebug) {
            return; // F3 debug screen is open
        }

        // Update memory stats periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > MCOPTConstants.UI.MEMORY_HUD_UPDATE_INTERVAL_MS) {
            updateMemoryStats();
            lastUpdateTime = currentTime;
        }

        // Render the HUD text
        // Draw with shadow for better readability
        guiGraphics.drawString(
            minecraft.font,
            cachedMemoryText,
            MCOPTConstants.UI.HUD_MARGIN_X,
            MCOPTConstants.UI.HUD_MARGIN_Y,
            MCOPTConstants.UI.COLOR_WHITE,
            true // Enable shadow
        );
    }

    /**
     * Update the cached memory statistics text.
     */
    private static void updateMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // Convert to MB for readability
        long usedMB = usedMemory / MCOPTConstants.UI.BYTES_PER_MB;
        long maxMB = maxMemory / MCOPTConstants.UI.BYTES_PER_MB;

        // Calculate percentage
        int percentage = (int) ((usedMemory * 100) / maxMemory);

        // Format the text
        cachedMemoryText = String.format("RAM: %d / %d MB (%d%%)", usedMB, maxMB, percentage);
    }
}
