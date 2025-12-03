package com.randomstrangerpassenger.mcopt.mixin.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Particle.class)
public abstract class ParticleMixin implements MCOptCulledParticle {
    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;

    @Unique
    private boolean mcopt$culled = false;

    @Override
    public double mcopt$distanceSquared(Vec3 camera) {
        double dx = this.x - camera.x;
        double dy = this.y - camera.y;
        double dz = this.z - camera.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public void mcopt$setCulled(boolean culled) {
        this.mcopt$culled = culled;
    }

    @Override
    public boolean mcopt$isCulled() {
        return mcopt$culled;
    }
}
