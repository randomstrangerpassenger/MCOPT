package com.randomstrangerpassenger.mcopt.mixin.client;

import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;

/**
 * Mixin to fix mouse button input in inventory screens.
 * <p>
 * Problem: Vanilla Minecraft ignores mouse buttons mapped to keyboard keys
 * (like drop, swap offhand)
 * when inside inventory screens. Players can't use mouse buttons for these
 * actions.
 * <p>
 * Solution: This mixin intercepts mouseClicked events and checks if the mouse
 * button
 * is mapped to any inventory key. If so, it triggers the equivalent slotClicked
 * behavior.
 * <p>
 * Inspired by MouseKeyInventoryFix mod but implemented independently for MCOPT.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MouseInputFixMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType);

    /**
     * Intercepts mouse clicks in inventory screens.
     * <p>
     * Checks if the clicked mouse button is mapped to an inventory key and triggers
     * the appropriate inventory action.
     *
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param button Mouse button clicked
     * @param cir    Callback info returnable
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        // Check if config is enabled
        if (!RenderingConfig.ENABLE_MOUSE_INPUT_FIX.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || hoveredSlot == null) {
            return;
        }

        // Check if the mouse button is bound to the drop key (Q by default)
        // We check by comparing the button number with the key binding's button
        try {
            // Get the drop key binding
            var dropKey = minecraft.options.keyDrop;

            // Check if this key is bound to a mouse button
            // In 1.21.10, we need to check the key's InputConstants.Key
            var key = dropKey.getKey();

            // InputConstants.Type.MOUSE means it's a mouse button
            if (key.getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE) {
                int boundButton = key.getValue();

                // If the clicked button matches the bound button, trigger drop
                if (boundButton == button) {
                    // Drop the hovered item (equivalent to pressing Q)
                    slotClicked(hoveredSlot, hoveredSlot.index, 0, ClickType.THROW);
                    cir.setReturnValue(true);
                    return;
                }
            }

            // Check swap offhand key (F by default)
            var swapKey = minecraft.options.keySwapOffhand;
            key = swapKey.getKey();

            if (key.getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE) {
                int boundButton = key.getValue();

                if (boundButton == button) {
                    // Swap with offhand (equivalent to pressing F)
                    slotClicked(hoveredSlot, hoveredSlot.index, 40, ClickType.SWAP);
                    cir.setReturnValue(true);
                    return;
                }
            }

            // Check hotbar keys (1-9)
            for (int i = 0; i < 9; i++) {
                var hotbarKey = minecraft.options.keyHotbarSlots[i];
                key = hotbarKey.getKey();

                if (key.getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE) {
                    int boundButton = key.getValue();

                    if (boundButton == button) {
                        // Swap with hotbar slot
                        slotClicked(hoveredSlot, hoveredSlot.index, i, ClickType.SWAP);
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }

        } catch (Exception e) {
            // Fail silently to avoid breaking the game
        }
    }
}
