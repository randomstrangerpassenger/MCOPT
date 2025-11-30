package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bee.class)
public abstract class BeeStuckFixMixin {
    @Unique private int mcopt$pathingFailTicks = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mcopt$stabilityGuard(CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_BEE_STUCK_FIX.get()) {
            return;
        }

        Bee self = (Bee) (Object) this;
        if (self.level().isClientSide) {
            return;
        }

        BlockPos hive = self.getHivePos();
        PathNavigation navigation = self.getNavigation();
        if (hive != null && navigation != null) {
            if (navigation.isDone()) {
                mcopt$pathingFailTicks++;
            } else {
                mcopt$pathingFailTicks = 0;
            }

            int timeout = MCOPTConfig.BEE_STUCK_TIMEOUT_TICKS.get();
            if (mcopt$pathingFailTicks > timeout) {
                int cooldown = MCOPTConfig.BEE_RELINK_COOLDOWN_TICKS.get();
                self.setHivePos(null);
                self.setStayOutOfHiveCountdown(cooldown);
                mcopt$pathingFailTicks = 0;
                MCOPT.LOGGER.debug("[BeeGuard] Cleared stalled hive target after {} ticks to prevent runaway pathing", timeout);
            }
        }
    }
}
