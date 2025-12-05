package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Handles Level of Detail (LOD) for entity animations.
 * 
 * <p>
 * Reduces animation update frequency for distant entities to save CPU cycles
 * on skeletal bone matrix calculations (Math.sin/cos).
 * </p>
 * 
 * <p>
 * <strong>LOD Tiers:</strong>
 * </p>
 * <ul>
 * <li>Near (&lt;16 blocks): Full animation every frame</li>
 * <li>Mid (16-32 blocks): Animation every 2-3 frames, reuse poses between</li>
 * <li>Far (&gt;32 blocks): Static idle pose</li>
 * </ul>
 * 
 * <p>
 * <strong>Immersion First:</strong> AI and gameplay unaffected. Only visual
 * smoothness of distant animations is reduced.
 * </p>
 */
public class AnimationLODHandler {

    // Cached config values
    private static boolean enabled = true;
    private static int nearDistance = 16;
    private static int farDistance = 32;

    // Cache for animation frame tracking
    private static final Map<Integer, AnimationState> animationStates = new WeakHashMap<>();

    /**
     * LOD tier for animation updates.
     */
    public enum LODTier {
        /** Full animation every frame */
        NEAR(1),
        /** Animation every 2-3 frames */
        MID(3),
        /** Static idle pose */
        FAR(0);

        private final int updateInterval;

        LODTier(int interval) {
            this.updateInterval = interval;
        }

        public int getUpdateInterval() {
            return updateInterval;
        }
    }

    /**
     * Tracks animation state for an entity.
     */
    private static class AnimationState {
        long lastUpdateFrame;
        float cachedLimbSwing;
        float cachedLimbSwingAmount;
        float cachedAgeInTicks;

        AnimationState(long frame) {
            this.lastUpdateFrame = frame;
        }

        void update(long frame, float limbSwing, float limbSwingAmount,
                float ageInTicks, float headYaw, float headPitch) {
            this.lastUpdateFrame = frame;
            this.cachedLimbSwing = limbSwing;
            this.cachedLimbSwingAmount = limbSwingAmount;
            this.cachedAgeInTicks = ageInTicks;

        }
    }

    /**
     * Refresh configuration cache.
     */
    public static void refreshConfigCache() {
        enabled = RenderingConfig.ENABLE_ANIMATION_LOD.get();
        nearDistance = RenderingConfig.ANIMATION_LOD_NEAR_DISTANCE.get();
        farDistance = RenderingConfig.ANIMATION_LOD_FAR_DISTANCE.get();
    }

    /**
     * Check if animation LOD is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the LOD tier for an entity based on distance.
     * 
     * @param entity The entity to check
     * @return The appropriate LOD tier
     */
    @Nonnull
    public static LODTier getLODTier(@Nonnull Entity entity) {
        if (!enabled) {
            return LODTier.NEAR;
        }

        double distanceSq = getDistanceToPlayerSq(entity);
        int nearDistSq = nearDistance * nearDistance;
        int farDistSq = farDistance * farDistance;

        if (distanceSq <= nearDistSq) {
            return LODTier.NEAR;
        } else if (distanceSq <= farDistSq) {
            return LODTier.MID;
        } else {
            return LODTier.FAR;
        }
    }

    /**
     * Check if this entity's animation should be updated this frame.
     * 
     * @param entity       The entity to check
     * @param currentFrame Current render frame number
     * @return true if animation should be recalculated
     */
    public static boolean shouldUpdateAnimation(@Nonnull LivingEntity entity, long currentFrame) {
        if (!enabled) {
            return true;
        }

        LODTier tier = getLODTier(entity);

        // Near entities always update
        if (tier == LODTier.NEAR) {
            return true;
        }

        // Far entities use static pose (never update animation math)
        if (tier == LODTier.FAR) {
            return false;
        }

        // Mid-range: check frame interval
        int entityId = entity.getId();
        @Nullable
        AnimationState state = animationStates.get(entityId);

        if (state == null) {
            animationStates.put(entityId, new AnimationState(currentFrame));
            return true;
        }

        long framesSinceUpdate = currentFrame - state.lastUpdateFrame;
        if (framesSinceUpdate >= tier.getUpdateInterval()) {
            state.lastUpdateFrame = currentFrame;
            return true;
        }

        return false;
    }

    /**
     * Store cached animation parameters for an entity.
     * Used when animation update is skipped to provide consistent values.
     */
    public static void cacheAnimationParams(@Nonnull LivingEntity entity, long currentFrame,
            float limbSwing, float limbSwingAmount, float ageInTicks,
            float headYaw, float headPitch) {
        if (!enabled) {
            return;
        }

        int entityId = entity.getId();
        @Nullable
        AnimationState state = animationStates.get(entityId);

        if (state == null) {
            state = new AnimationState(currentFrame);
            animationStates.put(entityId, state);
        }

        state.update(currentFrame, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
    }

    /**
     * Get cached limb swing value.
     */
    public static float getCachedLimbSwing(@Nonnull LivingEntity entity, float defaultValue) {
        if (!enabled) {
            return defaultValue;
        }

        @Nullable
        AnimationState state = animationStates.get(entity.getId());
        return state != null ? state.cachedLimbSwing : defaultValue;
    }

    /**
     * Get cached limb swing amount.
     */
    public static float getCachedLimbSwingAmount(@Nonnull LivingEntity entity, float defaultValue) {
        if (!enabled) {
            return defaultValue;
        }

        @Nullable
        AnimationState state = animationStates.get(entity.getId());
        return state != null ? state.cachedLimbSwingAmount : defaultValue;
    }

    /**
     * Get cached age in ticks.
     */
    public static float getCachedAgeInTicks(@Nonnull LivingEntity entity, float defaultValue) {
        if (!enabled) {
            return defaultValue;
        }

        @Nullable
        AnimationState state = animationStates.get(entity.getId());
        return state != null ? state.cachedAgeInTicks : defaultValue;
    }

    /**
     * Check if entity should use static idle pose (far LOD).
     */
    public static boolean shouldUseIdlePose(@Nonnull Entity entity) {
        if (!enabled) {
            return false;
        }

        return getLODTier(entity) == LODTier.FAR;
    }

    /**
     * Get squared distance from entity to local player.
     */
    private static double getDistanceToPlayerSq(@Nonnull Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        @Nullable
        LocalPlayer player = mc.player;
        if (player == null) {
            return 0;
        }
        return entity.distanceToSqr(player);
    }

    /**
     * Clear animation state for an entity.
     */
    public static void clearEntity(int entityId) {
        animationStates.remove(entityId);
    }

    /**
     * Clear all cached animation states.
     */
    public static void clearAll() {
        animationStates.clear();
    }

    /**
     * Get debug statistics.
     */
    @Nonnull
    public static String getDebugStats() {
        return "AnimationLOD: " + animationStates.size() + " tracked, enabled=" + enabled;
    }
}
