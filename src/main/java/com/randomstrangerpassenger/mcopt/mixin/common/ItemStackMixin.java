package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.fixes.ItemDataSanitizer;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ItemStack Mixin for NBT tag sanitization.
 * <p>
 * Automatically converts empty NBT tags to null to fix item merging issues.
 * <p>
 * <b>Problem</b>: ItemStacks with empty tags ({}) don't merge with ones that
 * have null tags
 * <p>
 * <b>Solution</b>: Auto-sanitize empty tags to null when tags are set
 *
 * @see ItemDataSanitizer
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {

    /**
     * Sanitize empty tags after setTag is called.
     * <p>
     * This injection runs AFTER setTag completes, checking if the tag is empty
     * and converting it to null if needed.
     */
    @Inject(method = "setTag", at = @At("RETURN"))
    private void mcopt$sanitizeAfterSetTag(CompoundTag tag, CallbackInfo ci) {
        // Feature toggle check
        if (!GameplayConfig.ENABLE_ITEM_NBT_SANITIZER.get()) {
            return;
        }

        ItemStack self = (ItemStack) (Object) this;
        ItemDataSanitizer.sanitizeEmptyTag(self);
    }
}
