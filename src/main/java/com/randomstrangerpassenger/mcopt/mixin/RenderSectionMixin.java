package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
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
     */
    @Unique
    private boolean mcopt$shouldCullSection() {
        if (!MCOPTConfig.ENABLE_ELLIPTICAL_RENDER_DISTANCE.get()) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera == null) {
            return false;
        }

        Vec3 cameraPos = camera.getPosition();

        // Calculate chunk center position (in world coordinates)
        double chunkCenterX = origin.getX() * 16.0 + 8.0;
        double chunkCenterY = origin.getY() * 16.0 + 8.0;
        double chunkCenterZ = origin.getZ() * 16.0 + 8.0;

        // Get render distance
        double renderDistance = minecraft.options.renderDistance().get() * 16.0;

        // Check if outside ellipsoid
        return mcopt$isOutsideEllipsoid(
            cameraPos.x, cameraPos.y, cameraPos.z,
            chunkCenterX, chunkCenterY, chunkCenterZ,
            renderDistance
        );
    }

    /**
     * Calculates if a position is outside the render ellipsoid.
     * Uses configurable vertical and horizontal stretch factors.
     */
    @Unique
    private boolean mcopt$isOutsideEllipsoid(
        double camX, double camY, double camZ,
        double chunkX, double chunkY, double chunkZ,
        double baseRenderDistance
    ) {
        // Get stretch configuration
        double verticalStretch = MCOPTConfig.VERTICAL_RENDER_STRETCH.get();
        double horizontalStretch = MCOPTConfig.HORIZONTAL_RENDER_STRETCH.get();

        // Calculate distance components with stretch factors
        double dx = (camX - chunkX) * horizontalStretch;
        double dy = (camY - chunkY) * verticalStretch;
        double dz = (camZ - chunkZ) * horizontalStretch;

        // Calculate squared distance in ellipsoid space
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double renderDistanceSquared = baseRenderDistance * baseRenderDistance;

        // If distance is greater than render distance, cull this chunk
        return distanceSquared > renderDistanceSquared;
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
