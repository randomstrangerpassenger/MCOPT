package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.server.entity.ai.BrainOptimizer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Brain.class)
@SuppressWarnings("null")
public class BrainMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(net.minecraft.server.level.ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        if (!BrainOptimizer.shouldTickBrain(entity)) {
            ci.cancel();
        }
    }
}
