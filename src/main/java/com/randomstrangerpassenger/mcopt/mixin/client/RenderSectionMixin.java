package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.rendering.RenderFrameCache;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for individual render sections to implement elliptical distance culling.
 * This is where the actual per-chunk distance check happens.
 */
@Mixin(SectionRenderDispatcher.RenderSection.class)
public class RenderSectionMixin {

    @Shadow
    @Final
    private SectionPos origin;

    /**
     * Checks if this chunk section should be culled based on elliptical distance from camera.
     * Uses the shared RenderFrameCache to avoid redundant per-section calculations.
     */
    @Unique
    private boolean mcopt$shouldCullSection() {
        if (!MCOPTConfig.ENABLE_ELLIPTICAL_RENDER_DISTANCE.get()) {
            return false;
        }

        // Calculate chunk center position (in world coordinates)
        double chunkCenterX = origin.getX() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
        double chunkCenterY = origin.getY() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
        double chunkCenterZ = origin.getZ() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;

        // Use cached frame data for culling decision
        // This avoids redundant camera/config lookups per section
        return RenderFrameCache.shouldCullChunk(chunkCenterX, chunkCenterY, chunkCenterZ);
    }

    /**
     * Intercept the hasAllNeighbors check to implement elliptical culling.
     * If the section is outside our ellipsoid, pretend it doesn't have all neighbors
     * so it won't be rendered.
     */
    @Inject(
        method = "hasAllNeighbors",
        at = @At("HEAD"),
        cancellable = true
    )
    private void applyEllipticalCulling(CallbackInfoReturnable<Boolean> cir) {
        if (mcopt$shouldCullSection()) {
            cir.setReturnValue(false);
        }
    }
}
