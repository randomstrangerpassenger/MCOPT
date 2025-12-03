package com.randomstrangerpassenger.mcopt.server.entity.clearlag;

import com.randomstrangerpassenger.mcopt.config.SafetyConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom clear-lag implementation inspired by server utilities, but tuned for
 * MCOPT.
 * Cleans up stray items, orbs, and projectiles on a configurable schedule with
 * opt-in protections to keep named or whitelisted entities intact.
 */
public class ClearLagManager {

    private int ticksUntilCleanup = SafetyConfig.CLEAR_LAG_INTERVAL_TICKS.get();
    private boolean warningIssued;

    // Cache the whitelist to avoid parsing it every cleanup cycle
    private Set<ResourceLocation> cachedWhitelist = null;
    private List<? extends String> lastWhitelistConfig = null;

    // Cache frequently accessed config values to avoid repeated .get() calls
    private boolean enableClearLag;
    private int intervalTicks;
    private int warningTicks;
    private boolean removeItems;
    private boolean removeXpOrbs;
    private boolean removeProjectiles;
    private boolean skipNamedItems;

    public ClearLagManager() {
        refreshConfigCache();
    }

    /**
     * Refreshes all cached config values.
     * Call this when config is reloaded.
     */
    private void refreshConfigCache() {
        enableClearLag = SafetyConfig.ENABLE_CLEAR_LAG.get();
        intervalTicks = SafetyConfig.CLEAR_LAG_INTERVAL_TICKS.get();
        warningTicks = SafetyConfig.CLEAR_LAG_WARNING_TICKS.get();
        removeItems = SafetyConfig.CLEAR_LAG_REMOVE_ITEMS.get();
        removeXpOrbs = SafetyConfig.CLEAR_LAG_REMOVE_XP_ORBS.get();
        removeProjectiles = SafetyConfig.CLEAR_LAG_REMOVE_PROJECTILES.get();
        skipNamedItems = SafetyConfig.CLEAR_LAG_SKIP_NAMED_ITEMS.get();
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (!enableClearLag) {
            return;
        }

        ticksUntilCleanup--;

        // Issue warning chat message
        if (ticksUntilCleanup == warningTicks && !warningIssued) {
            broadcastMessage(Component.translatable("mcopt.clearlag.warning", warningTicks / 20));
            warningIssued = true;
        }

        // Perform cleanup
        if (ticksUntilCleanup <= 0) {
            performCleanup();
            ticksUntilCleanup = intervalTicks;
            warningIssued = false;
        }
    }

    private void performCleanup() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        int removedCount = 0;
        Map<EntityTypeCategory, Integer> removedCounts = new EnumMap<>(EntityTypeCategory.class);

        for (ServerLevel level : server.getAllLevels()) {
            List<Entity> toRemove = new ArrayList<>();

            for (Entity entity : level.getAllEntities()) {
                if (shouldRemove(entity)) {
                    toRemove.add(entity);
                }
            }

            for (Entity entity : toRemove) {
                EntityTypeCategory category = getCategory(entity);
                entity.discard();
                removedCount++;
                removedCounts.merge(category, 1, Integer::sum);
            }
        }

        if (removedCount > 0) {
            broadcastMessage(Component.translatable("mcopt.clearlag.complete", removedCount));
        }
    }

    private boolean shouldRemove(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }

        // Check whitelist
        if (isWhitelisted(entity)) {
            return false;
        }

        // Check named items protection
        if (skipNamedItems && entity.hasCustomName()) {
            return false;
        }

        if (entity instanceof ItemEntity) {
            return removeItems;
        }

        if (entity instanceof ExperienceOrb) {
            return removeXpOrbs;
        }

        if (entity instanceof Projectile) {
            // Don't remove projectiles that are "stuck" in ground if we want to keep arrows
            // But usually clear lag removes all projectiles
            return removeProjectiles;
        }

        return false;
    }

    private boolean isWhitelisted(Entity entity) {
        if (cachedWhitelist == null || !SafetyConfig.CLEAR_LAG_ENTITY_WHITELIST.get().equals(lastWhitelistConfig)) {
            updateWhitelistCache();
        }

        ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return cachedWhitelist.contains(entityType);
    }

    private void updateWhitelistCache() {
        lastWhitelistConfig = SafetyConfig.CLEAR_LAG_ENTITY_WHITELIST.get();
        cachedWhitelist = lastWhitelistConfig.stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void broadcastMessage(Component message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private EntityTypeCategory getCategory(Entity entity) {
        if (entity instanceof ItemEntity)
            return EntityTypeCategory.ITEM;
        if (entity instanceof ExperienceOrb)
            return EntityTypeCategory.XP_ORB;
        if (entity instanceof Projectile)
            return EntityTypeCategory.PROJECTILE;
        return EntityTypeCategory.OTHER;
    }

    private enum EntityTypeCategory {
        ITEM, XP_ORB, PROJECTILE, OTHER
    }
}
