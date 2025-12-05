package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.rendering.particle.ParticlePhysicsOptimizer;
import net.minecraft.client.particle.Particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Particle.class)
public abstract class ParticlePhysicsMixin {

    @Shadow
    public abstract void setPos(double x, double y, double z);

    @Shadow
    public double x;
    @Shadow
    public double y;
    @Shadow
    public double z;
    @Shadow
    public double xd;
    @Shadow
    public double yd;
    @Shadow
    public double zd;
    @Shadow
    protected boolean onGround;

    /**
     * Redirects the collision movement logic in Particle.tick().
     */
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;move(DDD)V"))
    public void redirectMove(Particle instance, double xd, double yd, double zd) {
        // Correctly pass double coordinates
        ParticlePhysicsOptimizer.PhysicsMode mode = ParticlePhysicsOptimizer.getPhysicsMode(x, y, z);

        if (mode == ParticlePhysicsOptimizer.PhysicsMode.NO_CLIP) {
            // Simple movement without collision checks
            this.setPos(x + xd, y + yd, z + zd);
        } else if (mode == ParticlePhysicsOptimizer.PhysicsMode.HEIGHT_ONLY) {
            // For now, fall back to normal movement even for HEIGHT_ONLY
            // because implementing height-only physics here is complex.
            instance.move(xd, yd, zd);
        } else {
            // Full physics
            instance.move(xd, yd, zd);
        }
    }
}
