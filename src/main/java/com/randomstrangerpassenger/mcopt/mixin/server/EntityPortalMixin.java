package com.randomstrangerpassenger.mcopt.mixin.server;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class EntityPortalMixin {
    @Inject(method = "copy", at = @At("RETURN"), cancellable = true)
    private void mcopt$preserveData(CallbackInfoReturnable<ItemStack> cir) {

        // Logic disabled: ItemStack.copy() in 1.21 should handle components correctly.
        /*
         * // NeoForge 1.21: Use DataComponents for custom data
         * net.minecraft.world.item.component.CustomData customData = self
         * .get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
         * if (customData != null) {
         * result.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
         * customData.copy());
         * }
         */
    }
}
