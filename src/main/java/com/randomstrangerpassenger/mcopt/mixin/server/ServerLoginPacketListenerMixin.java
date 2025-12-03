package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to extend login timeout for heavy modpacks.
 * <p>
 * In heavy modpacks, clients may take longer than 30 seconds to complete
 * the login handshake, causing "Timed Out" errors. This mixin allows
 * configuring a longer timeout period.
 * <p>
 * Inspired by various server-side timeout fixes, but implemented independently
 * for NeoForge with full configurability.
 */
@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerMixin {

    @Shadow
    private int tick;

    /**
     * Inject into the tick method to modify timeout checking.
     * <p>
     * The vanilla timeout is hardcoded at 600 ticks (30 seconds).
     * We override this by resetting the tick counter when it approaches
     * the vanilla timeout but hasn't reached our configured timeout yet.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void mcopt$extendLoginTimeout(CallbackInfo ci) {
        if (!GameplayConfig.ENABLE_LOGIN_TIMEOUT_FIX.get()) {
            return;
        }

        int configuredTimeoutTicks = GameplayConfig.LOGIN_TIMEOUT_SECONDS.get() * 20;
        int vanillaTimeoutTicks = 600; // 30 seconds

        // If we're approaching vanilla timeout but haven't reached our configured timeout
        if (this.tick >= vanillaTimeoutTicks - 1 && this.tick < configuredTimeoutTicks) {
            // Reset to a safe value to prevent vanilla timeout
            this.tick = vanillaTimeoutTicks - 20; // Keep it just below vanilla timeout
        }
    }
}
