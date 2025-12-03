package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.fixes.DamageTiltFix;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.damagesource.DamageSource;

/**
 * LocalPlayer Mixin for damage tilt direction fix.
 * <p>
 * Fixes incorrect camera tilt when taking damage with invalid Yaw values.
 * <p>
 * <b>Problem</b>: Server damage packets with Yaw=0 or NaN cause incorrect tilt
 * direction
 * <p>
 * <b>Solution</b>: Detect invalid Yaw and replace with random or calculated
 * direction
 *
 * @see DamageTiltFix
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow
    public float hurtDir;

    /**
     * Fix camera tilt direction after taking damage.
     * <p>
     * Intercepts the hurt method to correct invalid Yaw values before they
     * affect the camera tilt animation.
     */
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", shift = At.Shift.BEFORE))
    private void mcopt$fixDamageTiltDirection(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!GameplayConfig.ENABLE_DAMAGE_TILT_FIX.get()) {
            return;
        }

        // Check if hurtDir is invalid
        if (!DamageTiltFix.isValidYaw(this.hurtDir)) {
            // Replace with random direction (fallback)
            this.hurtDir = DamageTiltFix.getRandomYaw();
        }
    }
}
