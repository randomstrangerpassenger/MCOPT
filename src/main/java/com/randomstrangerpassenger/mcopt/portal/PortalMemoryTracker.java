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
        CompoundTag mcoptData = ensureMcoptData(player);
        CompoundTag memory = mcoptData.getCompound(TAG_PORTAL_MEMORY);

        String dimensionKey = player.level().dimension().location().toString();

        CompoundTag portalTag = new CompoundTag();
        portalTag.put(TAG_POSITION, NbtUtils.writeBlockPos(portalPos));
        portalTag.putFloat(TAG_YAW, player.getYRot());

        memory.put(dimensionKey, portalTag);
        mcoptData.put(TAG_PORTAL_MEMORY, memory);
        player.getPersistentData().put(MCOPT.MOD_ID, mcoptData);
    }

    public static void redirectToRememberedPortal(ServerPlayer player, ResourceKey<Level> destination) {
        Optional<PortalMemory> memory = getPortalMemory(player, destination);
        if (memory.isEmpty()) {
            return;
        }

        ServerLevel level = player.server.getLevel(destination);
        if (level == null) {
            return;
        }

        PortalMemory portalMemory = memory.get();
        BlockPos portalPos = portalMemory.pos();

        if (!level.isLoaded(portalPos)) {
            return;
        }

        if (!(level.getBlockState(portalPos).getBlock() instanceof NetherPortalBlock)) {
            return;
        }

        double x = portalPos.getX() + 0.5;
        double y = portalPos.getY() + 0.1;
        double z = portalPos.getZ() + 0.5;

        player.connection.teleport(x, y, z, portalMemory.yaw(), player.getXRot());
    }

    private static Optional<PortalMemory> getPortalMemory(ServerPlayer player, ResourceKey<Level> destination) {
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
        float yaw = portalTag.contains(TAG_YAW, Tag.TAG_FLOAT) ? portalTag.getFloat(TAG_YAW) : player.getYRot();

        return Optional.of(new PortalMemory(portalPos, yaw));
    }

    private static CompoundTag ensureMcoptData(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(MCOPT.MOD_ID, Tag.TAG_COMPOUND)) {
            persistentData.put(MCOPT.MOD_ID, new CompoundTag());
        }

        return persistentData.getCompound(MCOPT.MOD_ID);
    }

    private record PortalMemory(BlockPos pos, float yaw) {
    }
}
