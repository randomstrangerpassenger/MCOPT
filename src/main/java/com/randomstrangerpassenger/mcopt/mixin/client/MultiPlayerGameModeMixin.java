package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for client-side interaction fallthrough behavior (RCF-style).
 * <p>
 * When using the main hand item fails (e.g., no space to place a block),
 * this automatically falls through to try the off-hand item.
 * <p>
 * <b>Problem</b>: In vanilla, if the main hand item use fails (returns FAIL),
 * the game immediately returns without trying the off-hand item.
 * <p>
 * <b>Solution</b>: When main hand returns FAIL and off-hand has an item,
 * attempt to use the off-hand item instead.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Intercepts useItemOn to implement fallthrough behavior.
     * <p>
     * When the main hand interaction fails but the off-hand has an item,
     * this method triggers the off-hand usage automatically.
     */
    @Inject(method = "useItemOn", at = @At("RETURN"), cancellable = true)
    private void mcopt$onUseItemOnReturn(LocalPlayer player, InteractionHand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!GameplayConfig.ENABLE_RIGHT_CLICK_FALLTHROUGH.get()) {
            return;
        }

        // Only process main hand failures
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        InteractionResult result = cir.getReturnValue();

        // Only fallthrough on FAIL result
        if (result != InteractionResult.FAIL) {
            return;
        }

        // Check if off-hand has an item
        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.isEmpty()) {
            return;
        }

        // Try using off-hand on the block
        MultiPlayerGameMode gameMode = (MultiPlayerGameMode) (Object) this;
        InteractionResult offHandResult = gameMode.useItemOn(player, InteractionHand.OFF_HAND, hitResult);

        // If off-hand succeeded, return its result
        if (offHandResult.consumesAction()) {
            cir.setReturnValue(offHandResult);
        }
    }

    /**
     * Intercepts useItem (use in air) to implement fallthrough behavior.
     * <p>
     * When the main hand interaction fails but the off-hand has an item,
     * this method triggers the off-hand usage automatically.
     */
    @Inject(method = "useItem", at = @At("RETURN"), cancellable = true)
    private void mcopt$onUseItemReturn(LocalPlayer player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!GameplayConfig.ENABLE_RIGHT_CLICK_FALLTHROUGH.get()) {
            return;
        }

        // Only process main hand failures
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        InteractionResult result = cir.getReturnValue();

        // Only fallthrough on FAIL result
        if (result != InteractionResult.FAIL) {
            return;
        }

        // Check if off-hand has an item
        ItemStack offHandItem = player.getOffhandItem();
        if (offHandItem.isEmpty()) {
            return;
        }

        // Try using off-hand in air
        MultiPlayerGameMode gameMode = (MultiPlayerGameMode) (Object) this;
        InteractionResult offHandResult = gameMode.useItem(player, InteractionHand.OFF_HAND);

        // If off-hand succeeded, return its result
        if (offHandResult.consumesAction()) {
            cir.setReturnValue(offHandResult);
        }
    }
}
