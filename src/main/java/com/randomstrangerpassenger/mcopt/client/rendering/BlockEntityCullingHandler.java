package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Handles block entity culling logic for rendering optimization.
 * <p>
 * Block entities (chests, signs, skulls, etc.) can cause significant FPS drops
 * in large warehouses or storage rooms. This handler culls block entities that:
 * - Are too far from the camera
 * - Are behind the camera
 * - Are behind walls (optional)
 * <p>
 * Separates business logic from BlockEntityRenderDispatcherMixin to improve
 * testability and maintainability.
 */
public final class BlockEntityCullingHandler {

    private BlockEntityCullingHandler() {
        // Utility class
    }

    /**
     * Determines if a block entity should be culled from rendering.
     *
     * @param blockPos        Position of the block entity
     * @param camera          Current camera
     * @param cullingDistance Maximum render distance
     * @return true if block entity should be culled, false if it should be
     *         rendered
     */
    public static boolean shouldCullBlockEntity(
            BlockPos blockPos,
            Camera camera,
            int cullingDistance) {

        if (!RenderingConfig.ENABLE_BLOCK_ENTITY_CULLING.get()) {
            return false;
        }

        Vec3 cameraPos = camera.getPosition();
        Vec3 blockEntityPos = Vec3.atCenterOf(blockPos);

        // Distance culling
        double distanceSquared = cameraPos.distanceToSqr(blockEntityPos);
        double maxDistanceSquared = cullingDistance * cullingDistance;

        if (distanceSquared > maxDistanceSquared) {
            return true; // Cull
        }

        // Backface culling (block entities behind camera)
        if (RenderingConfig.CULL_BLOCK_ENTITIES_BEHIND_WALLS.get()) {
            Vector3f look = camera.getLookVector();
            Vec3 viewVector = new Vec3(look.x(), look.y(), look.z());
            Vec3 toBlockEntity = blockEntityPos.subtract(cameraPos).normalize();

            double dotProduct = viewVector.dot(toBlockEntity);
            if (dotProduct < MCOPTConstants.Performance.BACKFACE_CULLING_DOT_THRESHOLD
                    && distanceSquared > MCOPTConstants.Minecraft.ENTITY_BACKFACE_CULLING_DISTANCE_SQ) {
                return true; // Cull
            }
        }

        return false; // Don't cull
    }
}
