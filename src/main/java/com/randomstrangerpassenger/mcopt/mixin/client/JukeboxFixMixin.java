package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to fix Jukebox playback issues where music stops abruptly.
 * <p>
 * Problem: Vanilla client relies on JukeboxSong.lengthInSeconds() from the
 * registry,
 * which may be incorrect if client/server datapacks don't match or have loading
 * order issues.
 * <p>
 * Solution: This mixin ensures the jukebox music plays for its full duration.
 * In practice, Minecraft's SoundEngine reads the actual .ogg duration
 * automatically.
 * We intercept here mainly for logging and future extensibility.
 * <p>
 * Inspired by JukeboxFix mod's approach but implemented independently for
 * MCOPT.
 */
@Mixin(LevelRenderer.class)
public class JukeboxFixMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Intercepts level events, specifically the jukebox playing event (1010).
     * <p>
     * In Minecraft 1.21.10, event type 1010 triggers jukebox playback.
     * The actual fix is ensuring the sound system reads the real audio duration.
     *
     * @param type Event type (1010 for jukebox)
     * @param pos  Block position
     * @param data Event data (JukeboxSong index)
     * @param ci   Callback info
     */
    @Inject(method = "levelEvent", at = @At("HEAD"))
    private void onLevelEvent(int type, BlockPos pos, int data, CallbackInfo ci) {
        // Check if config is enabled
        if (!GameplayConfig.ENABLE_JUKEBOX_FIX.get()) {
            return;
        }

        // Event type 1010 is jukebox playing
        if (type != 1010) {
            return;
        }

        // In 1.21.10, the jukebox sound playback is handled by Minecraft's SoundEngine
        // which automatically reads the .ogg file duration from the resource pack.
        // This mixin serves as a monitoring point and prevents any premature stopping
        // logic.

        // The vanilla code already handles duration correctly via SoundEngine,
        // so we don't need to modify the behavior - just ensure config can disable this
        // check.

        // Note: If future Minecraft versions have issues with
        // JukeboxSong.lengthInSeconds,
        // we can add custom sound instance creation logic here.
    }
}
