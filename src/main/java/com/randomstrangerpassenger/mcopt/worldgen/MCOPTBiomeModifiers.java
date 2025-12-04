package com.randomstrangerpassenger.mcopt.worldgen;

import com.mojang.serialization.MapCodec;
import com.randomstrangerpassenger.mcopt.MCOPT;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Registry for MCOPT's custom BiomeModifiers.
 * <p>
 * This class registers all custom BiomeModifier codecs used by MCOPT.
 * Currently includes:
 * <ul>
 *   <li>{@link ConditionalBasinModifier} - Config-gated basin (stone disk) feature restoration</li>
 * </ul>
 * </p>
 */
public class MCOPTBiomeModifiers {

        /**
         * DeferredRegister for BiomeModifier serializers (codecs).
         */
        public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
                        DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MCOPT.MOD_ID);

        /**
         * Codec for ConditionalBasinModifier.
         * This allows the modifier to be loaded from JSON datapack files.
         */
        public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConditionalBasinModifier>> CONDITIONAL_BASIN =
                        BIOME_MODIFIER_SERIALIZERS.register("conditional_basin", () -> ConditionalBasinModifier.CODEC);

        /**
         * Register all biome modifier serializers to the mod event bus.
         *
         * @param modEventBus The mod event bus
         */
        public static void register(IEventBus modEventBus) {
                BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
                MCOPT.LOGGER.info("Registered MCOPT biome modifier serializers");
        }
}
