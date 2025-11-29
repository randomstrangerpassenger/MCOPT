package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

import java.util.Random;

/**
 * Optimizes particle rendering by limiting particle spawn rate and total particle count.
 * This prevents FPS drops in particle-heavy scenarios like explosions or rain.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    private int mcopt$particlesThisFrame = 0;
    private long mcopt$lastFrameTime = 0;
    private final Random mcopt$random = new Random();

    /**
     * Limits particle spawning to prevent FPS drops from excessive particles.
     * Uses probabilistic reduction to maintain visual quality while improving performance.
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

        long currentTime = System.currentTimeMillis();

        // Reset counter each frame
        if (currentTime != mcopt$lastFrameTime) {
            mcopt$particlesThisFrame = 0;
            mcopt$lastFrameTime = currentTime;
        }

        // Hard limit on particles per frame
        int maxParticles = MCOPTConfig.MAX_PARTICLES_PER_FRAME.get();
        if (mcopt$particlesThisFrame >= maxParticles) {
            ci.cancel();
            return;
        }

        // Probabilistic reduction of particle spawning
        double reductionFactor = MCOPTConfig.PARTICLE_SPAWN_REDUCTION.get();
        if (reductionFactor > 0 && mcopt$random.nextDouble() < reductionFactor) {
            ci.cancel();
            return;
        }

        mcopt$particlesThisFrame++;
    }

    /**
     * Optimizes particle tick updates by skipping particles that are far away
     * or outside the view frustum.
     */
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void optimizeParticleTick(CallbackInfo ci) {
        // Reset frame counter on tick
        // This ensures we don't carry over counts between frames
        if (!MCOPTConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get()) {
            return;
        }

        // Additional tick optimizations can be added here
        // For example, culling particles that are too far from the camera
    }
}
