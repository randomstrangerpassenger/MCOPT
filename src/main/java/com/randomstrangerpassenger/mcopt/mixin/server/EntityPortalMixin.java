package com.randomstrangerpassenger.mcopt.mixin.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class EntityPortalMixin {
    @Inject(method = "copy", at = @At("RETURN"), cancellable = true)
    private void mcopt$preserveData(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        ItemStack result = cir.getReturnValue();
        CompoundTag tag = self.getTag();
        if (tag != null) {
            result.setTag(tag.copy());
        }
    }
}
