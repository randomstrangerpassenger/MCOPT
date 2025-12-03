package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import com.randomstrangerpassenger.mcopt.mixin.accessor.RenderSectionAccessor;

/**
 * Handles rendering optimizations logic, particularly elliptical render
 * distance calculations.
 * Extracted from LevelRendererMixin to separate business logic from Mixin
 * injection code.
 */
public final class RenderHandler {

        private RenderHandler() {
                // Utility class
        }

        /**
         * Calculates if a position is outside the render ellipsoid.
         * Uses configurable vertical and horizontal stretch factors.
         *
         * @param cameraPos          The camera position
         * @param chunkCenterX       The X coordinate of the chunk center
         * @param chunkCenterY       The Y coordinate of the chunk center
         * @param chunkCenterZ       The Z coordinate of the chunk center
         * @param baseRenderDistance The base render distance in blocks
         * @return true if the position is outside the ellipsoid (should be culled)
         */
        public static boolean isOutsideEllipsoid(
                        Vec3 cameraPos,
                        double chunkCenterX,
                        double chunkCenterY,
                        double chunkCenterZ,
                        double baseRenderDistance) {
                // Get stretch configuration
                double verticalStretch = RenderingConfig.VERTICAL_RENDER_STRETCH.get();
                double horizontalStretch = RenderingConfig.HORIZONTAL_RENDER_STRETCH.get();

                // Calculate distance components
                double dx = (cameraPos.x - chunkCenterX) * horizontalStretch;
                double dy = (cameraPos.y - chunkCenterY) * verticalStretch;
                double dz = (cameraPos.z - chunkCenterZ) * horizontalStretch;

                // Calculate squared distance in ellipsoid space
                double distanceSquared = dx * dx + dy * dy + dz * dz;
                double renderDistanceSquared = baseRenderDistance * baseRenderDistance;

                // If distance is greater than render distance, cull this chunk
                return distanceSquared > renderDistanceSquared;
        }

        /**
         * Checks if a chunk section should be rendered based on elliptical distance
         * culling.
         *
         * @param renderSection  The chunk section to check
         * @param cameraPos      The camera position
         * @param renderDistance The render distance in blocks
         * @return true if the chunk should be rendered, false if it should be culled
         */
        public static boolean shouldRenderChunkSection(
                        SectionRenderDispatcher.RenderSection renderSection,
                        Vec3 cameraPos,
                        double renderDistance) {
                // Get chunk section origin using accessor
                SectionPos origin = ((RenderSectionAccessor) renderSection).getOrigin();

                // Calculate chunk center position (in world coordinates)
                double chunkCenterX = origin.getX() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
                                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
                double chunkCenterY = origin.getY() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
                                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
                double chunkCenterZ = origin.getZ() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
                                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;

                return !isOutsideEllipsoid(cameraPos, chunkCenterX, chunkCenterY, chunkCenterZ, renderDistance);
        }
}
