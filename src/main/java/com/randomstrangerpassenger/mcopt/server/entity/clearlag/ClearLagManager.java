package com.randomstrangerpassenger.mcopt.server.entity.clearlag;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
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
import net.neoforged.neoforge.event.tick.TickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom clear-lag implementation inspired by server utilities, but tuned for MCOPT.
 * Cleans up stray items, orbs, and projectiles on a configurable schedule with
 * opt-in protections to keep named or whitelisted entities intact.
 */
public class ClearLagManager {

    private int ticksUntilCleanup = MCOPTConfig.CLEAR_LAG_INTERVAL_TICKS.get();
    private boolean warningIssued;

    // Cache the whitelist to avoid parsing it every cleanup cycle
    private Set<ResourceLocation> cachedWhitelist = null;
    private List<String> lastWhitelistConfig = null;

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
        enableClearLag = MCOPTConfig.ENABLE_CLEAR_LAG.get();
        intervalTicks = MCOPTConfig.CLEAR_LAG_INTERVAL_TICKS.get();
        warningTicks = MCOPTConfig.CLEAR_LAG_WARNING_TICKS.get();
        removeItems = MCOPTConfig.CLEAR_LAG_REMOVE_ITEMS.get();
        removeXpOrbs = MCOPTConfig.CLEAR_LAG_REMOVE_XP_ORBS.get();
        removeProjectiles = MCOPTConfig.CLEAR_LAG_REMOVE_PROJECTILES.get();
        skipNamedItems = MCOPTConfig.CLEAR_LAG_SKIP_NAMED_ITEMS.get();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!enableClearLag) {
            resetTimer();
            return;
        }

        if (--ticksUntilCleanup <= 0) {
            performCleanup();
            resetTimer();
            return;
        }

        if (!warningIssued && warningTicks > 0 && ticksUntilCleanup <= warningTicks) {
            broadcastWarning(ticksUntilCleanup);
            warningIssued = true;
        }
    }

    private void resetTimer() {
        ticksUntilCleanup = Math.max(20, intervalTicks);
        warningIssued = false;
    }

    private void broadcastWarning(int ticksRemaining) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        double seconds = ticksRemaining / 20.0D;
        // Use string concatenation instead of String.format for better performance in hot path
        String messageText = "[MCOPT] 지상 엔티티 정리가 " + String.format(Locale.ROOT, "%.1f", seconds) + "초 후 진행됩니다. 떨어진 아이템을 회수하세요!";
        Component message = Component.literal(messageText);
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private void performCleanup() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        Set<ResourceLocation> whitelist = getCachedWhitelist();
        CleanupStats total = new CleanupStats();

        for (ServerLevel level : server.getAllLevels()) {
            total.merge(cleanLevel(level, whitelist));
        }

        if (total.totalRemoved() > 0) {
            // Build summary message efficiently
            String summaryText = "[MCOPT] 정리 완료: 아이템 " + total.get(RemovalCategory.ITEM) +
                    "개, 경험치 " + total.get(RemovalCategory.XP_ORB) +
                    "개, 투사체 " + total.get(RemovalCategory.PROJECTILE) + "개 제거";
            Component summary = Component.literal(summaryText);
            server.getPlayerList().broadcastSystemMessage(summary, false);
        }
    }

    private CleanupStats cleanLevel(ServerLevel level, Set<ResourceLocation> whitelist) {
        CleanupStats stats = new CleanupStats();

        // Get all potential entities (items, orbs, projectiles) in one pass
        List<Entity> candidates = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Entity.class),
                entity -> entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof Projectile);

        // Classify and remove in a single pass to avoid duplicate classify() calls
        for (Entity entity : candidates) {
            RemovalCategory category = classify(entity, whitelist);
            if (category != null) {
                entity.discard();
                stats.increment(category);
            }
        }
        return stats;
    }

    private RemovalCategory classify(Entity entity, Set<ResourceLocation> whitelist) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (whitelist.contains(id)) {
            return null;
        }

        if (entity instanceof ItemEntity item) {
            if (!removeItems) {
                return null;
            }
            if (skipNamedItems && item.hasCustomName()) {
                return null;
            }
            return RemovalCategory.ITEM;
        }

        if (entity instanceof ExperienceOrb) {
            return removeXpOrbs ? RemovalCategory.XP_ORB : null;
        }

        if (entity instanceof Projectile) {
            return removeProjectiles ? RemovalCategory.PROJECTILE : null;
        }

        return null;
    }

    /**
     * Gets the cached whitelist or regenerates it if the config has changed.
     * This avoids parsing the whitelist on every cleanup cycle.
     */
    private Set<ResourceLocation> getCachedWhitelist() {
        List<String> currentConfig = MCOPTConfig.CLEAR_LAG_ENTITY_WHITELIST.get();

        // Regenerate cache if config has changed or cache is null
        if (cachedWhitelist == null || !currentConfig.equals(lastWhitelistConfig)) {
            cachedWhitelist = currentConfig.stream()
                    .map(String::trim)
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            lastWhitelistConfig = List.copyOf(currentConfig); // Defensive copy
        }

        return cachedWhitelist;
    }

    private enum RemovalCategory {
        ITEM,
        XP_ORB,
        PROJECTILE
    }

    private static final class CleanupStats {
        private final Map<RemovalCategory, Integer> counts = new EnumMap<>(RemovalCategory.class);

        void increment(RemovalCategory category) {
            counts.merge(category, 1, Integer::sum);
        }

        int get(RemovalCategory category) {
            return counts.getOrDefault(category, 0);
        }

        int totalRemoved() {
            return counts.values().stream().mapToInt(Integer::intValue).sum();
        }

        void merge(CleanupStats other) {
            for (Map.Entry<RemovalCategory, Integer> entry : other.counts.entrySet()) {
                counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
    }
}
