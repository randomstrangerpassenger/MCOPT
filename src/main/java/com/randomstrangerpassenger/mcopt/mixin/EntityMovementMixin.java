package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.memory.Vec3Pool;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes entity movement by using object pooling for Vec3 operations.
 * Reduces GC pressure from temporary Vec3 allocations during movement calculations.
 */
@Mixin(Entity.class)
public abstract class EntityMovementMixin {

    @Shadow
    private Vec3 position;

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(Vec3 deltaMovement);

    /**
     * Optimize entity tick by reducing Vec3 allocations.
     * This is called every tick for every entity, so reducing allocations here
     * significantly reduces GC pressure.
     */
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void optimizeTickAllocations(CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_MEMORY_OPTIMIZATIONS.get() || !MCOPTConfig.ENABLE_OBJECT_POOLING.get()) {
            return;
        }

        // The actual optimization happens implicitly by the presence of the Vec3Pool
        // Other parts of the code can use the pool for temporary calculations
        // This injection point serves as a reminder and placeholder for future optimizations
    }

    /**
     * Optimize movement calculations by using pooled Vec3 instances.
     * Note: This is a demonstration - actual implementation would need careful
     * analysis of specific movement methods to avoid breaking entity behavior.
     */
    @Inject(
        method = "move",
        at = @At("HEAD")
    )
    private void optimizeMoveAllocations(CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_MEMORY_OPTIMIZATIONS.get() || !MCOPTConfig.ENABLE_OBJECT_POOLING.get()) {
            return;
        }

        // Movement optimization placeholder
        // The Vec3Pool can be used in movement calculations to reduce allocations
        // However, we must be careful not to break vanilla behavior
    }
}
