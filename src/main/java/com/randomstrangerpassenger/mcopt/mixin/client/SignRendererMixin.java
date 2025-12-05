package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.ui.SignTextCache;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignRenderer.class)
// @SuppressWarnings("null") // Suppress warning if preferred, or use explicit
// check
public class SignRendererMixin {

    @Inject(method = "renderSignText", at = @At("HEAD"), cancellable = true)
    private void onRenderSignText(BlockPos pos, SignBlockEntity sign, com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, int packedOverlay,
            boolean isFrontText, CallbackInfo ci) {
        if (pos != null && !SignTextCache.shouldRenderText(pos)) {
            ci.cancel();
        }
    }
}
