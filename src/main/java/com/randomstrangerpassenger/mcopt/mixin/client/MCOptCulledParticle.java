package com.randomstrangerpassenger.mcopt.mixin.client;

import net.minecraft.world.phys.Vec3;

public interface MCOptCulledParticle {
    double mcopt$distanceSquared(Vec3 camera);
    void mcopt$setCulled(boolean culled);
    boolean mcopt$isCulled();
}
