package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.fixes.ItemFrameSilenceFix;
import net.minecraft.world.entity.decoration.ItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ItemFrame Mixin for silencing placement sounds during chunk loading.
 * <p>
 * When chunks are loaded or worlds are generated, item frames that were previously
 * placed trigger their placement sound unnecessarily. This can be very annoying
 * when exploring areas with many item frames.
 * <p>
 * <b>Problem</b>: Item frame placement sound plays when loading chunks containing item frames
 * <p>
 * <b>Solution</b>: Cancel the sound if the entity is in its first tick (just loaded from chunk)
 *
 * @see ItemFrameSilenceFix
 * @see GameplayConfig#ENABLE_ITEM_FRAME_SILENCE
 */
@Mixin(ItemFrame.class)
public class ItemFrameSilenceMixin {

    /**
     * Intercept placement sound and cancel if entity was just loaded.
     * <p>
     * This injection runs at the HEAD of playPlacementSound, allowing us to
     * cancel the method entirely if we determine the entity was just loaded
     * from chunk data rather than placed by a player.
     */
    @Inject(method = "playPlacementSound", at = @At("HEAD"), cancellable = true)
    private void mcopt$silenceOnChunkLoad(CallbackInfo ci) {
        // Feature toggle check
        if (!GameplayConfig.ENABLE_ITEM_FRAME_SILENCE.get()) {
            return;
        }

        ItemFrame self = (ItemFrame) (Object) this;

        // If entity was just loaded (tickCount == 0 or firstTick), silence the sound
        if (!ItemFrameSilenceFix.shouldPlaySound(self)) {
            ci.cancel();
        }
    }
}
