package com.randomstrangerpassenger.mcopt.mixin.common;

import com.randomstrangerpassenger.mcopt.server.entity.ai.PathfindingCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.PathNavigationRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(PathFinder.class)
@SuppressWarnings("null")
public class PathFinderMixin {

    // Target: findPath(PathNavigationRegion, Mob, Set<BlockPos>, float, int, float)
    // Note: Signature varies by mapping. Check carefully.

    // In many mappings: findPath(PathNavigationRegion region, Mob mob,
    // Set<BlockPos> targetPositions, float maxRange, int accuracy, float
    // searchDepth)

    @Inject(method = "findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("HEAD"), cancellable = true)
    private void onFindPath(PathNavigationRegion region, Mob mob, Set<BlockPos> targetPositions, float maxRange,
            int accuracy, float searchDepth, CallbackInfoReturnable<Path> cir) {
        if (PathfindingCache.isEnabled() && targetPositions.size() == 1) {
            BlockPos target = targetPositions.iterator().next();
            Path cached = PathfindingCache.getCachedPath(mob, target);
            if (cached != null) {
                cir.setReturnValue(cached);
            }
        }
    }

    @Inject(method = "findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
    private void onFindPathReturn(PathNavigationRegion region, Mob mob, Set<BlockPos> targetPositions, float maxRange,
            int accuracy, float searchDepth, CallbackInfoReturnable<Path> cir) {
        if (PathfindingCache.isEnabled() && targetPositions.size() == 1) {
            Path result = cir.getReturnValue();
            if (result != null) {
                BlockPos target = targetPositions.iterator().next();
                PathfindingCache.cachePath(mob, target, result);
            }
        }
    }
}
