package com.randomstrangerpassenger.mcopt.mixin.server;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void mcopt$refreshEnchantmentSeed(Container container, CallbackInfo ci) {
        if (!GameplayConfig.FIX_ENCHANTMENT_RNG.get()) {
            return;
        }

        // Use accessor to access private fields
        com.randomstrangerpassenger.mcopt.mixin.accessor.EnchantmentMenuAccessor accessor = (com.randomstrangerpassenger.mcopt.mixin.accessor.EnchantmentMenuAccessor) (Object) this;

        if (accessor.getPlayer().level().isClientSide() || container != accessor.getEnchantSlots()) {
            return;
        }

        int newSeed = accessor.getRandom().nextInt();
        // Note: Player.setEnchantmentSeed() was removed in Minecraft 1.21
        // Only update the DataSlot which is sufficient for refreshing enchantments
        accessor.getEnchantmentSeed().set(newSeed);
    }
}
