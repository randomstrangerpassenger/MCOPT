package com.randomstrangerpassenger.mcopt.memory;

import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Object pool for Vec3 to reduce garbage collection pressure.
 * Provides reusable Vec3 instances for temporary calculations.
 */
public class Vec3Pool {
    private static final int MAX_POOL_SIZE = 1000;
    private static final ConcurrentLinkedQueue<MutableVec3> pool = new ConcurrentLinkedQueue<>();
    private static final ThreadLocal<MutableVec3> threadLocalVec3 = ThreadLocal.withInitial(MutableVec3::new);

    /**
     * Get a pooled Vec3 instance for single-threaded use.
     * This is safe for temporary calculations within a single method.
     * WARNING: Do not store the returned reference - it will be reused!
     */
    public static MutableVec3 get(double x, double y, double z) {
        MutableVec3 vec = threadLocalVec3.get();
        vec.set(x, y, z);
        return vec;
    }

    /**
     * Acquire a Vec3 from the pool for multi-threaded use.
     * Call release() when done to return it to the pool.
     */
    public static MutableVec3 acquire(double x, double y, double z) {
        MutableVec3 vec = pool.poll();
        if (vec == null) {
            vec = new MutableVec3();
        }
        vec.set(x, y, z);
        return vec;
    }

    /**
     * Return a Vec3 to the pool for reuse.
     */
    public static void release(MutableVec3 vec) {
        if (pool.size() < MAX_POOL_SIZE) {
            pool.offer(vec);
        }
    }

    /**
     * Clear the pool to free memory.
     */
    public static void clear() {
        pool.clear();
    }

    /**
     * Mutable Vec3 wrapper for pooling.
     */
    public static class MutableVec3 extends Vec3 {
        public MutableVec3() {
            super(0, 0, 0);
        }

        public void set(double x, double y, double z) {
            // Access the fields directly using Mixin accessor or reflection
            // For now, we'll use a simple approach
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void set(Vec3 other) {
            set(other.x, other.y, other.z);
        }

        // Add common operations that return this instance instead of creating new ones
        public MutableVec3 addMutable(double x, double y, double z) {
            this.x += x;
            this.y += y;
            this.z += z;
            return this;
        }

        public MutableVec3 scaleMutable(double factor) {
            this.x *= factor;
            this.y *= factor;
            this.z *= factor;
            return this;
        }
    }
}
