package com.randomstrangerpassenger.mcopt.client.rendering;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import com.randomstrangerpassenger.mcopt.util.MCOPTConstants;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;

/**
 * Handles rendering optimizations logic, particularly elliptical render distance calculations.
 * Extracted from LevelRendererMixin to separate business logic from Mixin injection code.
 */
public final class RenderHandler {

    private RenderHandler() {
        // Utility class
    }

    /**
     * Calculates if a position is outside the render ellipsoid.
     * Uses configurable vertical and horizontal stretch factors.
     *
     * @param cameraPos The camera position
     * @param chunkCenterX The X coordinate of the chunk center
     * @param chunkCenterY The Y coordinate of the chunk center
     * @param chunkCenterZ The Z coordinate of the chunk center
     * @param baseRenderDistance The base render distance in blocks
     * @return true if the position is outside the ellipsoid (should be culled)
     */
    public static boolean isOutsideEllipsoid(
            Vec3 cameraPos,
            double chunkCenterX,
            double chunkCenterY,
            double chunkCenterZ,
            double baseRenderDistance
    ) {
        // Get stretch configuration
        double verticalStretch = MCOPTConfig.VERTICAL_RENDER_STRETCH.get();
        double horizontalStretch = MCOPTConfig.HORIZONTAL_RENDER_STRETCH.get();

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
     * Checks if a chunk section should be rendered based on elliptical distance culling.
     *
     * @param renderSection The chunk section to check
     * @param cameraPos The camera position
     * @param renderDistance The render distance in blocks
     * @return true if the chunk should be rendered, false if it should be culled
     */
    public static boolean shouldRenderChunkSection(
            SectionRenderDispatcher.RenderSection renderSection,
            Vec3 cameraPos,
            double renderDistance
    ) {
        if (cameraPos == null) {
            return true;
        }

        // Get chunk section position
        SectionPos sectionPos = renderSection.getOrigin();

        // Calculate chunk center position (in world coordinates)
        double chunkCenterX = sectionPos.getX() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
        double chunkCenterY = sectionPos.getY() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
        double chunkCenterZ = sectionPos.getZ() * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;

        // Calculate elliptical distance and determine if should cull
        boolean shouldCull = isOutsideEllipsoid(
                cameraPos,
                chunkCenterX, chunkCenterY, chunkCenterZ,
                renderDistance
        );

        return !shouldCull;
    }

    /**
     * Gets the configured vertical render stretch factor.
     *
     * @return The vertical stretch factor
     */
    public static double getVerticalStretch() {
        return MCOPTConfig.VERTICAL_RENDER_STRETCH.get();
    }

    /**
     * Gets the configured horizontal render stretch factor.
     *
     * @return The horizontal stretch factor
     */
    public static double getHorizontalStretch() {
        return MCOPTConfig.HORIZONTAL_RENDER_STRETCH.get();
    }
}
