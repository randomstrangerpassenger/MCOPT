package com.randomstrangerpassenger.mcopt.client.rendering.particle;

import net.minecraft.client.Camera;

/**
 * Simple access interface for particle occlusion tracking.
 *
 * <p>Implemented via a Mixin on {@link net.minecraft.client.particle.Particle} to store
 * per-particle occlusion state and cache durations. The goal is to mimic the behavior of
 * external particle-culling mods while keeping implementation lightweight and fully
 * client-side.</p>
 */
public interface ParticleCullingAccess {

    /**
     * Determines whether the particle should render this frame.
     *
     * @param camera            active camera used for visibility testing
     * @param partialTicks      current partial tick value
     * @param maxDistanceSquared squared distance threshold after which occlusion is skipped
     * @param checkInterval     how many render calls to wait before re-running occlusion tests
     * @return {@code true} if the particle should render; {@code false} if culled
     */
    boolean mcopt$shouldRender(Camera camera, float partialTicks, double maxDistanceSquared, int checkInterval);
}
