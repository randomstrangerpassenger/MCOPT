package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.server.worldgen.WorldGenFixes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Prevents crashes during lake generation caused by checking biomes in unloaded
 * chunks.
 * <p>
 * This fix intercepts biome freeze checks during lake feature generation and
 * ensures
 * that chunks are loaded before attempting to access biome data.
 */
@SuppressWarnings("deprecation") // LakeFeature is deprecated but we still need to mixin to it
@Mixin(LakeFeature.class)
public abstract class LakeFeatureMixin {

    /**
     * Redirects the biome shouldFreeze check to a safe version that checks chunk
     * loading first.
     * <p>
     * During lake generation, the vanilla code checks if surrounding blocks should
     * freeze.
     * If the chunk at that position isn't loaded, this can cause crashes. Our
     * redirect
     * ensures the chunk is loaded before checking the biome.
     */
    @Redirect(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"), require = 0 // Don't
                                                                                                                                                                                                                                                                                  // fail
                                                                                                                                                                                                                                                                                  // if
                                                                                                                                                                                                                                                                                  // the
                                                                                                                                                                                                                                                                                  // method
                                                                                                                                                                                                                                                                                  // signature
                                                                                                                                                                                                                                                                                  // doesn't
                                                                                                                                                                                                                                                                                  // match
                                                                                                                                                                                                                                                                                  // (for
                                                                                                                                                                                                                                                                                  // version
                                                                                                                                                                                                                                                                                  // compatibility)
    )
    private boolean mcopt$safeBiomeFreezeCheck(Biome biome, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        if (!GameplayConfig.ENABLE_LAKE_CRASH_FIX.get()) {
            // If the fix is disabled, use vanilla behavior
            return biome.shouldFreeze(level, pos);
        }

        // Use our safe version that checks chunk loading
        if (level instanceof WorldGenLevel worldGenLevel) {
            return WorldGenFixes.shouldBiomeFreezeWaterSafely(worldGenLevel, pos);
        }

        // Fallback to vanilla if not a WorldGenLevel (shouldn't happen during
        // generation)
        return biome.shouldFreeze(level, pos);
    }

    /**
     * Redirects the biome shouldSnow check to a safe version that checks chunk
     * loading first.
     * <p>
     * Similar to the freeze check, but for snow placement on lake edges.
     */
    @Redirect(method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"), require = 0 // Don't
                                                                                                                                                                                                                                                                                // fail
                                                                                                                                                                                                                                                                                // if
                                                                                                                                                                                                                                                                                // the
                                                                                                                                                                                                                                                                                // method
                                                                                                                                                                                                                                                                                // signature
                                                                                                                                                                                                                                                                                // doesn't
                                                                                                                                                                                                                                                                                // match
                                                                                                                                                                                                                                                                                // (for
                                                                                                                                                                                                                                                                                // version
                                                                                                                                                                                                                                                                                // compatibility)
    )
    private boolean mcopt$safeBiomeSnowCheck(Biome biome, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        if (!GameplayConfig.ENABLE_LAKE_CRASH_FIX.get()) {
            // If the fix is disabled, use vanilla behavior
            return biome.shouldSnow(level, pos);
        }

        // Use our safe version that checks chunk loading
        if (level instanceof WorldGenLevel worldGenLevel) {
            return WorldGenFixes.shouldBiomeSnowSafely(worldGenLevel, pos);
        }

        // Fallback to vanilla if not a WorldGenLevel (shouldn't happen during
        // generation)
        return biome.shouldSnow(level, pos);
    }
}
