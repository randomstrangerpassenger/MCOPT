package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedAttribute.class)
public class RangedAttributeMixin {
    @Shadow
    @Final
    private double maxValue;

    @Inject(method = "getMaxValue", at = @At("HEAD"), cancellable = true)
    private void mcopt$expandReportedMaxValue(CallbackInfoReturnable<Double> cir) {
        if (!SafetyConfig.ENABLE_ATTRIBUTE_RANGE_EXPANSION.get()) {
            return;
        }

        double configuredLimit = SafetyConfig.ATTRIBUTE_MAX_LIMIT.get();
        if (configuredLimit > this.maxValue) {
            cir.setReturnValue(configuredLimit);
        }
    }

    @ModifyArg(method = "sanitizeValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D"), index = 2)
    private double mcopt$expandClampedMax(double originalMax) {
        if (!SafetyConfig.ENABLE_ATTRIBUTE_RANGE_EXPANSION.get()) {
            return originalMax;
        }

        double configuredLimit = SafetyConfig.ATTRIBUTE_MAX_LIMIT.get();
        double unclampedMax = Math.max(originalMax, configuredLimit);
        return Mth.clamp(unclampedMax, originalMax, Double.MAX_VALUE);
    }
}
