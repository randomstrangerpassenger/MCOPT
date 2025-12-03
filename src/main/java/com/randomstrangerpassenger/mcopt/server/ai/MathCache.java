package com.randomstrangerpassenger.mcopt.server.ai;

import com.randomstrangerpassenger.mcopt.MCOPT;

/**
 * High-performance atan2 caching system for AI calculations.
 * <p>
 * This class pre-computes atan2 lookup tables to avoid expensive runtime
 * calculations
 * during entity AI tick processing (specifically in
 * {@link OptimizedLookControl}).
 * <p>
 * <b>Optimization Note:</b> sin/cos lookup tables were removed as they were
 * completely unused,
 * saving 32KB of memory. Java 21's native Math.sin/cos implementations are fast
 * enough that
 * lookup tables provide negligible benefit on modern CPUs.
 * <p>
 * <b>Memory Usage:</b>
 * <ul>
 * <li>atan2: ~1KB (256 entries × 4 bytes)</li>
 * <li>Total: 1KB (down from 33KB)</li>
 * </ul>
 * <p>
 * <b>Trade-offs:</b>
 * <ul>
 * <li>Precision: Slight loss due to table interpolation vs hardware FPU</li>
 * <li>Performance: atan2 caching still beneficial for frequent AI
 * calculations</li>
 * </ul>
 *
 * @see OptimizedLookControl
 */
public class MathCache {

    // atan2 lookup table configuration
    private static final int ATAN2_BITS = 8; // 8 bits = 256 entries
    private static final int ATAN2_DIM = 1 << (ATAN2_BITS >> 1); // 16x16 = 256
    private static final float[] ATAN2_TABLE = new float[ATAN2_DIM * ATAN2_DIM];

    // Thread-safe initialization flag
    // volatile ensures visibility across threads without requiring synchronization
    // on reads
    private static volatile boolean initialized = false;

    /**
     * Initialize the math cache.
     * Must be called during mod initialization.
     */
    public static void init() {
        if (initialized) {
            return;
        }

        // Initialize atan2 table
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                float x = (float) i / ATAN2_DIM;
                float y = (float) j / ATAN2_DIM;
                ATAN2_TABLE[i * ATAN2_DIM + j] = (float) Math.atan2(y, x);
            }
        }

        initialized = true;
        MCOPT.LOGGER.info("MathCache initialized (atan2 table: {} entries)", ATAN2_TABLE.length);
    }

    /**
     * Fast atan2 approximation using lookup table.
     *
     * @param y Y coordinate
     * @param x X coordinate
     * @return Angle in radians
     */
    public static float atan2(double y, double x) {
        if (!initialized) {
            // Fallback if not initialized (shouldn't happen in normal operation)
            return (float) Math.atan2(y, x);
        }

        if (x >= 0) {
            if (y >= 0) {
                if (x >= y)
                    return atan2_0_45(y, x);
                else
                    return (float) (Math.PI / 2) - atan2_0_45(x, y);
            } else {
                if (x >= -y)
                    return -atan2_0_45(-y, x);
                else
                    return -(float) (Math.PI / 2) + atan2_0_45(x, -y);
            }
        } else {
            if (y >= 0) {
                if (-x >= y)
                    return (float) Math.PI - atan2_0_45(y, -x);
                else
                    return (float) (Math.PI / 2) + atan2_0_45(-x, y);
            } else {
                if (-x >= -y)
                    return -(float) Math.PI + atan2_0_45(-y, -x);
                else
                    return -(float) (Math.PI / 2) - atan2_0_45(-x, -y);
            }
        }
    }

    private static float atan2_0_45(double y, double x) {
        if (x == 0)
            return 0; // Should not happen given logic in atan2, but safe guard
        double z = y / x;
        if (z < 0)
            z = 0;
        if (z > 1)
            z = 1;
        int index = (int) (z * ATAN2_DIM * ATAN2_DIM);
        if (index >= ATAN2_TABLE.length)
            index = ATAN2_TABLE.length - 1;
        return ATAN2_TABLE[index];
    }
}
