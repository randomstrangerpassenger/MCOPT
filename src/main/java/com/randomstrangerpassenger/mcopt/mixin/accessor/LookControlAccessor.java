package com.randomstrangerpassenger.mcopt.mixin.accessor;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LookControl.class)
public interface LookControlAccessor {
    @Accessor("mob")
    Mob getMob();
}
