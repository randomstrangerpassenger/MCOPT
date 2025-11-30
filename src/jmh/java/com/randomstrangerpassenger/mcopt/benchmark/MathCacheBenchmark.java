package com.randomstrangerpassenger.mcopt.benchmark;

import com.randomstrangerpassenger.mcopt.ai.MathCache;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for MathCache effectiveness on Java 21+ and modern CPUs.
 *
 * This benchmark compares the performance of:
 * 1. MathCache lookup tables vs native Math functions
 * 2. Memory access patterns and cache efficiency
 * 3. Different usage scenarios (AI entity calculations)
 *
 * To run this benchmark:
 * ./gradlew jmh
 *
 * Expected results on Java 21+ with modern CPUs:
 * - atan2: MathCache may be 1.5-3x faster
 * - sin/cos: Native Math may be comparable or faster due to hardware acceleration
 *
 * Interpretation:
 * - If MathCache.atan2 is > 50% faster: Keep atan2 caching
 * - If MathCache.sin/cos is < 20% faster: Consider removing sin/cos caching
 * - Consider L1/L2 cache miss rates and memory footprint (16KB+)
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
public class MathCacheBenchmark {

    // Test data - typical values from mob AI calculations
    private double[] xValues;
    private double[] yValues;
    private double[] angles;
    private static final int DATA_SIZE = 1000;

    @Setup
    public void setup() {
        MathCache.init();

        Random random = new Random(42);
        xValues = new double[DATA_SIZE];
        yValues = new double[DATA_SIZE];
        angles = new double[DATA_SIZE];

        // Generate realistic values from mob AI
        for (int i = 0; i < DATA_SIZE; i++) {
            // Position differences typically range from -100 to +100 blocks
            xValues[i] = (random.nextDouble() - 0.5) * 200.0;
            yValues[i] = (random.nextDouble() - 0.5) * 200.0;

            // Angles from 0 to 360 degrees
            angles[i] = random.nextDouble() * 360.0;
        }
    }

    // ========== atan2 Benchmarks ==========

    @Benchmark
    public void baselineAtan2_JavaMath(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            double result = Math.atan2(yValues[i], xValues[i]);
            bh.consume(result);
        }
    }

    @Benchmark
    public void optimizedAtan2_MathCache(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            float result = MathCache.atan2(yValues[i], xValues[i]);
            bh.consume(result);
        }
    }

    // ========== sin/cos Benchmarks ==========

    @Benchmark
    public void baselineSin_JavaMath(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            double radians = Math.toRadians(angles[i]);
            double result = Math.sin(radians);
            bh.consume(result);
        }
    }

    @Benchmark
    public void optimizedSin_MathCache(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            float result = MathCache.sin((float) angles[i]);
            bh.consume(result);
        }
    }

    @Benchmark
    public void baselineCos_JavaMath(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            double radians = Math.toRadians(angles[i]);
            double result = Math.cos(radians);
            bh.consume(result);
        }
    }

    @Benchmark
    public void optimizedCos_MathCache(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            float result = MathCache.cos((float) angles[i]);
            bh.consume(result);
        }
    }

    // ========== Combined AI-style workload ==========

    /**
     * Simulates typical AI look control calculation.
     * This represents the actual usage pattern in OptimizedLookControl.
     */
    @Benchmark
    public void realWorldScenario_JavaMath(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            double dx = xValues[i];
            double dy = yValues[i];
            double dz = xValues[(i + 1) % DATA_SIZE];

            // Calculate pitch
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            double pitch = -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));

            // Calculate yaw
            double yaw = Math.atan2(dz, dx) * (180.0 / Math.PI) - 90.0;

            bh.consume(pitch);
            bh.consume(yaw);
        }
    }

    @Benchmark
    public void realWorldScenario_MathCache(Blackhole bh) {
        for (int i = 0; i < DATA_SIZE; i++) {
            double dx = xValues[i];
            double dy = yValues[i];
            double dz = xValues[(i + 1) % DATA_SIZE];

            // Calculate pitch
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            float pitch = (float) (-(MathCache.atan2(dy, horizontalDist) * (180.0 / Math.PI)));

            // Calculate yaw
            float yaw = (float) (MathCache.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;

            bh.consume(pitch);
            bh.consume(yaw);
        }
    }
}
