package com.randomstrangerpassenger.mcopt.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

// Placeholder because MapRenderer class location is uncertain/changed in 1.21
// and finding it via file search failed.
// Will re-enable when correct mapping is found.
@Mixin(targets = "net.minecraft.client.gui.MapRenderer", remap = false) // Use string target to avoid import error
public class MapRendererMixin {
    // Logic disabled for now
}
