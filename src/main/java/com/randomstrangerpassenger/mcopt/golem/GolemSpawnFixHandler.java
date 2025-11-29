package com.randomstrangerpassenger.mcopt.golem;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.eventbus.api.Event;

import java.util.Optional;

/**
 * Iron golem spawn reliability improvements.
 * <p>
 * Vanilla villager-based golem spawning relies on the heightmap to relocate spawn attempts,
 * which regularly pushes the position onto roofs or decorative blocks, causing repeated failures.
 * This handler gently snaps MOB_SUMMONED golems downward to the nearest safe ground so they can
 * actually appear inside the intended spawn zone without changing villager logic.
 */
public final class GolemSpawnFixHandler {

    @SubscribeEvent
    public void onGolemCheckSpawn(MobSpawnEvent.CheckSpawn event) {
        if (!MCOPTConfig.ENABLE_GOLEM_SPAWN_FIX.get()) {
            return;
        }

        Mob mob = event.getEntity();
        if (!(mob instanceof IronGolem golem)) {
            return;
        }

        if (event.getSpawnReason() != MobSpawnType.MOB_SUMMONED && event.getSpawnReason() != MobSpawnType.EVENT) {
            return; // Keep natural/structure spawning intact
        }

        findStableSpawnBelow(event.getLevel(), event.getPos(), golem).ifPresent(target -> {
            Vec3 anchor = Vec3.atBottomCenterOf(target);
            golem.moveTo(anchor.x, anchor.y, anchor.z, golem.getYRot(), golem.getXRot());
            event.setResult(Event.Result.ALLOW);
        });
    }

    private Optional<BlockPos> findStableSpawnBelow(LevelReader level, BlockPos start, IronGolem golem) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        int searchDepth = MCOPTConfig.GOLEM_SPAWN_SEARCH_RANGE.get();
        for (int i = 0; i <= searchDepth; i++) {
            if (isSpawnableSurface(level, cursor, golem)) {
                return Optional.of(cursor.immutable());
            }
            cursor.move(0, -1, 0);
            if (cursor.getY() < level.getMinBuildHeight()) {
                break;
            }
        }
        return Optional.empty();
    }

    private boolean isSpawnableSurface(LevelReader level, BlockPos pos, IronGolem golem) {
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
            return false; // Need two blocks of air for the golem's head
        }

        BlockPos floorPos = pos.below();
        BlockState floor = level.getBlockState(floorPos);
        if (!floor.isSolid()) {
            return false;
        }

        // Ensure the golem can stand here with vanilla collision checks
        return level.noCollision(golem.getType().getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(pos)));
    }
}
