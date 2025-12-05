package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.common.cache.RecipeLookupCache;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    // Target: getRecipeFor(RecipeType, Container, Level)
    // Actually, inputs are usually extracted from container.
    // Simplifying for this example: assuming a method signature match.

    // Note: Mixin signatures must be exact.
    // public <C extends Container, T extends Recipe<C>> Optional<RecipeHolder<T>>
    // getRecipeFor(RecipeType<T> type, C container, Level level)

    // We can't cache based on Container object easily (stateful).
    // But verify if we can extract items.

    // Fallback: Just invalidate cache on reload.
    @Inject(method = "apply", at = @At("HEAD")) // apply is called on reload
    private void onReload(CallbackInfoReturnable<Object> cir) { // Signature varies
        RecipeLookupCache.invalidateAll();
    }
}
