package com.randomstrangerpassenger.mcopt.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Abstract base class for tick-based event handlers that can be toggled via
 * configuration.
 */
public abstract class ConfigurableTickHandler {

    private final ModConfigSpec.BooleanValue enableConfig;
    private boolean cachedEnabled;

    protected ConfigurableTickHandler(ModConfigSpec.BooleanValue enableConfig) {
        this.enableConfig = enableConfig;
        this.cachedEnabled = enableConfig.get();
    }

    public void refreshConfigCache() {
        this.cachedEnabled = enableConfig.get();
    }

    @SubscribeEvent
    public final void onTick(ServerTickEvent.Post event) {
        if (!cachedEnabled) {
            return;
        }
        onConfiguredTick(event);
    }

    protected abstract void onConfiguredTick(ServerTickEvent.Post event);
}
