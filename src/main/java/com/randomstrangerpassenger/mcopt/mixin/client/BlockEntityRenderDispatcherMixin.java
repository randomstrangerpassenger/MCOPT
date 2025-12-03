package com.randomstrangerpassenger.mcopt.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.randomstrangerpassenger.mcopt.client.rendering.BlockEntityCullingHandler;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes block entity rendering by culling block entities that are not
 * visible or too far away.
 * <p>
 * Block entities like chests, signs, and player heads can cause significant
 * FPS drops in large storage rooms or warehouses. This mixin applies the same
 * culling techniques used for regular entities to block entities.
 * <p>
 * Business logic delegated to {@link BlockEntityCullingHandler} for better
 * testability.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    /**
     * Skips rendering block entities that are too far away or outside the view
     * frustum.
     * Delegates culling logic to BlockEntityCullingHandler.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void mcopt$cullDistantBlockEntities(
            BlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci) {

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameRenderer == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        var camera = minecraft.gameRenderer.getMainCamera();
        int cullingDistance = RenderingConfig.BLOCK_ENTITY_CULLING_DISTANCE.get();

        if (BlockEntityCullingHandler.shouldCullBlockEntity(
                blockEntity.getBlockPos(),
                camera,
                cullingDistance)) {
            ci.cancel();
        }
    }
}
