package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import com.randomstrangerpassenger.mcopt.mixin.client.MCOptCulledParticle;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.phys.Vec3;

/**
 * Handles particle optimization logic including spawn limiting and distance
 * culling.
 * <p>
 * Separates business logic from ParticleEngineMixin to improve
 * testability and maintainability.
 */
public final class ParticleOptimizationHandler {

    private ParticleOptimizationHandler() {
        // Utility class
    }

    // State tracking for spawn limiting
    private static long lastTick = -1;
    private static int spawnedThisTick = 0;

    /**
     * Checks if a particle should be culled based on spawn limits and distance.
     * 
     * @param particle       Particle to check
     * @param cameraPosition Current camera position
     * @param currentTick    Current game tick
     * @return true if particle should be culled, false otherwise
     */
    public static boolean shouldCullParticle(
            Particle particle,
            Vec3 cameraPosition,
            long currentTick) {

        if (!RenderingConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get()) {
            return false;
        }

        // Reset spawn counter on new tick
        if (lastTick != currentTick) {
            lastTick = currentTick;
            spawnedThisTick = 0;
        }

        spawnedThisTick++;

        // Spawn limit check
        if (spawnedThisTick > MCOPTConstants.Performance.PARTICLE_SPAWN_LIMIT_PER_TICK) {
            MCOPT.LOGGER.debug("[ParticleCull] Throttled particle spawn at tick {} ({} > {})",
                    currentTick, spawnedThisTick, MCOPTConstants.Performance.PARTICLE_SPAWN_LIMIT_PER_TICK);
            return true;
        }

        // Distance culling
        MCOptCulledParticle access = (MCOptCulledParticle) particle;
        double distanceSq = access.mcopt$distanceSquared(cameraPosition);

        double cullRange = RenderingConfig.PARTICLE_CULLING_RANGE.get();
        if (distanceSq > cullRange * cullRange) {
            access.mcopt$setCulled(true);
            return true;
        }

        return false;
    }
}
