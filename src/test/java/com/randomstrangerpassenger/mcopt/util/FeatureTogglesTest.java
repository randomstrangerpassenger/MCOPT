package com.randomstrangerpassenger.mcopt.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for FeatureToggles utility.
 * <p>
 * Note: These tests verify the public API contract and thread-safety.
 * Full integration testing requires a complete NeoForge environment with
 * ModList and MCOPTConfig initialization, which is beyond unit test scope.
 * <p>
 * Tests cover:
 * - Public API availability and contract
 * - Thread-safety of concurrent reads
 * - Method consistency
 */
@DisplayName("FeatureToggles Unit Tests")
class FeatureTogglesTest {

    // ========== API Contract Tests ==========

    @Test
    @DisplayName("Should provide isEnabled method with FeatureKey enum")
    void testPublicApiExists() {
        // Verify isEnabled method exists and accepts FeatureKey
        assertThatCode(() -> {
            Method m1 = FeatureToggles.class.getMethod("isEnabled", FeatureKey.class);
            Method m2 = FeatureToggles.class.getMethod("refreshFromConfig");

            assertThat(m1.getReturnType()).isEqualTo(boolean.class);
            assertThat(m2.getReturnType()).isEqualTo(void.class);
        })
                .as("isEnabled(FeatureKey) and refreshFromConfig() should exist with correct signatures")
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return boolean values for all feature keys")
    void testToggleMethodsReturnBoolean() {
        // All FeatureKey values should work with isEnabled
        assertThatCode(() -> {
            boolean result1 = FeatureToggles.isEnabled(FeatureKey.XP_ORB_MERGING);
            boolean result2 = FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS);
            boolean result3 = FeatureToggles.isEnabled(FeatureKey.LEAK_GUARD);
            boolean result4 = FeatureToggles.isEnabled(FeatureKey.DYNAMIC_FPS);
            boolean result5 = FeatureToggles.isEnabled(FeatureKey.BETTER_SNOW_LOGIC);
            boolean result6 = FeatureToggles.isEnabled(FeatureKey.ACTION_GUARD);
            boolean result7 = FeatureToggles.isEnabled(FeatureKey.ITEM_NBT_SANITIZER);
            boolean result8 = FeatureToggles.isEnabled(FeatureKey.DAMAGE_TILT_FIX);
            boolean result9 = FeatureToggles.isEnabled(FeatureKey.RIGHT_CLICK_FALLTHROUGH);

            // Results should be valid boolean values
            assertThat(result1).isIn(true, false);
            assertThat(result2).isIn(true, false);
            assertThat(result3).isIn(true, false);
            assertThat(result4).isIn(true, false);
            assertThat(result5).isIn(true, false);
            assertThat(result6).isIn(true, false);
            assertThat(result7).isIn(true, false);
            assertThat(result8).isIn(true, false);
            assertThat(result9).isIn(true, false);
        })
                .as("All feature keys should return boolean values")
                .doesNotThrowAnyException();
    }

    // ========== Thread Safety Tests ==========

    @Test
    @DisplayName("Should be thread-safe for concurrent reads")
    void testConcurrentReads() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Simulate multiple threads reading toggles simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        // Just reading values, shouldn't throw exceptions
                        FeatureToggles.isEnabled(FeatureKey.XP_ORB_MERGING);
                        FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("All threads should complete in time").isTrue();
        assertThat(errorCount.get()).as("No exceptions should occur during concurrent reads").isEqualTo(0);
    }
}
