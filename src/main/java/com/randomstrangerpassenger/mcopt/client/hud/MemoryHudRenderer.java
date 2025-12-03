package com.randomstrangerpassenger.mcopt.client.hud;

import com.randomstrangerpassenger.mcopt.MCOPT;

import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import net.neoforged.bus.api.SubscribeEvent;

/**
 * Renders memory usage HUD using the modern NeoForge 1.21.10 GUI Layer system.
 * Displays RAM usage in the top-left corner of the screen.
 */
public class MemoryHudRenderer {

    /**
     * Register the memory HUD as a GUI layer.
     * This is the modern 1.21.10 approach using RegisterGuiLayersEvent.
     */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        MCOPT.LOGGER.info("Registering Memory HUD GUI layer");

        // Register our HUD layer above the vanilla debug layer
        // event.registerAbove(
        // VanillaGuiLayers.DEBUG,
        // MCOPT.MOD_ID + ":memory_hud",
        // MemoryHudRenderer::renderMemoryHud);
    }

    /**
     * Render the memory HUD.
     * This method is called every frame by the GUI layer system.
     */
    // private static void renderMemoryHud(GuiGraphics guiGraphics, DeltaTracker
    // deltaTracker) {
    // if (!PerformanceConfig.ENABLE_MEMORY_OPTIMIZATIONS.get() ||
    // !PerformanceConfig.SHOW_MEMORY_HUD.get()) {
    // return;
    // }

    // Minecraft minecraft = Minecraft.getInstance();

    // // Don't render in certain screens or when F3 is open
    // if (minecraft.getDebugOverlay().showDebugScreen()) {
    // return; // F3 debug screen is open
    // }

    // // Update memory stats periodically
    // long currentTime = System.currentTimeMillis();
    // if (currentTime - lastUpdateTime >
    // MCOPTConstants.UI.MEMORY_HUD_UPDATE_INTERVAL_MS) {
    // updateMemoryStats();
    // lastUpdateTime = currentTime;
    // }

    // // Render the HUD text
    // // Draw with shadow for better readability
    // guiGraphics.drawString(
    // minecraft.font,
    // cachedMemoryText,
    // MCOPTConstants.UI.HUD_MARGIN_X,
    // MCOPTConstants.UI.HUD_MARGIN_Y,
    // MCOPTConstants.UI.COLOR_WHITE,
    // true // Enable shadow
    // );
    // }

    /**
     * Update the cached memory statistics text.
     */

}
