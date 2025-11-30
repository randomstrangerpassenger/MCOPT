package com.randomstrangerpassenger.mcopt.server.entity.portal;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static com.randomstrangerpassenger.mcopt.MCOPT.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class PortalRedirectionHandler {

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!MCOPTConfig.ENABLE_PORTAL_REDIRECT.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PortalMemoryTracker.redirectToRememberedPortal(player, event.getTo());
    }
}
