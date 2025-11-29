package com.randomstrangerpassenger.mcopt;

import com.randomstrangerpassenger.mcopt.client.IdleFpsController;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MCOpt.MOD_ID)
public class MCOpt {
    public static final String MOD_ID = "mcopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MCOpt.class);

    public MCOpt(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> IdleFpsController::register);

        LOGGER.info("MCOpt loaded: lightweight performance guardrail and stability patches active.");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> LOGGER.info("MCOpt common setup completed."));
    }
}
