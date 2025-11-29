package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfo;

/**
 * Optimizes entity rendering by culling entities that are not visible or too far away.
 * This significantly reduces the rendering overhead in areas with many entities.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /**
     * Skips rendering entities that are too far away or outside the view frustum.
     * This is especially helpful in multiplayer servers or areas with many mobs.
     */
    @Inject(
        method = "render",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cullDistantEntities(
        Entity entity,
        double x,
        double y,
        double z,
        float rotationYaw,
        float partialTicks,
        CallbackInfo ci
    ) {
        if (!MCOPTConfig.ENABLE_ENTITY_CULLING.get()) {
            return;
        }

        // Get camera position
        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
        Camera camera = dispatcher.camera;

        if (camera == null) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();
        Vec3 entityPos = entity.position();

        // Calculate distance from camera to entity
        double distanceSquared = cameraPos.distanceToSqr(entityPos);
        int cullingDistance = MCOPTConfig.ENTITY_CULLING_DISTANCE.get();
        double maxDistanceSquared = cullingDistance * cullingDistance;

        // Skip rendering if entity is too far and not important
        if (distanceSquared > maxDistanceSquared) {
            // Don't cull players or vehicles the player is in
            if (!entity.isVehicle() && !entity.isPassenger()) {
                ci.cancel();
                return;
            }
        }

        // Additional check: skip entities behind the camera if configured
        if (MCOPTConfig.CULL_ENTITIES_BEHIND_WALLS.get()) {
            Vec3 viewVector = camera.getLookVector();
            Vec3 toEntity = entityPos.subtract(cameraPos).normalize();

            // If entity is behind the camera (dot product < 0)
            double dotProduct = viewVector.dot(toEntity);
            if (dotProduct < MCOPTConstants.Performance.BACKFACE_CULLING_DOT_THRESHOLD
                && distanceSquared > MCOPTConstants.Minecraft.ENTITY_BACKFACE_CULLING_DISTANCE_SQ) {
                ci.cancel();
            }
        }
    }
}
