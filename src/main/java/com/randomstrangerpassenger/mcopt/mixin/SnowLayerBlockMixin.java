package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "Simple Snowy Fix" 스타일의 눈 적층 최적화를 구현한다.
 * 눈이 쌓일 때 불필요한 이웃 알림을 줄여 대규모 눈보라 시 발생하는
 * 광역 청크 리빌드를 완화한다.
 */
@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerBlockMixin {

    /**
     * 눈이 한 층 더 쌓여도 상태가 변하지 않는 경우 서버 업데이트를 완전히 건너뛴다.
     * 이는 눈이 이미 최대 높이인 위치에서 반복적으로 setBlock이 호출되어
     * 발생하는 불필요한 리빌드를 방지한다.
     */
    @Inject(method = "handlePrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), cancellable = true)
    private void mcopt$skipNoopSnowUpdates(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation, CallbackInfo ci) {
        if (!MCOPTConfig.ENABLE_SNOW_ACCUMULATION_FIX.get()) {
            return;
        }

        if (precipitation != Biome.Precipitation.SNOW || level.isClientSide) {
            return;
        }

        int currentLayers = state.getValue(SnowLayerBlock.LAYERS);
        if (currentLayers >= 8) {
            // 이미 최대 높이인 경우 상태 변동이 없으므로 업데이트를 건너뛴다.
            ci.cancel();
            return;
        }

        BlockState currentState = level.getBlockState(pos);
        BlockState newState = state.setValue(SnowLayerBlock.LAYERS, Math.min(8, currentLayers + 1));

        // 눈 층 수가 동일하다면 변화가 없으므로 리빌드가 불필요하다.
        if (currentState.equals(newState)) {
            ci.cancel();
        }
    }

    /**
     * 눈 층이 증가할 때 이웃 알림 플래그를 낮춰 청크 리렌더링을 최소화한다.
     */
    @ModifyArg(method = "handlePrecipitation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), index = 2)
    private int mcopt$reduceNeighborSpam(int originalFlags) {
        if (!MCOPTConfig.ENABLE_SNOW_ACCUMULATION_FIX.get()) {
            return originalFlags;
        }

        // 최소한의 클라이언트 싱크만 수행하고, 대규모 이웃 알림은 생략한다.
        return Block.UPDATE_CLIENTS;
    }
}
