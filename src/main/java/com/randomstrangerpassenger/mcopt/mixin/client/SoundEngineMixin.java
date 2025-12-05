package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.sound.SoundCullingHandler;
import com.randomstrangerpassenger.mcopt.config.SoundConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for SoundEngine to implement sound optimization features.
 * <p>
 * Applies volume-based culling and duplicate sound limiting to reduce
 * audio processing overhead without affecting the audible experience.
 * </p>
 * <p>
 * Immersion-first: Only culls sounds that are truly inaudible or redundant.
 * </p>
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    /**
     * Intercepts sound play requests to apply optimization logic.
     * Cancels sounds that are inaudible or duplicate.
     *
     * @param soundInstance The sound to be played
     * @param ci            Callback info for cancellation
     */
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void mcopt$cullSound(SoundInstance soundInstance, CallbackInfo ci) {
        if (!SoundConfig.ENABLE_SOUND_OPTIMIZATIONS.get()) {
            return;
        }

        if (soundInstance == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        // Get listener (player) position - use local variables with requireNonNull
        // after null check
        LocalPlayer player = java.util.Objects.requireNonNull(minecraft.player, "Player is null");
        ClientLevel level = java.util.Objects.requireNonNull(minecraft.level, "Level is null");
        Vec3 listenerPos = player.position();
        long currentTick = level.getGameTime();

        // Check if sound should be culled
        if (SoundCullingHandler.shouldCullSound(soundInstance, listenerPos, currentTick)) {
            ci.cancel();
        }
    }

    /**
     * Intercepts delayed sound play requests with the same optimization logic.
     *
     * @param soundInstance The sound to be played
     * @param delay         Delay in ticks before playing
     * @param ci            Callback info for cancellation
     */
    @Inject(method = "playDelayed", at = @At("HEAD"), cancellable = true)
    private void mcopt$cullDelayedSound(SoundInstance soundInstance, int delay, CallbackInfo ci) {
        if (!SoundConfig.ENABLE_SOUND_OPTIMIZATIONS.get()) {
            return;
        }

        if (soundInstance == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        // Use local variables with requireNonNull after null check
        LocalPlayer player = java.util.Objects.requireNonNull(minecraft.player, "Player is null");
        ClientLevel level = java.util.Objects.requireNonNull(minecraft.level, "Level is null");
        Vec3 listenerPos = player.position();
        long currentTick = level.getGameTime();

        if (SoundCullingHandler.shouldCullSound(soundInstance, listenerPos, currentTick)) {
            ci.cancel();
        }
    }
}
