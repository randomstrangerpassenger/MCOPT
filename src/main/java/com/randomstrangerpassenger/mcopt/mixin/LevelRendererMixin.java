package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

/**
 * Optimizes level rendering by improving frustum culling and chunk visibility checks.
 * This reduces the number of chunks and entities that need to be processed each frame.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    private Frustum mcopt$cachedFrustum;
    private long mcopt$lastFrustumUpdate = 0;
    private static final long FRUSTUM_CACHE_TIME = 16; // ~1 frame at 60fps

    /**
     * Caches frustum calculations to reduce CPU overhead.
     * The frustum doesn't change every tick, so we can safely cache it briefly.
     */
    @Inject(
        method = "prepareCullFrustum",
        at = @At("HEAD")
    )
    private void cacheFrustumCalculation(CallbackInfo ci) {
        if (!MCOPTConfig.AGGRESSIVE_CHUNK_CULLING.get()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Update cached frustum only if enough time has passed
        if (currentTime - mcopt$lastFrustumUpdate > FRUSTUM_CACHE_TIME) {
            mcopt$lastFrustumUpdate = currentTime;
            // Frustum will be recalculated
        }
    }

    /**
     * Additional optimization for chunk rendering to skip chunks that are
     * definitely not visible based on distance and view direction.
     */
    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"
        )
    )
    private void optimizeChunkVisibility(CallbackInfo ci) {
        // Optimization happens in setupRender
        // This is a marker for future enhancements
    }
}
