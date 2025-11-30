package com.randomstrangerpassenger.mcopt.portal;

import com.randomstrangerpassenger.mcopt.MCOPT;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;

import java.util.Optional;

/**
 * Stores the player's last used nether portal per dimension and redirects future arrivals
 * to that remembered location to prevent unwanted portal swapping.
 */
public final class PortalMemoryTracker {
    private static final String TAG_PORTAL_MEMORY = "portal_memory";
    private static final String TAG_POSITION = "pos";
    private static final String TAG_YAW = "yaw";

    private PortalMemoryTracker() {
    }

    public static void rememberPortal(ServerPlayer player, BlockPos portalPos) {
        // Validate portal position before storing
        if (!isValidPosition(portalPos)) {
            MCOPT.LOGGER.warn("Attempted to remember invalid portal position: {}", portalPos);
            return;
        }

        try {
            CompoundTag mcoptData = ensureMcoptData(player);
            CompoundTag memory = mcoptData.getCompound(TAG_PORTAL_MEMORY);

            String dimensionKey = player.level().dimension().location().toString();

            CompoundTag portalTag = new CompoundTag();
            portalTag.put(TAG_POSITION, NbtUtils.writeBlockPos(portalPos));
            portalTag.putFloat(TAG_YAW, player.getYRot());

            memory.put(dimensionKey, portalTag);
            mcoptData.put(TAG_PORTAL_MEMORY, memory);
            player.getPersistentData().put(MCOPT.MOD_ID, mcoptData);

            MCOPT.LOGGER.debug("Remembered portal at {} for player {} in dimension {}",
                    portalPos, player.getName().getString(), dimensionKey);
        } catch (Exception e) {
            MCOPT.LOGGER.error("Failed to save portal memory for player {}", player.getName().getString(), e);
        }
    }

    public static void redirectToRememberedPortal(ServerPlayer player, ResourceKey<Level> destination) {
        Optional<PortalMemory> memory = getPortalMemory(player, destination);
        if (memory.isEmpty()) {
            return;
        }

        ServerLevel level = player.server.getLevel(destination);
        if (level == null) {
            MCOPT.LOGGER.warn("Failed to get destination level for portal redirect: {}", destination.location());
            return;
        }

        PortalMemory portalMemory = memory.get();
        BlockPos portalPos = portalMemory.pos();

        // Validate portal position is within world bounds
        if (!isValidPosition(portalPos)) {
            MCOPT.LOGGER.warn("Invalid portal position stored in memory: {}", portalPos);
            return;
        }

        if (!level.isLoaded(portalPos)) {
            return;
        }

        if (!(level.getBlockState(portalPos).getBlock() instanceof NetherPortalBlock)) {
            MCOPT.LOGGER.debug("Portal block no longer exists at remembered position: {}", portalPos);
            return;
        }

        double x = portalPos.getX() + 0.5;
        double y = portalPos.getY() + 0.1;
        double z = portalPos.getZ() + 0.5;

        // Wrap teleport in try-catch to handle potential errors gracefully
        try {
            player.connection.teleport(x, y, z, portalMemory.yaw(), player.getXRot());
            MCOPT.LOGGER.debug("Redirected player {} to remembered portal at {}", player.getName().getString(), portalPos);
        } catch (Exception e) {
            MCOPT.LOGGER.error("Failed to teleport player {} to remembered portal at {}",
                    player.getName().getString(), portalPos, e);
        }
    }

    private static Optional<PortalMemory> getPortalMemory(ServerPlayer player, ResourceKey<Level> destination) {
        try {
            CompoundTag persistentData = player.getPersistentData();
            if (!persistentData.contains(MCOPT.MOD_ID, Tag.TAG_COMPOUND)) {
                return Optional.empty();
            }

            CompoundTag mcoptData = persistentData.getCompound(MCOPT.MOD_ID);
            if (!mcoptData.contains(TAG_PORTAL_MEMORY, Tag.TAG_COMPOUND)) {
                return Optional.empty();
            }

            CompoundTag memory = mcoptData.getCompound(TAG_PORTAL_MEMORY);
            String dimensionKey = destination.location().toString();

            if (!memory.contains(dimensionKey, Tag.TAG_COMPOUND)) {
                return Optional.empty();
            }

            CompoundTag portalTag = memory.getCompound(dimensionKey);
            if (!portalTag.contains(TAG_POSITION, Tag.TAG_COMPOUND)) {
                return Optional.empty();
            }

            BlockPos portalPos = NbtUtils.readBlockPos(portalTag.getCompound(TAG_POSITION));

            // Validate the read position
            if (!isValidPosition(portalPos)) {
                MCOPT.LOGGER.warn("Invalid portal position read from NBT: {}", portalPos);
                return Optional.empty();
            }

            float yaw = portalTag.contains(TAG_YAW, Tag.TAG_FLOAT) ? portalTag.getFloat(TAG_YAW) : player.getYRot();

            // Validate yaw is within expected range
            if (!Float.isFinite(yaw)) {
                MCOPT.LOGGER.warn("Invalid yaw value read from NBT: {}", yaw);
                yaw = player.getYRot();
            }

            return Optional.of(new PortalMemory(portalPos, yaw));
        } catch (Exception e) {
            MCOPT.LOGGER.error("Error reading portal memory from NBT for player {}", player.getName().getString(), e);
            return Optional.empty();
        }
    }

    private static CompoundTag ensureMcoptData(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(MCOPT.MOD_ID, Tag.TAG_COMPOUND)) {
            persistentData.put(MCOPT.MOD_ID, new CompoundTag());
        }

        return persistentData.getCompound(MCOPT.MOD_ID);
    }

    /**
     * Validates that a BlockPos is within reasonable world bounds.
     * Minecraft's build limits are -64 to 320 for Y, and ±30,000,000 for X/Z.
     *
     * @param pos The position to validate
     * @return true if the position is valid, false otherwise
     */
    private static boolean isValidPosition(BlockPos pos) {
        if (pos == null) {
            return false;
        }

        // Check Y coordinate is within build limits (-64 to 320)
        if (pos.getY() < -64 || pos.getY() > 320) {
            return false;
        }

        // Check X and Z coordinates are within world border limits
        // Minecraft's max world border is ±29,999,984 blocks
        int maxCoordinate = 29_999_984;
        if (Math.abs(pos.getX()) > maxCoordinate || Math.abs(pos.getZ()) > maxCoordinate) {
            return false;
        }

        return true;
    }

    private record PortalMemory(BlockPos pos, float yaw) {
    }
}
