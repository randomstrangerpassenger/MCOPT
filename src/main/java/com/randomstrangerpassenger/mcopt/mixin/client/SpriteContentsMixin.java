package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.rendering.AnimatedTextureOptimizer;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for SpriteContents to optimize animated texture updates.
 * <p>
 * Intercepts the animation tick method to skip updates for textures
 * that are off-screen or too far away to notice the animation.
 * </p>
 * <p>
 * Immersion-first: Visible animations always update normally.
 * </p>
 */
@Mixin(SpriteContents.class)
public abstract class SpriteContentsMixin {

    /**
     * Intercepts animation tick to conditionally skip updates.
     * 
     * @param ci Callback info for cancellation
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void mcopt$conditionalAnimationTick(CallbackInfo ci) {
        if (!RenderingConfig.ENABLE_SMART_ANIMATIONS.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        // Get the texture name for this sprite - stored in field, no resource leak
        String textureName = ((SpriteContents) (Object) this).name().toString();

        // Get current game tick using local variable after null check
        net.minecraft.client.multiplayer.ClientLevel level = java.util.Objects.requireNonNull(minecraft.level,
                "Level is null");
        long currentTick = level.getGameTime();

        // Check if this animation should be updated
        if (!AnimatedTextureOptimizer.shouldUpdateAnimation(textureName, currentTick)) {
            ci.cancel();
        }
    }
}
