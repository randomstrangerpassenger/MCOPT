package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.client.particle.ParticleCullingAccess;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

import java.util.Random;

/**
 * Optimizes particle rendering by limiting particle spawn rate and total particle count.
 * This prevents FPS drops in particle-heavy scenarios like explosions or rain.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    // Particle count tracking (reset every tick for frame-accurate limiting)
    private int mcopt$particlesThisTick = 0;
    private final Random mcopt$random = new Random();

    /**
     * Limits particle spawning to prevent FPS drops from excessive particles.
     * Uses probabilistic reduction to maintain visual quality while improving performance.
     *
     * Note: Counter is reset in tick() method for proper frame synchronization.
     */
    @Inject(
        method = "createParticle",
        at = @At("HEAD"),
        cancellable = true
    )
    private void limitParticleSpawning(
        ParticleOptions particleOptions,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed,
        CallbackInfo ci
    ) {
        if (!MCOPTConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get()) {
            return;
        }

        // Hard limit on particles per tick
        int maxParticles = MCOPTConfig.MAX_PARTICLES_PER_FRAME.get();
        if (mcopt$particlesThisTick >= maxParticles) {
            ci.cancel();
            return;
        }

        // Probabilistic reduction of particle spawning
        double reductionFactor = MCOPTConfig.PARTICLE_SPAWN_REDUCTION.get();
        if (reductionFactor > 0 && mcopt$random.nextDouble() < reductionFactor) {
            ci.cancel();
            return;
        }

        mcopt$particlesThisTick++;
    }

    /**
     * Resets the particle counter at the start of each tick.
     * This ensures proper synchronization with the game's update cycle.
     */
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void resetParticleCounter(CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get()) {
            return;
        }

        // Reset counter at the start of each tick for proper frame synchronization
        mcopt$particlesThisTick = 0;
    }

    /**
     * Performs lightweight occlusion culling before rendering each particle. This mirrors the
     * "pc" mod behavior by skipping particles hidden behind opaque blocks while preserving
     * vanilla visuals for visible effects.
     */
    @Redirect(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V"
        )
    )
    private void mcopt$conditionallyRenderParticle(
        Particle instance,
        PoseStack poseStack,
        BufferSource bufferSource,
        LightTexture lightTexture,
        Camera camera,
        float partialTicks
    ) {
        if (!MCOPTConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get() || !MCOPTConfig.ENABLE_PARTICLE_CULLING.get()) {
            instance.render(poseStack, bufferSource, lightTexture, camera, partialTicks);
            return;
        }

        double maxDistance = MCOPTConfig.PARTICLE_CULLING_RANGE.get();
        int checkInterval = MCOPTConfig.PARTICLE_OCCLUSION_CHECK_INTERVAL.get();
        ParticleCullingAccess access = (ParticleCullingAccess) instance;

        if (access.mcopt$shouldRender(camera, partialTicks, maxDistance * maxDistance, checkInterval)) {
            instance.render(poseStack, bufferSource, lightTexture, camera, partialTicks);
        }
    }
}
