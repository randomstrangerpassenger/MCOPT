package com.randomstrangerpassenger.mcopt.health;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * Stabilizes player health when the MAX_HEALTH attribute changes.
 * <p>
 * Vanilla clamps health to the new max value but does not scale the current
 * health proportionally. This can cause players with increased max health to
 * log back in with only 10 hearts or lose a large chunk of health when a
 * modifier expires. This handler preserves the player's health percentage when
 * MAX_HEALTH changes, mirroring the intent of the Max Health Fix mod while
 * keeping MCOPT's independent implementation.
 */
@EventBusSubscriber(modid = MCOPT.MOD_ID)
public class HealthStabilityHandler {

    private static final Map<UUID, Double> LAST_MAX_HEALTH = new HashMap<>();
    private static final double EPSILON = 0.0001D;

    // Cache config value to avoid repeated .get() calls every tick
    private static boolean enableMaxHealthStability = MCOPTConfig.ENABLE_MAX_HEALTH_STABILITY.get();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.CLIENT || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!enableMaxHealthStability) {
            return;
        }

        Player player = event.player;
        UUID uuid = player.getUUID();

        double currentMax = player.getAttributeValue(Attributes.MAX_HEALTH);
        double previousMax = LAST_MAX_HEALTH.getOrDefault(uuid, currentMax);

        // Avoid division by zero and noise from tiny attribute fluctuations
        if (previousMax <= EPSILON || Math.abs(currentMax - previousMax) < EPSILON) {
            LAST_MAX_HEALTH.put(uuid, currentMax);
            return;
        }

        float currentHealth = player.getHealth();
        double healthRatio = currentHealth / previousMax;

        // Clamp ratio to sensible range to avoid NaN/overflow scenarios
        healthRatio = Math.max(0.0D, Math.min(healthRatio, 1.0D));

        float targetHealth = (float) Math.max(0.0D, Math.min(currentMax, healthRatio * currentMax));

        if (Math.abs(targetHealth - currentHealth) > 0.001F) {
            player.setHealth(targetHealth);
            MCOPT.LOGGER.debug("Scaled player health to preserve ratio: {} -> {} (max: {} -> {})", currentHealth, targetHealth, previousMax, currentMax);
        }

        LAST_MAX_HEALTH.put(uuid, currentMax);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_MAX_HEALTH.remove(event.getEntity().getUUID());
    }
}
