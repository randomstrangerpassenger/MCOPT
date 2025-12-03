package com.randomstrangerpassenger.mcopt.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Specialized version of {@link ConfigurableTickHandler} for server-side ticks.
 * Provides convenient access to server tick events with automatic phase and
 * config filtering.
 * <p>
 * Example usage:
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;EventBusSubscriber(modid = MCOPT.MOD_ID)
 *     public class MyServerHandler extends ConfigurableServerTickHandler {
 *         public MyServerHandler() {
 *             super(MCOPTConfig.ENABLE_MY_FEATURE);
 *         }
 *
 *         @Override
 *         protected void onConfiguredServerTick(TickEvent.ServerTickEvent event) {
 *             // Your server-side logic here
 *         }
 *     }
 * }
 * </pre>
 */
public abstract class ConfigurableServerTickHandler {

    private final ModConfigSpec.BooleanValue enableConfig;
    private boolean cachedEnabled;

    protected ConfigurableServerTickHandler(ModConfigSpec.BooleanValue enableConfig) {
        this.enableConfig = enableConfig;
        this.cachedEnabled = enableConfig.get();
    }

    /**
     * Refreshes the cached config value.
     * Call this when configuration is reloaded.
     */
    public void refreshConfigCache() {
        this.cachedEnabled = enableConfig.get();
    }

    @SubscribeEvent
    public final void onServerTick(ServerTickEvent.Post event) {
        if (cachedEnabled) {
            onConfiguredServerTick(event);
        }
    }

    /**
     * Called when a server tick occurs and the handler is enabled.
     *
     * @param event The server tick event
     */
    protected abstract void onConfiguredServerTick(ServerTickEvent.Post event);
}
