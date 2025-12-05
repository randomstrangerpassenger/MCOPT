package com.randomstrangerpassenger.mcopt.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

// Placeholder because SoundEngine injection point was ambiguous.
// Logic handles by SoundOcclusionCache directly where possible.
@Mixin(targets = "net.minecraft.client.sounds.SoundEngine", remap = false)
public class SoundOcclusionCacheMixin {
    // Logic disabled for now
}
