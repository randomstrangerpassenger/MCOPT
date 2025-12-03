package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.rendering.ParticleOptimizationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes particle spawning and rendering.
 * <p>
 * Business logic delegated to {@link ParticleOptimizationHandler} for better
 * testability.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void mcopt$cullParticle(Particle particle, CallbackInfo ci) {
        if (particle == null || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        Vec3 camera = this.minecraft.gameRenderer.getMainCamera().getPosition();
        long tick = this.minecraft.level.getGameTime();

        if (ParticleOptimizationHandler.shouldCullParticle(particle, camera, tick)) {
            ci.cancel();
        }
    }
}
