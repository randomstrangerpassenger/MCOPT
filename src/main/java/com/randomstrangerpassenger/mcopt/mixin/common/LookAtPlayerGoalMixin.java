package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.client.entity.VisibilityOptimizer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for LookAtPlayerGoal to skip visual interaction when entity is not
 * visible.
 * 
 * <p>
 * This optimization skips the "look at player" behavior for entities that are:
 * <ul>
 * <li>Behind walls (occluded)</li>
 * <li>Outside the player's view frustum</li>
 * <li>Far from the player</li>
 * </ul>
 * 
 * <p>
 * <strong>Immersion First:</strong> AI pathfinding and combat is NOT affected.
 * Only the visual "looking" behavior is skipped.
 * </p>
 */
@Mixin(LookAtPlayerGoal.class)
public class LookAtPlayerGoalMixin {

    @Shadow
    @Final
    protected Mob mob;

    /**
     * Inject into canUse to skip the goal entirely when entity is not visible.
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void mcopt$skipWhenNotVisible(CallbackInfoReturnable<Boolean> cir) {
        if (!VisibilityOptimizer.isEnabled()) {
            return;
        }

        // Check if this mob should execute visual goals
        // Mob extends LivingEntity so this is safe
        if (mob != null && !VisibilityOptimizer.shouldExecuteVisualGoal(mob)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Inject into canContinueToUse to stop the goal when entity becomes
     * non-visible.
     */
    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void mcopt$stopWhenNotVisible(CallbackInfoReturnable<Boolean> cir) {
        if (!VisibilityOptimizer.isEnabled()) {
            return;
        }

        // Stop looking if player can no longer see this mob
        if (mob != null && !VisibilityOptimizer.shouldExecuteVisualGoal(mob)) {
            cir.setReturnValue(false);
        }
    }
}
