package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.server.ai.AIOptimizationSystem;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

/**
 * Mixin to inject AI optimization processing after mob AI goals are registered.
 *
 * This is the key integration point that applies all AI optimizations:
 * - LookControl replacement
 * - AI goal filtering and removal
 * - Entity-specific optimizations
 *
 * Unlike AI-Improvements which used EntityJoinWorldEvent, we use Mixin for better
 * performance and more precise control over when optimizations are applied.
 */
@Mixin(Mob.class)
public class MobAIMixin {

    /**
     * Inject after registerGoals() completes to process and optimize the mob's AI.
     *
     * This injection point is perfect because:
     * - All vanilla AI goals have been registered
     * - Other mods haven't had a chance to modify goals yet (if they use events)
     * - We can modify the goals before the first AI tick
     */
    @Inject(
            method = "registerGoals",
            at = @At("RETURN")
    )
    private void onRegisterGoals(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        // Apply AI optimizations if system is initialized
        if (AIOptimizationSystem.isInitialized()) {
            AIOptimizationSystem.processMobGoals(mob);
        }
    }
}
