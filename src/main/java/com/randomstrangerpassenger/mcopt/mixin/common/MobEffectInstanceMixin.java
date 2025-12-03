package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes MobEffectInstance amplifier byte overflow issue.
 * <p>
 * <b>Problem</b>: Minecraft stores amplifier as byte in NBT, causing overflow
 * at level 128+
 * <ul>
 * <li>Level 128+ wraps to negative values (-128 to -1)</li>
 * <li>Causes effects to behave incorrectly (e.g., Haste 128 prevents mining)</li>
 * </ul>
 * <p>
 * <b>Solution</b>: Store amplifier as int in NBT instead of byte
 * <p>
 * <b>Config</b>: {@link SafetyConfig#ENABLE_POTION_LIMIT_FIX}
 */
@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin {

    @Shadow
    private int amplifier;

    /**
     * Override NBT save to store amplifier as int instead of byte.
     * This prevents overflow for amplifiers >= 128.
     */
    @Inject(
            method = "save",
            at = @At("RETURN")
    )
    private void mcopt$saveAmplifierAsInt(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        if (!SafetyConfig.ENABLE_POTION_LIMIT_FIX.get()) {
            return;
        }

        // Replace byte "Amplifier" with int "mcopt:AmplifierInt"
        // Keep both for backwards compatibility
        if (this.amplifier >= 128 || this.amplifier < -128) {
            tag.putInt("mcopt:AmplifierInt", this.amplifier);
        }
    }

    /**
     * Override NBT load to read amplifier as int if present.
     * Falls back to byte for vanilla compatibility.
     */
    @Inject(
            method = "load",
            at = @At("RETURN")
    )
    private static void mcopt$loadAmplifierAsInt(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<MobEffectInstance> cir) {
        if (!SafetyConfig.ENABLE_POTION_LIMIT_FIX.get()) {
            return;
        }

        MobEffectInstance instance = cir.getReturnValue();
        if (instance == null) {
            return;
        }

        // Check if we stored an extended int amplifier
        if (tag.contains("mcopt:AmplifierInt")) {
            int extendedAmplifier = tag.getInt("mcopt:AmplifierInt");
            // Use accessor or reflection to set the amplifier field
            ((MobEffectInstanceMixin) (Object) instance).amplifier = extendedAmplifier;
        }
    }
}
