package com.randomstrangerpassenger.mcopt.server.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for MathCache lookup table functionality.
 * <p>
 * Tests cover:
 * - Initialization (thread-safe, idempotent)
 * - Mathematical accuracy (compared to Math.* functions)
 * - Edge cases (zero, negative, special angles)
 */
class MathCacheTest {

    private static final float ATAN2_TOLERANCE = 0.0001f;

    // ========== Initialization Tests ==========

    @Test
    @DisplayName("Should initialize successfully")
    void testInitialization() {
        // MathCache.isInitialized() is package-private, so we test side effects (no
        // exception)
        assertThatCode(MathCache::init).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should be idempotent (multiple init calls are safe)")
    void testIdempotentInitialization() {
        // Should not throw or cause issues
        assertThatCode(() -> {
            MathCache.init();
            MathCache.init();
            MathCache.init();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should be thread-safe during initialization")
    void testThreadSafeInitialization() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Simulate multiple threads trying to initialize simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    MathCache.init();
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get())
                .as("All threads should complete initialization without errors")
                .isEqualTo(threadCount);
    }

    // ========== atan2 Accuracy Tests ==========

    @ParameterizedTest
    @DisplayName("Should calculate atan2 accurately for common cases")
    @CsvSource({
            "0.0, 1.0", // 0 degrees
            "1.0, 1.0", // 45 degrees
            "1.0, 0.0", // 90 degrees
            "1.0, -1.0", // 135 degrees
            "0.0, -1.0", // 180 degrees
            "-1.0, -1.0", // 225 degrees
            "-1.0, 0.0", // 270 degrees
            "-1.0, 1.0" // 315 degrees
    })
    void testAtan2Accuracy(double y, double x) {
        float expected = (float) Math.atan2(y, x);
        float actual = MathCache.atan2(y, x);

        assertThat(actual).isCloseTo(expected, within(ATAN2_TOLERANCE));
    }

    @ParameterizedTest
    @DisplayName("Should handle zero inputs correctly")
    @CsvSource({
            "0.0, 0.0",
            "0.0, 10.0",
            "10.0, 0.0"
    })
    void testAtan2ZeroHandling(double y, double x) {
        float expected = (float) Math.atan2(y, x);
        float actual = MathCache.atan2(y, x);

        assertThat(actual).isCloseTo(expected, within(ATAN2_TOLERANCE));
    }

    @Test
    @DisplayName("Should handle large values without overflow issues")
    void testAtan2LargeValues() {
        double large = 1000000.0;
        float expected = (float) Math.atan2(large, large);
        float actual = MathCache.atan2(large, large);

        assertThat(actual).isCloseTo(expected, within(ATAN2_TOLERANCE));
    }

    // ========== Performance/Stress Tests (Optional) ==========

    @Test
    @DisplayName("Should be faster or comparable to Math.atan2 (sanity check)")
    void testPerformanceSanityCheck() {
        // This is just a basic check to ensure it's not terribly slow
        // Real benchmarking should be done with JMH
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            MathCache.atan2(i, i + 1);
        }
        long duration = System.nanoTime() - start;

        assertThat(duration).isLessThan(100_000_000L); // Should take < 100ms for 10k ops
    }
}
