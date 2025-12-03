package com.randomstrangerpassenger.mcopt.server.entity.portal;

import com.randomstrangerpassenger.mcopt.MCOPT;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;

import java.util.Optional;

/**
 * Stores the player's last used nether portal per dimension and redirects
 * future arrivals
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
            CompoundTag memory;
            if (mcoptData.contains(TAG_PORTAL_MEMORY)) {
                memory = getCompoundOrNew(mcoptData, TAG_PORTAL_MEMORY);
            } else {
                memory = new CompoundTag();
            }

            String dimensionKey = player.level().dimension().location().toString();

            CompoundTag portalTag = new CompoundTag();
            // Store BlockPos as individual int values
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", portalPos.getX());
            posTag.putInt("Y", portalPos.getY());
            posTag.putInt("Z", portalPos.getZ());
            portalTag.put(TAG_POSITION, posTag);
            portalTag.putFloat(TAG_YAW, player.getYRot());

            memory.put(dimensionKey, portalTag);
            mcoptData.put(TAG_PORTAL_MEMORY, memory);

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

        ServerLevel level = player.level().getServer() != null ? player.level().getServer().getLevel(destination)
                : null;
        if (level == null) {
            MCOPT.LOGGER.warn("Failed to get destination level for portal redirect: {}", destination.location());
            return;
        }

        PortalMemory portalMemory = memory.get();
        BlockPos portalPos = portalMemory.pos();

        // Validate portal position is within world bounds
        if (!isValidPosition(portalPos)) {
            MCOPT.LOGGER.warn("Remembered portal position is invalid: {}", portalPos);
            return;
        }

        // Verify portal block still exists at location
        if (!(level.getBlockState(portalPos).getBlock() instanceof NetherPortalBlock)) {
            MCOPT.LOGGER.debug("Portal block missing at remembered location {}, skipping redirect", portalPos);
            return;
        }

        // Teleport player to remembered portal
        // NeoForge 1.21: teleportTo signature changed
        // ServerPlayer.teleportTo(ServerLevel, double, double, double, Set<Relative>,
        // float, float, boolean)
        player.teleportTo(level, portalPos.getX() + 0.5, portalPos.getY(), portalPos.getZ() + 0.5,
                java.util.Collections.emptySet(), portalMemory.yaw(), player.getXRot(), false);
        MCOPT.LOGGER.info("Redirected player {} to remembered portal at {}", player.getName().getString(), portalPos);
    }

    private static Optional<PortalMemory> getPortalMemory(ServerPlayer player, ResourceKey<Level> dimension) {
        try {
            CompoundTag data = player.getPersistentData();
            if (!data.contains(MCOPT.MOD_ID)) {
                return Optional.empty();
            }

            CompoundTag mcoptData = getCompoundOrNew(data, MCOPT.MOD_ID);

            if (!mcoptData.contains(TAG_PORTAL_MEMORY)) {
                return Optional.empty();
            }

            CompoundTag memory = getCompoundOrNew(mcoptData, TAG_PORTAL_MEMORY);

            String dimensionKey = dimension.location().toString();

            if (!memory.contains(dimensionKey)) {
                return Optional.empty();
            }

            CompoundTag portalTag = getCompoundOrNew(memory, dimensionKey);
            CompoundTag posTag = getCompoundOrNew(portalTag, TAG_POSITION);

            int x = getIntOrZero(posTag, "X");
            int y = getIntOrZero(posTag, "Y");
            int z = getIntOrZero(posTag, "Z");
            BlockPos pos = new BlockPos(x, y, z);

            float yaw = getFloatOrZero(portalTag, TAG_YAW);

            return Optional.of(new PortalMemory(pos, yaw));
        } catch (Exception e) {
            MCOPT.LOGGER.error("Failed to load portal memory", e);
            return Optional.empty();
        }
    }

    private static CompoundTag ensureMcoptData(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(MCOPT.MOD_ID)) {
            data.put(MCOPT.MOD_ID, new CompoundTag());
        }
        return getCompoundOrNew(data, MCOPT.MOD_ID);
    }

    // Helper methods to handle potential Optional returns from NBT getters
    @SuppressWarnings("unchecked")
    private static CompoundTag getCompoundOrNew(CompoundTag tag, String key) {
        try {
            Object result = tag.getCompound(key);
            if (result instanceof Optional) {
                return ((Optional<CompoundTag>) result).orElse(new CompoundTag());
            } else if (result instanceof CompoundTag) {
                return (CompoundTag) result;
            } else {
                return new CompoundTag();
            }
        } catch (Exception e) {
            return new CompoundTag();
        }
    }

    @SuppressWarnings("unchecked")
    private static int getIntOrZero(CompoundTag tag, String key) {
        try {
            Object result = tag.getInt(key);
            if (result instanceof Optional) {
                return ((Optional<Integer>) result).orElse(0);
            } else if (result instanceof Integer) {
                return (Integer) result;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private static float getFloatOrZero(CompoundTag tag, String key) {
        try {
            Object result = tag.getFloat(key);
            if (result instanceof Optional) {
                return ((Optional<Float>) result).orElse(0.0f);
            } else if (result instanceof Float) {
                return (Float) result;
            } else {
                return 0.0f;
            }
        } catch (Exception e) {
            return 0.0f;
        }
    }

    private static boolean isValidPosition(BlockPos pos) {
        return pos != null &&
                pos.getY() >= -64 && // Minimum Y for 1.18+
                pos.getY() <= 320 && // Maximum Y for 1.18+
                Math.abs(pos.getX()) < 30000000 &&
                Math.abs(pos.getZ()) < 30000000;
    }

    private record PortalMemory(BlockPos pos, float yaw) {
    }
}
