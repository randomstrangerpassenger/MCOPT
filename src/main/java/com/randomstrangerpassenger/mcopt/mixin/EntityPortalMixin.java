package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures passengers travel through dimensional portals together with their vehicle.
 * Vanilla sometimes updates portal logic only for the root vehicle, leaving riders behind.
 * This mixin mirrors the portal entry handling to all passengers so they follow the mount.
 */
@Mixin(Entity.class)
public abstract class EntityPortalMixin {

    @Inject(method = "handleInsidePortal", at = @At("TAIL"))
    private void mcopt$syncPassengerPortals(BlockPos portalPos, CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_PASSENGER_PORTAL_FIX.get()) {
            return;
        }

        Entity entity = (Entity) (Object) this;

        if (entity.level().isClientSide || entity.getPassengers().isEmpty()) {
            return;
        }

        for (Entity passenger : entity.getPassengers()) {
            if (!passenger.isRemoved() && passenger.level() == entity.level()) {
                passenger.handleInsidePortal(portalPos);
            }
        }
    }
}
