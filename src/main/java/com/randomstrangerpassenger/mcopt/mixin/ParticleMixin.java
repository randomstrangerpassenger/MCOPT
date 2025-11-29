package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.client.particle.ParticleCullingAccess;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Stores per-particle occlusion state to support lightweight culling.
 */
@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleCullingAccess {

    @Shadow
    protected ClientLevel level;

    @Shadow
    protected double x;

    @Shadow
    protected double y;

    @Shadow
    protected double z;

    private boolean mcopt$occluded = false;
    private int mcopt$occlusionCooldown = 0;

    @Override
    public boolean mcopt$shouldRender(Camera camera, float partialTicks, double maxDistanceSquared, int checkInterval) {
        if (level == null) {
            return true;
        }

        if (mcopt$occlusionCooldown > 0) {
            mcopt$occlusionCooldown--;
            return !mcopt$occluded;
        }

        mcopt$occlusionCooldown = Math.max(1, checkInterval);

        Vec3 cameraPos = camera.getPosition();
        Vec3 particlePos = new Vec3(this.x, this.y, this.z);
        double distanceSquared = cameraPos.distanceToSqr(particlePos);

        // Skip occlusion checks for very distant particles to reduce overhead
        if (distanceSquared > maxDistanceSquared) {
            mcopt$occluded = false;
            return true;
        }

        ClipContext context = new ClipContext(
            cameraPos,
            particlePos,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            camera.getEntity()
        );
        BlockHitResult hitResult = level.clip(context);

        mcopt$occluded = hitResult != null
            && hitResult.getType() == HitResult.Type.BLOCK
            && hitResult.getLocation().distanceToSqr(cameraPos) + 1.0E-4 < distanceSquared;

        return !mcopt$occluded;
    }
}
