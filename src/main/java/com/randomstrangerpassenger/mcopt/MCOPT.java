package com.randomstrangerpassenger.mcopt;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import com.randomstrangerpassenger.mcopt.config.GameplayConfig;

import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import com.randomstrangerpassenger.mcopt.util.FeatureKey;
import com.randomstrangerpassenger.mcopt.safety.ActionGuardHandler;
import com.randomstrangerpassenger.mcopt.client.fps.DynamicFpsManager;
import com.randomstrangerpassenger.mcopt.client.bucket.BucketPreviewHandler;

import com.randomstrangerpassenger.mcopt.config.ConfigMigrationHandler;

import com.randomstrangerpassenger.mcopt.server.entity.clearlag.ClearLagManager;
import com.randomstrangerpassenger.mcopt.server.entity.golem.GolemSpawnFixHandler;
import com.randomstrangerpassenger.mcopt.server.entity.limiter.PerChunkEntityLimiter;
import com.randomstrangerpassenger.mcopt.server.ai.AIOptimizationSystem;
import com.randomstrangerpassenger.mcopt.command.MCOPTStatusCommand;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MCOPT.MOD_ID)
public class MCOPT {
    public static final String MOD_ID = "mcopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MCOPT(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("MCOPT initializing - Performance optimization mod for Minecraft");

        // Register domain-specific configurations
        modContainer.registerConfig(ModConfig.Type.CLIENT, MCOPTConfig.getRenderingSpec(), "mcopt-rendering.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, MCOPTConfig.getPerformanceSpec(), "mcopt-performance.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, MCOPTConfig.getGameplaySpec(), "mcopt-gameplay.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, MCOPTConfig.getSafetySpec(), "mcopt-safety.toml");

        // Resolve feature toggles based on config and mod compatibility
        FeatureToggles.refreshFromConfig();
        modEventBus.addListener(FeatureToggles::onModConfigReloaded);

        // Event handlers
        if (FeatureToggles.isEnabled(FeatureKey.ACTION_GUARD)) {
            NeoForge.EVENT_BUS.register(new ActionGuardHandler());
        }

        // Register other handlers
        NeoForge.EVENT_BUS.register(new GolemSpawnFixHandler());
        NeoForge.EVENT_BUS.register(new ClearLagManager());

        if (SafetyConfig.ENABLE_PER_CHUNK_ENTITY_LIMIT.get()) {
            NeoForge.EVENT_BUS.register(new PerChunkEntityLimiter());
            LOGGER.info("Per-chunk entity limiter: ENABLED (max {} per chunk)",
                    SafetyConfig.MAX_ENTITIES_PER_CHUNK.get());
        }

        // Register command
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            MCOPTStatusCommand.register(event.getDispatcher());
            LOGGER.info("MCOPT status command registered: /mcopt status");
        });

        // Register config migration handler
        modEventBus.addListener(ConfigMigrationHandler::onConfigLoad);
        modEventBus.addListener(ConfigMigrationHandler::onConfigReload);

        // Register client-side event handlers
        // Client-side registration disabled temporarily due to dist check issues.
        /*
         * if (FMLEnvironment.dist == Dist.CLIENT) {
         * modEventBus.addListener(MemoryHudRenderer::onRegisterGuiLayers);
         * modEventBus.register(PanicButtonHandler.ModEventHandler.class);
         * NeoForge.EVENT_BUS.register(PanicButtonHandler.GameEventHandler.class);
         * }
         */

        // Register setup handlers
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("MCOPT initialization complete");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MCOPT common setup");

        // Initialize AI optimization system
        if (FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS)) {
            event.enqueueWork(() -> {
                AIOptimizationSystem.init();
                LOGGER.info("AI optimization system: ENABLED");
            });
        }

        if (SafetyConfig.ENABLE_MAX_HEALTH_STABILITY.get()) {
            LOGGER.info("Max health stability fix: ENABLED");
        }

        if (SafetyConfig.ENABLE_ATTRIBUTE_RANGE_EXPANSION.get()) {
            LOGGER.info("Attribute cap expansion: ENABLED (max {})", SafetyConfig.ATTRIBUTE_MAX_LIMIT.get());
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("MCOPT client setup - Loading client-side optimizations");

        if (RenderingConfig.ENABLE_CHUNK_OPTIMIZATIONS.get()) {
            LOGGER.info("Chunk rendering optimizations: ENABLED");
        }

        if (RenderingConfig.ENABLE_ENTITY_CULLING.get()) {
            LOGGER.info("Entity culling optimizations: ENABLED");
        }

        if (RenderingConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get()) {
            LOGGER.info("Particle system optimizations: ENABLED");
        }

        if (FeatureToggles.isEnabled(FeatureKey.XP_ORB_MERGING)) {
            LOGGER.info("Experience orb merging optimizations: ENABLED");
        }

        if (FeatureToggles.isEnabled(FeatureKey.LEAK_GUARD)) {
            LOGGER.info("Leak guard (AllTheLeaks-style world leak detection): ENABLED");
        }

        if (FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS)) {
            LOGGER.info("AI optimization: ENABLED");
            if (PerformanceConfig.ENABLE_MATH_CACHE.get()) {
                LOGGER.info("  - Math caching: ENABLED");
            }
            if (PerformanceConfig.ENABLE_OPTIMIZED_LOOK_CONTROL.get()) {
                LOGGER.info("  - Optimized LookControl: ENABLED");
            }
        }

        if (FeatureToggles.isEnabled(FeatureKey.DYNAMIC_FPS)) {
            LOGGER.info("Dynamic FPS controller: ENABLED");
            NeoForge.EVENT_BUS.register(new DynamicFpsManager());
        }

        if (GameplayConfig.ENABLE_BUCKET_PREVIEW.get()) {
            LOGGER.info("Bucket preview tooltips: ENABLED");
            NeoForge.EVENT_BUS.register(new BucketPreviewHandler());
        }
    }
}
