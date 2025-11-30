package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.server.entity.bee.BeeFixHandler;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents bees from getting stuck trying to pathfind to unreachable hives.
 * <p>
 * Business logic is delegated to {@link BeeFixHandler} for better maintainability.
 */
@Mixin(Bee.class)
public abstract class BeeStuckFixMixin {
    @Unique
    private int mcopt$pathingFailTicks = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mcopt$stabilityGuard(CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_BEE_STUCK_FIX.get()) {
            return;
        }

        Bee self = (Bee) (Object) this;
        if (self.level().isClientSide) {
            return;
        }

        // Delegate stuck check logic to handler
        mcopt$pathingFailTicks = BeeFixHandler.handleBeeStuckCheck(self, mcopt$pathingFailTicks);
    }
}
