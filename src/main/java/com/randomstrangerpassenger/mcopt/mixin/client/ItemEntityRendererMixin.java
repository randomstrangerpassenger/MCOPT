package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.rendering.ItemEntityBatcher;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Mixin for ItemEntityRenderer to enable visual item batching.
 * 
 * <p>
 * This mixin intercepts item entity rendering to:
 * <ul>
 * <li>Skip rendering items that are part of another batch</li>
 * <li>Render batched item counts for primary items</li>
 * </ul>
 * 
 * <p>
 * <strong>Immersion First:</strong> Server-side entity logic remains unchanged.
 * </p>
 */
@SuppressWarnings("null")
@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    /**
     * Inject at the start of render to check if this item should be skipped.
     */
    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void mcopt$onRenderItem(ItemEntity entity, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            CallbackInfo ci) {
        // Check if this item should be rendered or skipped
        if (!ItemEntityBatcher.shouldRenderItem(entity)) {
            ci.cancel();
        }
    }
}
