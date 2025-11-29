package com.randomstrangerpassenger.mcopt.memory;

import net.minecraft.core.BlockPos;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Object pool for BlockPos to reduce garbage collection pressure.
 * Uses BlockPos.MutableBlockPos for efficient reuse.
 */
public class BlockPosPool {
    private static final int MAX_POOL_SIZE = 500;
    private static final ConcurrentLinkedQueue<BlockPos.MutableBlockPos> pool = new ConcurrentLinkedQueue<>();
    private static final ThreadLocal<BlockPos.MutableBlockPos> threadLocalPos =
        ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

    /**
     * Get a pooled MutableBlockPos for single-threaded use.
     * This is safe for temporary calculations within a single method.
     * WARNING: Do not store the returned reference - it will be reused!
     */
    public static BlockPos.MutableBlockPos get(int x, int y, int z) {
        BlockPos.MutableBlockPos pos = threadLocalPos.get();
        pos.set(x, y, z);
        return pos;
    }

    /**
     * Get a pooled MutableBlockPos from another BlockPos.
     */
    public static BlockPos.MutableBlockPos get(BlockPos pos) {
        return get(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Acquire a MutableBlockPos from the pool for multi-threaded use.
     * Call release() when done to return it to the pool.
     */
    public static BlockPos.MutableBlockPos acquire(int x, int y, int z) {
        BlockPos.MutableBlockPos pos = pool.poll();
        if (pos == null) {
            pos = new BlockPos.MutableBlockPos();
        }
        pos.set(x, y, z);
        return pos;
    }

    /**
     * Return a MutableBlockPos to the pool for reuse.
     */
    public static void release(BlockPos.MutableBlockPos pos) {
        if (pool.size() < MAX_POOL_SIZE) {
            pool.offer(pos);
        }
    }

    /**
     * Clear the pool to free memory.
     */
    public static void clear() {
        pool.clear();
    }
}
