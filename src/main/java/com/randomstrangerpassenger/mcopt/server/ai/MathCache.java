package com.randomstrangerpassenger.mcopt.server.ai;

import com.randomstrangerpassenger.mcopt.MCOPT;

/**
 * High-performance math function caching system for AI calculations.
 * <p>
 * This class pre-computes and caches commonly used math functions (atan2, sin, cos)
 * to avoid expensive runtime calculations during entity AI tick processing.
 * <p>
 * <b>Inspiration:</b> Based on AI-Improvements mod's FastTrig concept, with extended functionality
 * <p>
 * <b>Current Usage:</b>
 * <ul>
 *   <li>atan2: Used in {@link OptimizedLookControl} for mob rotation calculations</li>
 *   <li>sin/cos: Currently UNUSED - candidate for removal</li>
 * </ul>
 * <p>
 * <b>Performance Analysis:</b>
 * <table border="1">
 *   <tr><th>Function</th><th>Minecraft 1.7-1.9</th><th>Minecraft 1.10+</th><th>Java 21+ / Modern CPU</th></tr>
 *   <tr><td>atan2 cache</td><td>Very beneficial</td><td>Beneficial</td><td>Needs benchmarking</td></tr>
 *   <tr><td>sin/cos cache</td><td>10-20% FPS gain</td><td>2-5% FPS gain</td><td>Likely negligible</td></tr>
 * </table>
 * <p>
 * <b>Trade-offs:</b>
 * <ul>
 *   <li>Memory: ~16KB (atan2: 1KB, sin: 16KB, cos: 16KB)</li>
 *   <li>CPU Cache: May cause L1/L2 cache pressure, pushing out more frequently used data</li>
 *   <li>Precision: Slight loss due to table interpolation vs hardware FPU</li>
 *   <li>Modern CPUs: Hardware-accelerated Math functions may outperform table lookups</li>
 * </ul>
 * <p>
 * <b>Benchmark Instructions:</b>
 * <pre>
 * # Run JMH benchmarks to verify effectiveness on your system
 * ./gradlew jmh -PenableJmh
 *
 * # Interpret results:
 * - If atan2 cache is > 50% faster: Keep atan2 caching
 * - If sin/cos cache is < 20% faster: Consider removing (Java 21 has fast native impl)
 * - Check "realWorldScenario" benchmarks for actual usage patterns
 * </pre>
 * <p>
 * <b>Removal Candidates:</b>
 * <ul>
 *   <li>sin/cos tables: Currently unused, occupying 32KB memory</li>
 *   <li>If benchmarks show < 20% improvement, remove to reduce memory footprint</li>
 * </ul>
 *
 * @see OptimizedLookControl
 * @see com.randomstrangerpassenger.mcopt.benchmark.MathCacheBenchmark
 */
public class MathCache {

    // atan2 lookup table configuration
    private static final int ATAN2_BITS = 8;  // 8 bits = 256 entries
    private static final int ATAN2_DIM = 1 << (ATAN2_BITS >> 1);  // 16x16 = 256
    private static final float[] ATAN2_TABLE = new float[ATAN2_DIM * ATAN2_DIM];

    // sin/cos lookup table configuration
    private static final int TRIG_BITS = 12;  // 12 bits = 4096 entries for full circle
    private static final int TRIG_SIZE = 1 << TRIG_BITS;  // 4096
    private static final int TRIG_MASK = TRIG_SIZE - 1;  // 4095
    private static final float[] SIN_TABLE = new float[TRIG_SIZE];
    private static final float[] COS_TABLE = new float[TRIG_SIZE];

    // Conversion factors
    private static final float DEG_TO_INDEX = TRIG_SIZE / 360.0f;
    private static final float RAD_TO_INDEX = TRIG_SIZE / (2.0f * (float) Math.PI);

    // Thread-safe initialization flag
    // volatile ensures visibility across threads without requiring synchronization on reads
    private static volatile boolean initialized = false;

