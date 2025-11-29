package com.randomstrangerpassenger.mcopt.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Particle.class)
public abstract class ParticleMixin implements MCOptCulledParticle {
    @Unique private boolean mcopt$culled = false;

    @Override
    public double mcopt$distanceSquared(Vec3 camera) {
        Particle self = (Particle) (Object) this;
        double dx = self.getX() - camera.x;
        double dy = self.getY() - camera.y;
        double dz = self.getZ() - camera.z;
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
