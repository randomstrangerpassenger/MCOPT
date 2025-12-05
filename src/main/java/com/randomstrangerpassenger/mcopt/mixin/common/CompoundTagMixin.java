package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Optimizes CompoundTag operations by caching hash codes.
 * This speeds up ItemStack comparisons and map lookups involving NBT.
 */
@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin {

    @Shadow
    public abstract Map<String, ?> tags();

    @Unique
    private int cachedHash = 0;

    @Inject(method = "hashCode", at = @At("HEAD"), cancellable = true)
    public void onHashCode(CallbackInfoReturnable<Integer> cir) {
        if (PerformanceConfig.ENABLE_NBT_HASH_CACHING.get()) {
            if (this.cachedHash != 0) {
                cir.setReturnValue(this.cachedHash);
            }
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"))
    public void onHashCodeReturn(CallbackInfoReturnable<Integer> cir) {
        if (PerformanceConfig.ENABLE_NBT_HASH_CACHING.get()) {
            this.cachedHash = cir.getReturnValue();
        }
    }

    // Invalidate cache on modification
    // Note: CompoundTag is mutable. We must clear cache on put usage.
    // However, Mixin injection into all put methods is tedious.
    // A better approach for full validity is to hook into `put` and `remove`
    // methods.

    @Inject(method = "put", at = @At("HEAD"))
    public void onPut(String key, net.minecraft.nbt.Tag tag, CallbackInfoReturnable<net.minecraft.nbt.Tag> cir) {
        this.cachedHash = 0;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onRemove(String key, CallbackInfoReturnable<Void> cir) {
        this.cachedHash = 0;
    }
}
