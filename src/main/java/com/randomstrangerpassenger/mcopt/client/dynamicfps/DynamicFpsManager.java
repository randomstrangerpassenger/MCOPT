package com.randomstrangerpassenger.mcopt.client.dynamicfps;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.TickEvent;

/**
 * Lightweight controller that dynamically adjusts the client framerate limit based on window focus.
 * <p>
 * The goal is to mimic the usability of Dynamic FPS while keeping a fully independent implementation
 * that favors compatibility: no custom game loop hooks, no alterations to simulation state, and only
 * simple framerate cap adjustments on the render thread.
 */
public final class DynamicFpsManager {

    private static final int MIN_RENDER_DISTANCE = 4;
    private static final int RENDER_DISTANCE_REDUCTION = 4;

    private final Minecraft minecraft;
    private DynamicFpsState activeState;
    private int userDefinedFramerate;
    private int appliedFramerate;
    private long lastInteractionMillis;
    private int previousRenderDistance;
    private boolean renderDistanceReduced;

    public DynamicFpsManager() {
        this.minecraft = Minecraft.getInstance();
        this.userDefinedFramerate = readUserFramerateLimit();
        this.appliedFramerate = this.userDefinedFramerate;
        this.activeState = DynamicFpsState.IN_GAME;
        this.lastInteractionMillis = Util.getMillis();
        this.previousRenderDistance = -1;
        this.renderDistanceReduced = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != 0) {
            markInteraction();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != 0) {
            markInteraction();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDelta() != 0.0) {
            markInteraction();
        }
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

        if (isIdleBoostActive()) {
            return DynamicFpsState.IDLE;
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
            case IDLE -> MCOPTConfig.IDLE_FRAME_RATE_LIMIT.get();
            case IN_GAME -> userDefinedFramerate;
        };
    }

    private void applyLimit(int targetLimit, DynamicFpsState detectedState) {
        minecraft.getWindow().setFramerateLimit(Math.max(0, targetLimit));
        appliedFramerate = targetLimit;

        // Apply render distance reduction when entering idle state
        if (detectedState == DynamicFpsState.IDLE && !renderDistanceReduced) {
            applyRenderDistanceReduction();
        } else if (detectedState != DynamicFpsState.IDLE && renderDistanceReduced) {
            restoreRenderDistance();
        }

        activeState = detectedState;
        MCOPT.LOGGER.debug("Dynamic FPS state: {} (FPS cap set to {})", detectedState, targetLimit);
    }

    private void applyRenderDistanceReduction() {
        Options options = minecraft.options;
        if (options != null) {
            previousRenderDistance = options.renderDistance().get();
            int newRenderDistance = Math.max(MIN_RENDER_DISTANCE, previousRenderDistance - RENDER_DISTANCE_REDUCTION);
            options.renderDistance().set(newRenderDistance);
            renderDistanceReduced = true;
            MCOPT.LOGGER.debug("[DynamicFPS] Idle state: reduced render distance {} -> {}", previousRenderDistance, newRenderDistance);
        }
    }

    private void restoreRenderDistance() {
        if (previousRenderDistance > 0) {
            Options options = minecraft.options;
            if (options != null) {
                options.renderDistance().set(previousRenderDistance);
                renderDistanceReduced = false;
                MCOPT.LOGGER.debug("[DynamicFPS] Restored render distance to {}", previousRenderDistance);
            }
        }
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

    private boolean isIdleBoostActive() {
        if (!MCOPTConfig.ENABLE_IDLE_BOOST.get()) {
            return false;
        }

        if (!minecraft.isWindowActive() || minecraft.getWindow().isMinimized()) {
            return false;
        }

        if (minecraft.screen != null) {
            return false;
        }

        long idleDurationMillis = Util.getMillis() - lastInteractionMillis;
        long idleThresholdMillis = MCOPTConfig.IDLE_BOOST_INACTIVITY_SECONDS.get() * 1000L;
        return idleDurationMillis >= idleThresholdMillis;
    }

    private void markInteraction() {
        lastInteractionMillis = Util.getMillis();

        if (activeState == DynamicFpsState.IDLE) {
            DynamicFpsState detectedState = detectState();
            int targetLimit = resolveTargetLimit(detectedState);
            applyLimit(targetLimit, detectedState);
        }
    }

    private boolean isForegroundGameplay() {
        return minecraft.isWindowActive() && !minecraft.getWindow().isMinimized() && minecraft.screen == null;
    }
}
