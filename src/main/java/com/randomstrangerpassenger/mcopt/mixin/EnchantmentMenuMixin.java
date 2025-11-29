package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Shadow
    @Final
    private Player player;

    @Shadow
    @Final
    private RandomSource random;

    @Shadow
    @Final
    private DataSlot enchantmentSeed;

    @Shadow
    @Final
    private Container enchantSlots;

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void mcopt$refreshEnchantmentSeed(Container container, CallbackInfo ci) {
        if (!MCOPTConfig.FIX_ENCHANTMENT_RNG.get()) {
            return;
        }

        if (this.player.level().isClientSide || container != this.enchantSlots) {
            return;
        }

        int newSeed = this.random.nextInt();
        this.player.setEnchantmentSeed(newSeed);
        this.enchantmentSeed.set(newSeed);
    }
}
