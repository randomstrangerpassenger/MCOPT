package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.server.entity.xp.XpOrbHandler;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes experience orb performance by merging nearby orbs into single entities.
 * This significantly reduces entity count and improves performance when many orbs spawn.
 * <p>
 * Business logic is delegated to {@link XpOrbHandler} for better maintainability.
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

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
        int mergeDelay = XpOrbHandler.getMergeCheckDelay();

        if (mcopt$mergeCheckTimer < mergeDelay) {
            return;
        }

        // Reset timer
        mcopt$mergeCheckTimer = 0;

        // Delegate merging logic to handler
        XpOrbHandler.mergeNearbyOrbs(thisOrb);
    }
}
