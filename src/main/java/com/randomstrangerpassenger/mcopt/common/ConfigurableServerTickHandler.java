package com.randomstrangerpassenger.mcopt.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * Specialized version of {@link ConfigurableTickHandler} for server-side ticks.
 * Provides convenient access to server tick events with automatic phase and config filtering.
 * <p>
 * Example usage:
 * <pre>{@code
 * @EventBusSubscriber(modid = MCOPT.MOD_ID)
 * public class MyServerHandler extends ConfigurableServerTickHandler {
 *     public MyServerHandler() {
 *         super(MCOPTConfig.ENABLE_MY_FEATURE);
 *     }
 *
 *     @Override
 *     protected void onConfiguredServerTick(TickEvent.ServerTickEvent event) {
 *         // Your server-side logic here
 *     }
 * }
 * }</pre>
 */
public abstract class ConfigurableServerTickHandler {

    private final ModConfigSpec.BooleanValue enableConfig;
    private final TickEvent.Phase requiredPhase;
    private boolean cachedEnabled;

    /**
     * Creates a new server tick handler that runs on Phase.END.
     *
     * @param enableConfig The configuration value that controls whether this handler is enabled
     */
    protected ConfigurableServerTickHandler(ModConfigSpec.BooleanValue enableConfig) {
        this(enableConfig, TickEvent.Phase.END);
    }

    /**
     * Creates a new server tick handler with a specific phase.
     *
     * @param enableConfig  The configuration value that controls whether this handler is enabled
     * @param requiredPhase The tick phase this handler should run on
     */
    protected ConfigurableServerTickHandler(ModConfigSpec.BooleanValue enableConfig, TickEvent.Phase requiredPhase) {
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
    public final void onServerTick(TickEvent.ServerTickEvent event) {
        // Check if this is the correct phase
        if (event.phase != requiredPhase) {
            return;
        }

        // Check if the feature is enabled
        if (!cachedEnabled) {
            return;
        }

        // Delegate to subclass implementation
        onConfiguredServerTick(event);
    }

    /**
     * Subclasses implement this method with their specific server tick logic.
     * This method is only called when:
     * - The event phase matches the required phase (default: Phase.END)
     * - The feature is enabled in configuration
     *
     * @param event The server tick event
     */
    protected abstract void onConfiguredServerTick(TickEvent.ServerTickEvent event);
}
