package com.randomstrangerpassenger.mcopt.client.dynamicfps;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.TickEvent;

/**
 * Lightweight controller that dynamically adjusts the client framerate limit based on window focus.
 * <p>
 * The goal is to mimic the usability of Dynamic FPS while keeping a fully independent implementation
 * that favors compatibility: no custom game loop hooks, no alterations to simulation state, and only
 * simple framerate cap adjustments on the render thread.
 */
public final class DynamicFpsManager {

    private final Minecraft minecraft;
    private DynamicFpsState activeState;
    private int userDefinedFramerate;
    private int appliedFramerate;

    public DynamicFpsManager() {
        this.minecraft = Minecraft.getInstance();
        this.userDefinedFramerate = readUserFramerateLimit();
        this.appliedFramerate = this.userDefinedFramerate;
        this.activeState = DynamicFpsState.IN_GAME;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (minecraft.getWindow() == null) {
            return;
        }

        // Always keep the user setting in sync when the window is focused and no menu is open.
        if (isForegroundGameplay()) {
            userDefinedFramerate = readUserFramerateLimit();
        }

        if (!FeatureToggles.isDynamicFpsEnabled()) {
            restoreUserLimitIfNeeded();
            return;
        }

        DynamicFpsState detectedState = detectState();
        int targetLimit = resolveTargetLimit(detectedState);

        if (detectedState != activeState || targetLimit != appliedFramerate) {
            applyLimit(targetLimit, detectedState);
        }
    }

    private DynamicFpsState detectState() {
        if (minecraft.getWindow().isMinimized()) {
            return DynamicFpsState.MINIMIZED;
        }

        if (!minecraft.isWindowActive()) {
            return DynamicFpsState.UNFOCUSED;
        }

        Screen currentScreen = minecraft.screen;
        if (currentScreen != null) {
            return DynamicFpsState.MENU;
        }

        return DynamicFpsState.IN_GAME;
    }

    private int resolveTargetLimit(DynamicFpsState state) {
        if (!MCOPTConfig.ENABLE_BACKGROUND_THROTTLING.get()
                && (state == DynamicFpsState.UNFOCUSED || state == DynamicFpsState.MINIMIZED)) {
            return userDefinedFramerate;
        }

        return switch (state) {
            case MINIMIZED -> MCOPTConfig.MINIMIZED_FRAME_RATE_LIMIT.get();
            case UNFOCUSED -> MCOPTConfig.UNFOCUSED_FRAME_RATE_LIMIT.get();
            case MENU -> MCOPTConfig.MENU_FRAME_RATE_LIMIT.get();
            case IN_GAME -> userDefinedFramerate;
        };
    }

    private void applyLimit(int targetLimit, DynamicFpsState detectedState) {
        minecraft.getWindow().setFramerateLimit(Math.max(0, targetLimit));
        appliedFramerate = targetLimit;
        activeState = detectedState;
        MCOPT.LOGGER.debug("Dynamic FPS state: {} (FPS cap set to {})", detectedState, targetLimit);
    }

    private void restoreUserLimitIfNeeded() {
        if (appliedFramerate != userDefinedFramerate) {
            applyLimit(userDefinedFramerate, DynamicFpsState.IN_GAME);
        }
    }

    private int readUserFramerateLimit() {
        Options options = minecraft.options;
        if (options != null) {
            try {
                OptionInstance<Integer> framerateLimit = options.framerateLimit();
                return framerateLimit.get();
            } catch (Exception e) {
                MCOPT.LOGGER.debug("Failed to read options framerate limit, falling back to window state", e);
            }
        }
        return Math.max(0, minecraft.getWindow().getFramerateLimit());
    }

    private boolean isForegroundGameplay() {
        return minecraft.isWindowActive() && !minecraft.getWindow().isMinimized() && minecraft.screen == null;
    }
}
