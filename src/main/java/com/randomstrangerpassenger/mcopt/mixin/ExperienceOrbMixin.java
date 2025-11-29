package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Optimizes experience orb performance by merging nearby orbs into single entities.
 * This significantly reduces entity count and improves performance when many orbs spawn.
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Shadow
    public int value;

    @Shadow
    public int count;

    @Unique
    private int mcopt$mergeCheckTimer = 0;

    /**
     * Periodically checks for nearby experience orbs and merges them to reduce entity count.
     * This is especially helpful during mob farming or mining operations.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (!FeatureToggles.isXpOrbMergingEnabled()) {
            return;
        }

        ExperienceOrb thisOrb = (ExperienceOrb) (Object) this;

        // Only process on server side or in singleplayer
        if (thisOrb.level().isClientSide) {
            return;
        }

        // Increment timer and check if it's time to merge
        mcopt$mergeCheckTimer++;
        int mergeDelay = MCOPTConfig.XP_ORB_MERGE_DELAY.get();

        if (mcopt$mergeCheckTimer < mergeDelay) {
            return;
        }

        // Reset timer
        mcopt$mergeCheckTimer = 0;

        // Don't process if this orb is already being removed
        if (!thisOrb.isAlive() || thisOrb.isRemoved()) {
            return;
        }

        // Get merge radius from config
        double mergeRadius = MCOPTConfig.XP_ORB_MERGE_RADIUS.get();

        // Create bounding box for searching nearby orbs
        AABB searchBox = thisOrb.getBoundingBox().inflate(mergeRadius);

        // Find nearby experience orbs
        List<ExperienceOrb> nearbyOrbs = thisOrb.level().getEntitiesOfClass(
                ExperienceOrb.class,
                searchBox,
                orb -> orb != thisOrb && orb.isAlive() && !orb.isRemoved()
        );

        // Merge nearby orbs into this one
        for (ExperienceOrb nearbyOrb : nearbyOrbs) {
            // Add the value of the nearby orb to this one
            this.value += nearbyOrb.value;

            // Remove the merged orb
            nearbyOrb.discard();
        }

        // If we merged any orbs, reset the count to prevent premature despawn
        if (!nearbyOrbs.isEmpty()) {
            // The count field affects despawn timing, reset it after merging
            this.count = 0;
        }
    }
}
