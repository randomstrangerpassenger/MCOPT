package com.randomstrangerpassenger.mcopt.server.entity.golem;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import java.util.Optional;

/**
 * Iron golem spawn reliability improvements.
 * <p>
 * Vanilla villager-based golem spawning relies on the heightmap to relocate
 * spawn attempts,
 * which regularly pushes the position onto roofs or decorative blocks, causing
 * repeated failures.
 * This handler gently snaps MOB_SUMMONED golems downward to the nearest safe
 * ground so they can
 * actually appear inside the intended spawn zone without changing villager
 * logic.
 */
public final class GolemSpawnFixHandler {

    @SubscribeEvent
    public void onGolemCheckSpawn(FinalizeSpawnEvent event) {
        if (!GameplayConfig.ENABLE_GOLEM_SPAWN_FIX.get()) {
            return;
        }

        if (!(event.getEntity() instanceof IronGolem golem)) {
            return;
        }

        // Only affect golems spawned by villagers (MOB_SUMMONED)
        // Workaround for missing MobSpawnType/SpawnReason class: compare string
        // representation
        if (!"MOB_SUMMONED".equals(event.getSpawnType().toString())) {
            return;
        }

        LevelAccessor level = event.getLevel();
        BlockPos pos = new BlockPos((int) event.getX(), (int) event.getY(), (int) event.getZ());

        // If the golem is spawning in a valid spot, leave it alone
        if (isSpawnableSurface(level, pos, golem)) {
            return;
        }

        // Otherwise, try to find a better spot below
        Optional<BlockPos> betterPos = findStableSpawnBelow(level, pos, golem);
        if (betterPos.isPresent()) {
            BlockPos newPos = betterPos.get();
            golem.setPos(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
        }
    }

    private Optional<BlockPos> findStableSpawnBelow(LevelAccessor level, BlockPos start, IronGolem golem) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        int searchDepth = GameplayConfig.GOLEM_SPAWN_SEARCH_RANGE.get();
        for (int i = 0; i <= searchDepth; i++) {
            if (isSpawnableSurface(level, cursor, golem)) {
                return Optional.of(cursor.immutable());
            }
            cursor.move(0, -1, 0);

            // NeoForge 1.21: getMinBuildHeight() not resolving correctly on LevelAccessor
            // Hardcoding to -64 (standard overworld min height) for now to unblock
            // compilation.
            if (cursor.getY() < -64) {
                break;
            }
        }
        return Optional.empty();
    }

    private boolean isSpawnableSurface(LevelAccessor level, BlockPos pos, IronGolem golem) {
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
            return false; // Need two blocks of air for the golem's head
        }

        BlockPos floorPos = pos.below();
        BlockState floor = level.getBlockState(floorPos);

        // isSolid() is deprecated, use isCollisionShapeFullBlock for "solid ground"
        // For spawning, we generally want a solid top face.
        if (!floor.isCollisionShapeFullBlock(level, floorPos)) {
            return false;
        }

        // Ensure the golem can stand here with vanilla collision checks
        return level.noCollision(golem.getType().getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(pos)));
    }
}
