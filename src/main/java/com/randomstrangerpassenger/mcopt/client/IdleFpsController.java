package com.randomstrangerpassenger.mcopt.client;

import com.randomstrangerpassenger.mcopt.MCOpt;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.InputEventMouseScrolling;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

/**
 * 유휴 입력 감지를 통해 FPS, 렌더 거리 등을 자동으로 조정하여
 * 백그라운드/무입력 상태에서 자원 사용량을 줄인다.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MCOpt.MOD_ID)
public final class IdleFpsController {
    private static final long IDLE_THRESHOLD_MS = 5_000L;
    private static final int REDUCED_FPS = 30;
    private static final int MIN_RENDER_DISTANCE = 4;

    private static long lastInteraction = System.currentTimeMillis();
    private static boolean throttled = false;
    private static int previousFpsLimit = -1;
    private static int previousRenderDistance = -1;

    private IdleFpsController() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(IdleFpsController.class);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        markInteraction();
    }

    @SubscribeEvent
    public static void onMouse(InputEventMouseScrolling event) {
        markInteraction();
    }

    @SubscribeEvent
    public static void onRaw(InputEvent.RawMouseEvent event) {
        markInteraction();
    }

    private static void markInteraction() {
        lastInteraction = System.currentTimeMillis();
        if (throttled) {
            restoreClientSettings();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.isPaused()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (!throttled && now - lastInteraction >= IDLE_THRESHOLD_MS) {
            applyIdleSettings(minecraft);
        }
    }

    private static void applyIdleSettings(Minecraft minecraft) {
        throttled = true;
        previousFpsLimit = minecraft.options.framerateLimit().get();
        previousRenderDistance = minecraft.options.renderDistance().get();

        minecraft.options.framerateLimit().set(REDUCED_FPS);
        int newRenderDistance = Mth.clamp(previousRenderDistance - 4, MIN_RENDER_DISTANCE, previousRenderDistance);
        minecraft.options.renderDistance().set(newRenderDistance);
        MCOpt.LOGGER.debug("[IdleBoost] Idle state detected: FPS {} -> {}, RenderDistance {} -> {}", previousFpsLimit, REDUCED_FPS, previousRenderDistance, newRenderDistance);
    }

    private static void restoreClientSettings() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        throttled = false;
        if (previousFpsLimit > 0) {
            minecraft.options.framerateLimit().set(previousFpsLimit);
        }
        if (previousRenderDistance > 0) {
            minecraft.options.renderDistance().set(previousRenderDistance);
        }
        MCOpt.LOGGER.debug("[IdleBoost] Interaction detected: restored FPS and render distance.");
    }
}
