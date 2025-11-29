package com.randomstrangerpassenger.mcopt.mixin;

import com.randomstrangerpassenger.mcopt.config.MCOPTConfig;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.callback.CallbackInfoReturnable;

/**
 * Optimizes chunk rendering by limiting the number of chunk updates per frame.
 * This prevents frame drops when many chunks need to be rebuilt simultaneously.
 */
@Mixin(SectionRenderDispatcher.class)
public class ChunkRenderDispatcherMixin {

    /** Tracks the number of chunk updates processed in the current frame */
    private int mcopt$frameUpdateCount = 0;

    /** Stores the timestamp of the last frame to detect frame changes */
    private long mcopt$lastFrameTime = 0;

    /**
     * Limits chunk updates per frame to prevent FPS drops during world loading
     * or when moving quickly through the world.
     */
    @Inject(
        method = "uploadAllPendingUploads",
        at = @At("HEAD"),
        cancellable = true
    )
    private void limitChunkUpdates(CallbackInfoReturnable<Boolean> cir) {
        if (!MCOPTConfig.ENABLE_CHUNK_OPTIMIZATIONS.get()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Reset counter each frame
        if (currentTime != mcopt$lastFrameTime) {
            mcopt$frameUpdateCount = 0;
            mcopt$lastFrameTime = currentTime;
        }

        // Limit updates per frame based on config
        int limit = MCOPTConfig.CHUNK_UPDATE_LIMIT.get();
        if (mcopt$frameUpdateCount >= limit) {
            cir.setReturnValue(false);
            return;
        }

        mcopt$frameUpdateCount++;
    }
}
