package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.MCOPT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    private static final double CULL_DISTANCE_SQ = 48 * 48;
    private static final int PER_TICK_SPAWN_LIMIT = 256;

    @Shadow @Final private Minecraft minecraft;

    @Unique private long mcopt$lastTick = -1;
    @Unique private int mcopt$spawnedThisTick = 0;

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void mcopt$cullParticle(Particle particle, CallbackInfo ci) {
        if (particle == null || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        long tick = this.minecraft.level.getGameTime();
        if (mcopt$lastTick != tick) {
            mcopt$lastTick = tick;
            mcopt$spawnedThisTick = 0;
        }

        mcopt$spawnedThisTick++;
        if (mcopt$spawnedThisTick > PER_TICK_SPAWN_LIMIT) {
            ci.cancel();
            MCOPT.LOGGER.debug("[ParticleCull] Throttled particle spawn at tick {} ({} > {})", tick, mcopt$spawnedThisTick, PER_TICK_SPAWN_LIMIT);
            return;
        }

        Vec3 camera = this.minecraft.gameRenderer.getMainCamera().getPosition();
        MCOptCulledParticle access = (MCOptCulledParticle) particle;
        double distanceSq = access.mcopt$distanceSquared(camera);

        if (distanceSq > CULL_DISTANCE_SQ) {
            access.mcopt$setCulled(true);
            ci.cancel();
        }
    }
}