    /**
     * Initialize all math lookup tables.
     * Must be called during mod initialization before any AI processing.
     * Thread-safe using double-checked locking pattern.
     */
    public static void init() {
        // Double-checked locking for thread-safe initialization
        if (initialized) {
            MCOPT.LOGGER.warn("MathCache already initialized, skipping");
            return;
        }

        synchronized (MathCache.class) {
            // Check again inside synchronized block
            if (initialized) {
                return;
            }

            long startTime = System.nanoTime();

            // Pre-compute atan2 table
            initAtan2Table();

            // Pre-compute sin/cos tables
            initTrigTables();

            long endTime = System.nanoTime();
            double elapsedMs = (endTime - startTime) / 1_000_000.0;

            initialized = true;
            MCOPT.LOGGER.info("MathCache initialized in {:.2f}ms (atan2: {} entries, sin/cos: {} entries each)",
                    elapsedMs, ATAN2_TABLE.length, SIN_TABLE.length);
        }
    }

    /**
     * Pre-compute atan2 lookup table.
     * Covers all possible angle combinations in a normalized square.
     */
    private static void initAtan2Table() {
        for (int i = 0; i < ATAN2_DIM; i++) {
            for (int j = 0; j < ATAN2_DIM; j++) {
                float x = (float) i / ATAN2_DIM;
                float y = (float) j / ATAN2_DIM;
                ATAN2_TABLE[j * ATAN2_DIM + i] = (float) Math.atan2(y, x);
            }
        }
    }

    /**
     * Pre-compute sin and cos lookup tables.
     * Covers full 360-degree circle with high precision.
     */
    private static void initTrigTables() {
        for (int i = 0; i < TRIG_SIZE; i++) {
            double angle = (i * 2.0 * Math.PI) / TRIG_SIZE;
            SIN_TABLE[i] = (float) Math.sin(angle);
            COS_TABLE[i] = (float) Math.cos(angle);
        }
    }

    /**
     * Fast atan2 approximation using lookup table.
     *
     * @param y Y coordinate
     * @param x X coordinate
     * @return Angle in radians [-PI, PI]
     */
    public static float atan2(double y, double x) {
        // Handle edge cases
        if (x == 0.0) {
            if (y > 0.0) return (float) Math.PI / 2;
            if (y < 0.0) return -(float) Math.PI / 2;
            return 0.0f;
        }

        // Normalize to first quadrant
        float absY = (float) Math.abs(y);
        float absX = (float) Math.abs(x);

        // Determine which axis is larger for better precision
        boolean swapped = absY > absX;
        float minVal = swapped ? absX : absY;
        float maxVal = swapped ? absY : absX;

        // Normalize to [0, 1] range
        float normalized = minVal / maxVal;

        // Lookup in table
        int index = (int) (normalized * (ATAN2_DIM - 1));
        float angle = ATAN2_TABLE[swapped ? (index * ATAN2_DIM + (ATAN2_DIM - 1)) : index];

        // Adjust for swapped axes
        if (swapped) {
            angle = (float) Math.PI / 2 - angle;
        }

        // Adjust for quadrant
        if (x < 0.0) {
            angle = (float) Math.PI - angle;
        }
        if (y < 0.0) {
            angle = -angle;
        }

        return angle;
    }

    /**
     * Fast sine approximation using lookup table.
     *
     * @param degrees Angle in degrees
     * @return Sine value
     */
    public static float sin(float degrees) {
        int index = (int) (degrees * DEG_TO_INDEX) & TRIG_MASK;
        return SIN_TABLE[index];
    }

    /**
     * Fast sine approximation using lookup table (radians).
     *
     * @param radians Angle in radians
     * @return Sine value
     */
    public static float sinRad(float radians) {
        int index = (int) (radians * RAD_TO_INDEX) & TRIG_MASK;
        return SIN_TABLE[index];
    }

    /**
     * Fast cosine approximation using lookup table.
     *
     * @param degrees Angle in degrees
     * @return Cosine value
     */
    public static float cos(float degrees) {
        int index = (int) (degrees * DEG_TO_INDEX) & TRIG_MASK;
        return COS_TABLE[index];
    }

    /**
     * Fast cosine approximation using lookup table (radians).
     *
     * @param radians Angle in radians
     * @return Cosine value
     */
    public static float cosRad(float radians) {
        int index = (int) (radians * RAD_TO_INDEX) & TRIG_MASK;
        return COS_TABLE[index];
    }

    /**
     * Check if MathCache has been initialized.
     *
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
