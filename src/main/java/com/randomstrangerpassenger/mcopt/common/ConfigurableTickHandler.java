package com.randomstrangerpassenger.mcopt.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * Abstract base class for tick-based event handlers that can be toggled via configuration.
 * This class handles common patterns found across many MCOPT handlers:
 * - Phase filtering (typically END phase)
 * - Configuration-based enable/disable
 * - Cached config values to avoid repeated .get() calls
 * <p>
 * Subclasses only need to implement the actual tick logic in {@link #onConfiguredTick}.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyFeatureHandler extends ConfigurableTickHandler {
 *     public MyFeatureHandler() {
 *         super(MCOPTConfig.ENABLE_MY_FEATURE, TickEvent.Phase.END);
 *     }
 *
 *     @Override
 *     protected void onConfiguredTick(TickEvent event) {
 *         // Your feature logic here
 *     }
 * }
 * }</pre>
 */
public abstract class ConfigurableTickHandler {

    private final ModConfigSpec.BooleanValue enableConfig;
    private final TickEvent.Phase requiredPhase;
    private boolean cachedEnabled;

    /**
     * Creates a new configurable tick handler.
     *
     * @param enableConfig  The configuration value that controls whether this handler is enabled
     * @param requiredPhase The tick phase this handler should run on (typically Phase.END)
     */
    protected ConfigurableTickHandler(ModConfigSpec.BooleanValue enableConfig, TickEvent.Phase requiredPhase) {
        this.enableConfig = enableConfig;
        this.requiredPhase = requiredPhase;
        this.cachedEnabled = enableConfig.get();
    }

    /**
     * Refreshes the cached config value.
     * Call this when configuration is reloaded.
     */
    public void refreshConfigCache() {
        this.cachedEnabled = enableConfig.get();
    }

    /**
     * Main event handler that performs common checks before delegating to subclass.
     */
    @SubscribeEvent
    public final void onTick(TickEvent event) {
        // Check if this is the correct phase
        if (event.phase != requiredPhase) {
            return;
        }

        // Check if the feature is enabled
        if (!cachedEnabled) {
            return;
        }

        // Delegate to subclass implementation
        onConfiguredTick(event);
    }

    /**
     * Subclasses implement this method with their specific tick logic.
     * This method is only called when:
     * - The event phase matches the required phase
     * - The feature is enabled in configuration
     *
     * @param event The tick event
     */
    protected abstract void onConfiguredTick(TickEvent event);
}
