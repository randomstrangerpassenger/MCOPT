package com.randomstrangerpassenger.mcopt.clearlag;

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

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!MCOPTConfig.ENABLE_CLEAR_LAG.get()) {
            resetTimer();
            return;
        }

        if (--ticksUntilCleanup <= 0) {
            performCleanup();
            resetTimer();
            return;
        }

        int warningThreshold = MCOPTConfig.CLEAR_LAG_WARNING_TICKS.get();
        if (!warningIssued && warningThreshold > 0 && ticksUntilCleanup <= warningThreshold) {
            broadcastWarning(ticksUntilCleanup);
            warningIssued = true;
        }
    }

    private void resetTimer() {
        ticksUntilCleanup = Math.max(20, MCOPTConfig.CLEAR_LAG_INTERVAL_TICKS.get());
        warningIssued = false;
    }

    private void broadcastWarning(int ticksRemaining) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        double seconds = ticksRemaining / 20.0D;
        Component message = Component.literal(String.format(Locale.ROOT,
                "[MCOPT] 지상 엔티티 정리가 %.1f초 후 진행됩니다. 떨어진 아이템을 회수하세요!", seconds));
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private void performCleanup() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        Set<ResourceLocation> whitelist = resolveWhitelist();
        CleanupStats total = new CleanupStats();

        for (ServerLevel level : server.getAllLevels()) {
            total.merge(cleanLevel(level, whitelist));
        }

        if (total.totalRemoved() > 0) {
            Component summary = Component.literal(String.format(Locale.ROOT,
                    "[MCOPT] 정리 완료: 아이템 %d개, 경험치 %d개, 투사체 %d개 제거",
                    total.get(RemovalCategory.ITEM),
                    total.get(RemovalCategory.XP_ORB),
                    total.get(RemovalCategory.PROJECTILE)));
            server.getPlayerList().broadcastSystemMessage(summary, false);
        }
    }

    private CleanupStats cleanLevel(ServerLevel level, Set<ResourceLocation> whitelist) {
        CleanupStats stats = new CleanupStats();
        List<Entity> targets = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Entity.class),
                entity -> classify(entity, whitelist) != null);

        for (Entity entity : targets) {
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
            if (!MCOPTConfig.CLEAR_LAG_REMOVE_ITEMS.get()) {
                return null;
            }
            if (MCOPTConfig.CLEAR_LAG_SKIP_NAMED_ITEMS.get() && item.hasCustomName()) {
                return null;
            }
            return RemovalCategory.ITEM;
        }

        if (entity instanceof ExperienceOrb) {
            return MCOPTConfig.CLEAR_LAG_REMOVE_XP_ORBS.get() ? RemovalCategory.XP_ORB : null;
        }

        if (entity instanceof Projectile) {
            return MCOPTConfig.CLEAR_LAG_REMOVE_PROJECTILES.get() ? RemovalCategory.PROJECTILE : null;
        }

        return null;
    }

    private Set<ResourceLocation> resolveWhitelist() {
        return MCOPTConfig.CLEAR_LAG_ENTITY_WHITELIST.get()
                .stream()
                .map(String::trim)
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
