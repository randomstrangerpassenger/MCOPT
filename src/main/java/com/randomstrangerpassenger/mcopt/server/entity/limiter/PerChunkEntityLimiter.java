package com.randomstrangerpassenger.mcopt.server.entity.limiter;

import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-chunk entity limiter that prevents localized lag from entity concentration.
 * <p>
 * Unlike ClearLagManager which cleans globally on a schedule, this handler
 * monitors each chunk in real-time and enforces per-chunk entity limits.
 * <p>
 * When a chunk exceeds the configured limit:
 * - If preventSpawnWhenFull is true: Blocks new spawns
 * - If preventSpawnWhenFull is false: Removes oldest entities
 * <p>
 * This is especially useful for:
 * - Mob grinders with poor entity removal
 * - Automated farms that spawn too many animals
 * - Item duplication exploits
 * - Chunk loaders with runaway entity creation
 */
public class PerChunkEntityLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerChunkEntityLimiter.class);

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!SafetyConfig.ENABLE_PER_CHUNK_ENTITY_LIMIT.get()) {
            return;
        }

        // Only process on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();
        ServerLevel level = (ServerLevel) event.getLevel();

        // Check if this entity type should be limited
        if (!shouldLimitEntityType(entity)) {
            return;
        }

        // Get the chunk this entity is trying to join
        ChunkPos chunkPos = entity.chunkPosition();
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);

        // Count entities of limited types in this chunk
        List<Entity> limitedEntities = new ArrayList<>();
        for (Entity chunkEntity : chunk.level().getEntitiesOfClass(Entity.class,
                chunk.getPos().getWorldPosition().expandTowards(16, level.getHeight(), 16))) {
            if (shouldLimitEntityType(chunkEntity) && chunkEntity.isAlive()) {
                limitedEntities.add(chunkEntity);
            }
        }

        int currentCount = limitedEntities.size();
        int maxAllowed = SafetyConfig.MAX_ENTITIES_PER_CHUNK.get();

        if (currentCount >= maxAllowed) {
            if (SafetyConfig.PREVENT_SPAWN_WHEN_FULL.get()) {
                // Cancel spawn
                event.setCanceled(true);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Prevented {} spawn in chunk {} (limit: {})",
                            entity.getType(), chunkPos, maxAllowed);
                }
            } else {
                // Remove oldest entity (first in list)
                if (!limitedEntities.isEmpty()) {
                    Entity oldest = limitedEntities.get(0);
                    oldest.discard();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Removed oldest {} in chunk {} to make room (limit: {})",
                                oldest.getType(), chunkPos, maxAllowed);
                    }
                }
            }
        }
    }

    /**
     * Determines if an entity type should be limited based on config.
     */
    private boolean shouldLimitEntityType(Entity entity) {
        // Never limit players
        if (entity.getType().toString().contains("player")) {
            return false;
        }

        // Never limit vehicles with passengers
        if (entity.isVehicle()) {
            return false;
        }

        if (entity instanceof Monster && SafetyConfig.LIMIT_MONSTERS.get()) {
            return true;
        }

        if (entity instanceof Animal && SafetyConfig.LIMIT_ANIMALS.get()) {
            return true;
        }

        if (entity instanceof ItemEntity && SafetyConfig.LIMIT_ITEMS.get()) {
            return true;
        }

        return false;
    }
}
