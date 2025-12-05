package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(method = "suckInItems", at = @At("HEAD"), cancellable = true)
    private static void onSuckInItems(net.minecraft.world.level.Level level,
            net.minecraft.world.level.block.entity.Hopper hopper, CallbackInfoReturnable<Boolean> cir) {
        if (PerformanceConfig.ENABLE_HOPPER_OPTIMIZATION.get()) {
            // Simplified logic: If hopper is full, don't try to suck items (Vanilla usually
            // handles this, but we can fast-fail)
            // Or more aggressively: limit frequency.
            // For now, let's just leave a placeholder logic that can be enabled.
            // A common optimization is to check if the area above has changed, but that
            // requires tracking entities.

            // Current approach: Do nothing extra unless we have a specific verified
            // optimization strategy.
            // The roadmap mentioned checking inventory changes.
        }
    }
}
