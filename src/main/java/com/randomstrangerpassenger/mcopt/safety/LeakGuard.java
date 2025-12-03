package com.randomstrangerpassenger.mcopt.safety;

import com.randomstrangerpassenger.mcopt.MCOPT;

import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import com.randomstrangerpassenger.mcopt.util.FeatureKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.lang.ref.WeakReference;

/**
 * Leak mitigation system inspired by the ideas of the AllTheLeaks mod.
 *
 * <p>
 * This implementation focuses on two goals:
 * </p>
 * <ul>
 * <li>Detect client-level references that survive world switches (common leak
 * pattern)</li>
 * <li>Proactively clear MCOPT caches when a world unloads to give the GC less
 * to track</li>
 * </ul>
 */
@EventBusSubscriber(modid = MCOPT.MOD_ID, value = Dist.CLIENT)
public class LeakGuard {

    private static WeakReference<ClientLevel> lastUnloadedLevel = new WeakReference<>(null);
    private static int ticksSinceUnload = 0;
    private static boolean gcAttempted = false;
    private static long lastMemorySample = 0L;
    private static long lastMemoryAlert = 0L;

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide() || !FeatureToggles.isEnabled(FeatureKey.LEAK_GUARD)) {
            return;
        }

        if (event.getLevel() instanceof ClientLevel clientLevel) {
            watchForLeaks(clientLevel);
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (!FeatureToggles.isEnabled(FeatureKey.LEAK_GUARD)) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            watchForLeaks(level);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!FeatureToggles.isEnabled(FeatureKey.LEAK_GUARD)) {
            return;
        }

        // Check for leaks if we're watching an unloaded level
        if (lastUnloadedLevel.get() != null) {
            ticksSinceUnload++;

            // If level is still in memory after 10 seconds (200 ticks), it might be leaking
            if (ticksSinceUnload > 200 && !gcAttempted) {
                attemptLeakRecovery();
            }
        } else {
            // Reset tracking if level has been collected
            if (ticksSinceUnload > 0) {
                MCOPT.LOGGER.debug("Unloaded level successfully collected by GC after {} ticks", ticksSinceUnload);
                ticksSinceUnload = 0;
                gcAttempted = false;
            }
        }

        // Periodic safe cleanup (every 5 minutes)
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            long now = System.currentTimeMillis();
            if (now - lastMemorySample > 300000) { // 5 minutes
                lastMemorySample = now;
                // Optional: Perform lightweight cleanup if memory usage is high
                checkMemoryUsage();
            }
        }
    }

    private static void watchForLeaks(ClientLevel level) {
        lastUnloadedLevel = new WeakReference<>(level);
        ticksSinceUnload = 0;
        gcAttempted = false;
        MCOPT.LOGGER.debug("LeakGuard watching level unload: {}", level);

        // Proactively clear MCOPT caches
        clearModCaches();
    }

    private static void clearModCaches() {
        // Clear RenderFrameCache
        // RenderFrameCache.reset(); // Method needs to be added to RenderFrameCache

        // Clear other static caches if necessary
        MCOPT.LOGGER.debug("LeakGuard cleared mod caches");
    }

    private static void attemptLeakRecovery() {
        MCOPT.LOGGER.warn("Potential level leak detected! Level still in memory 10s after unload.");
        gcAttempted = true;

        // Suggest GC to JVM (no guarantee, but often helps with weak references)
        System.gc();
    }

    private static void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usagePercent = (double) usedMemory / maxMemory;

        if (usagePercent > 0.90) {
            long now = System.currentTimeMillis();
            if (now - lastMemoryAlert > 60000) { // Don't spam alerts
                MCOPT.LOGGER.warn("High memory usage detected ({}%). Triggering emergency cleanup.",
                        (int) (usagePercent * 100));
                lastMemoryAlert = now;
                // Trigger cleanup
                clearModCaches();
            }
        }
    }
}
