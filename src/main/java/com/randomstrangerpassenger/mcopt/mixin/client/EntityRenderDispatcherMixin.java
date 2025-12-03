package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.client.rendering.EntityCullingHandler;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes entity rendering by culling entities that are not visible or too
 * far away.
 * <p>
 * Business logic delegated to {@link EntityCullingHandler} for better
 * testability.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /**
     * Skips rendering entities that are too far away or outside the view frustum.
     * Delegates culling logic to EntityCullingHandler.
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cullDistantEntities(
            Entity entity,
            double x,
            double y,
            double z,
            float rotationYaw,
            float partialTicks,
            CallbackInfo ci) {
        EntityRenderDispatcher dispatcher = (EntityRenderDispatcher) (Object) this;
        Camera camera = dispatcher.camera;

        if (camera == null) {
            return;
        }

        int cullingDistance = RenderingConfig.ENTITY_CULLING_DISTANCE.get();

        if (EntityCullingHandler.shouldCullEntity(entity, camera, cullingDistance)) {
            ci.cancel();
        }
    }
}
