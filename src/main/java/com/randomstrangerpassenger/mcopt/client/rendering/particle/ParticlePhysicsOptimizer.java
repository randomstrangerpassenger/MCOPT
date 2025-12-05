package com.randomstrangerpassenger.mcopt.client.rendering.particle;

import com.randomstrangerpassenger.mcopt.client.manager.AdaptiveLimitsManager;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Optimizes particle physics calculations based on distance from player.
 * 
 * <p>
 * Reduces collision detection overhead for distant particles where
 * precise physics is imperceptible to the player.
 * </p>
 * 
 * <p>
 * <strong>Physics Modes:</strong>
 * </p>
 * <ul>
 * <li>Full: All collision checks (near particles)</li>
 * <li>Simplified: Height check only or no-clip (distant particles)</li>
 * </ul>
 * 
 * <p>
 * <strong>Immersion First:</strong> Visual particle behavior remains natural.
 * Distant smoke going through walls is imperceptible.
 * </p>
 */
@SuppressWarnings("null")
public class ParticlePhysicsOptimizer {

    // Cached config values
    private static boolean enabled = true;
    private static int simplifyDistance = 16;

    /**
     * Physics mode for particles.
     */
    public enum PhysicsMode {
        /** Full collision detection */
        FULL,
        /** Height-only collision check */
        HEIGHT_ONLY,
        /** No collision (no-clip) */
        NO_CLIP
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = PerformanceConfig.ENABLE_SIMPLIFIED_PARTICLE_PHYSICS.get();
        simplifyDistance = PerformanceConfig.PARTICLE_PHYSICS_SIMPLIFY_DISTANCE.get();
    }

    /**
     * Check if particle physics optimization is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the physics mode for a particle at the given position.
     * 
     * @param x Particle X position
     * @param y Particle Y position
     * @param z Particle Z position
     * @return The appropriate physics mode
     */
    @Nonnull
    public static PhysicsMode getPhysicsMode(double x, double y, double z) {
        if (!enabled) {
            return PhysicsMode.FULL;
        }

        double distanceSq = getDistanceToPlayerSq(x, y, z);

        // Dynamically adjust distance if adaptive limits are enabled
        int effectiveDistance = simplifyDistance;
        if (AdaptiveLimitsManager.isUnderLoad()) {
            // Reduce distance by up to 50% based on stress
            float stress = AdaptiveLimitsManager.getStressFactor();
            effectiveDistance = (int) (simplifyDistance * (1.0f - (stress * 0.5f)));
            effectiveDistance = Math.max(8, effectiveDistance); // Minimum 8 blocks
        }

        int simplifyDistSq = effectiveDistance * effectiveDistance;

        if (distanceSq <= simplifyDistSq) {
            return PhysicsMode.FULL;
        }

        // For distant particles, use simplified physics
        // Height-only is a good balance between performance and visual quality
        return PhysicsMode.HEIGHT_ONLY;
    }

    /**
     * Check if collision detection should be skipped for this particle.
     * 
     * @param x Particle X position
     * @param y Particle Y position
     * @param z Particle Z position
     * @return true if collision can be skipped
     */
    public static boolean shouldSkipCollision(double x, double y, double z) {
        if (!enabled) {
            return false;
        }

        PhysicsMode mode = getPhysicsMode(x, y, z);
        return mode == PhysicsMode.NO_CLIP;
    }

    /**
     * Check if only height collision should be used.
     * 
     * @param x Particle X position
     * @param y Particle Y position
     * @param z Particle Z position
     * @return true if only height collision should be checked
     */
    public static boolean shouldUseHeightOnly(double x, double y, double z) {
        if (!enabled) {
            return false;
        }

        PhysicsMode mode = getPhysicsMode(x, y, z);
        return mode == PhysicsMode.HEIGHT_ONLY;
    }

    /**
     * Apply simplified gravity for distant particles.
     * Skips expensive lookups and uses simple downward motion.
     * 
     * @param currentY  Current Y position
     * @param velocityY Current Y velocity
     * @param gravity   Gravity value
     * @param groundY   Approximate ground level
     * @return New Y velocity after gravity
     */
    public static double applySimplifiedGravity(double currentY, double velocityY,
            double gravity, double groundY) {
        // Simple ground check
        if (currentY <= groundY) {
            return 0.0; // Stop at ground
        }

        return velocityY - gravity;
    }

    /**
     * Simple ground level estimation.
     * Uses current Y minus some offset as a quick approximation.
     */
    public static double estimateGroundLevel(double x, double y, double z) {
        // Rough estimate: assume ground is at Y=64 (sea level) or lower
        // This is intentionally simple for performance
        return Math.min(64.0, y - 16.0);
    }

    /**
     * Get squared distance from position to local player.
     */
    private static double getDistanceToPlayerSq(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return 0;
        }

        @Nonnull
        Vec3 playerPos = player.position();
        double dx = x - playerPos.x;
        double dy = y - playerPos.y;
        double dz = z - playerPos.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "ParticlePhysics: simplified beyond " + simplifyDistance + " blocks, enabled=" + enabled;
    }
}
