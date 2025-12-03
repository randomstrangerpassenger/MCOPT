package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for EnchantmentMenu to fix enchantment seed synchronization issues.
 *
 * <p>This implementation is inspired by the Enchanter Fix mod but uses a completely
 * independent approach:
 * <ul>
 *   <li>Refreshes the enchantment seed whenever the table slots change (items/lapis added/removed)</li>
 *   <li>Ensures the new seed is immediately synchronized to the client via broadcastChanges()</li>
 *   <li>Prevents the vanilla issue where the client shows stale enchantment options</li>
 * </ul>
 *
 * <p><b>Original Implementation:</b> This is MCOPT's own solution to the seed sync problem,
 * not a port of any existing mod's code. The approach of calling broadcastChanges() after
 * seed changes is a standard Minecraft container synchronization pattern.
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    /**
     * Injects at the start of slotsChanged to generate a new enchantment seed.
     * This makes enchantment options truly random instead of being deterministic
     * based on the player's seed.
     *
     * @param container The container that changed
     * @param ci Mixin callback info
     */
    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void mcopt$refreshEnchantmentSeed(Container container, CallbackInfo ci) {
        if (!GameplayConfig.FIX_ENCHANTMENT_RNG.get()) {
            return;
        }

        // Use accessor to access private fields
        com.randomstrangerpassenger.mcopt.mixin.accessor.EnchantmentMenuAccessor accessor =
            (com.randomstrangerpassenger.mcopt.mixin.accessor.EnchantmentMenuAccessor) (Object) this;

        // Only run on server side and only for the enchantment table's own slots
        if (accessor.getPlayer().level().isClientSide() || container != accessor.getEnchantSlots()) {
            return;
        }

        // Generate a new random seed
        int newSeed = accessor.getRandom().nextInt();

        // Update the DataSlot which is used for enchantment calculations
        // Note: Player.setEnchantmentSeed() was removed in Minecraft 1.21
        // The DataSlot approach is sufficient for refreshing enchantments
        accessor.getEnchantmentSeed().set(newSeed);
    }

    /**
     * Injects at the end of slotsChanged to immediately synchronize the new seed to the client.
     *
     * <p>This is the key fix for the Enchanter Fix functionality: without this explicit
     * broadcastChanges() call, the client may not receive the updated seed until the next
     * automatic sync, causing it to display incorrect enchantment options.
     *
     * <p>By calling broadcastChanges() immediately after the seed is updated, we ensure
     * the client always sees the correct enchantments that match the server's calculations.
     *
     * @param container The container that changed
     * @param ci Mixin callback info
     */
    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void mcopt$syncEnchantmentSeedToClient(Container container, CallbackInfo ci) {
        if (!GameplayConfig.FIX_ENCHANTMENT_RNG.get()) {
            return;
        }

        com.randomstrangerpassenger.mcopt.mixin.accessor.EnchantmentMenuAccessor accessor =
            (com.randomstrangerpassenger.mcopt.mixin.accessor.EnchantmentMenuAccessor) (Object) this;

        // Only sync on server side and only for the enchantment table's own slots
        if (accessor.getPlayer().level().isClientSide() || container != accessor.getEnchantSlots()) {
            return;
        }

        // Force immediate synchronization of all DataSlots (including the enchantment seed)
        // to the client. This ensures the client displays the correct enchantment options
        // that correspond to the newly generated seed.
        ((EnchantmentMenu)(Object)this).broadcastChanges();
    }
}
