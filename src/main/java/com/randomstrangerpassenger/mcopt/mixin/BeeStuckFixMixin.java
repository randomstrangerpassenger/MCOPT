package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.MCOpt;
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

            if (mcopt$pathingFailTicks > 200) {
                self.setHivePos(null);
                self.setStayOutOfHiveCountdown(200);
                mcopt$pathingFailTicks = 0;
                MCOpt.LOGGER.debug("[BeeGuard] Cleared stalled hive target to prevent runaway pathing.");
            }
        }
    }
}
