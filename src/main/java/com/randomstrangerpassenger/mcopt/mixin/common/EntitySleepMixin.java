package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.server.entity.ai.EntitySleepManager;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class EntitySleepMixin {

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void onServerAiStep(CallbackInfo ci) {
        if (EntitySleepManager.shouldSleep((Mob) (Object) this)) {
            ci.cancel();
        }
    }
}
