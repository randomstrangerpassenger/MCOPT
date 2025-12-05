package com.randomstrangerpassenger.mcopt.client.sound;

import com.randomstrangerpassenger.mcopt.MCOPT;
import com.randomstrangerpassenger.mcopt.config.SoundConfig;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles sound optimization logic including volume-based culling and duplicate
 * limiting.
 * <p>
 * Immersion-first approach: only culls sounds that are truly inaudible or
 * redundant,
 * preserving the vanilla audio experience while reducing processing overhead.
 * </p>
 */
@SuppressWarnings("null")
public final class SoundCullingHandler {

    private SoundCullingHandler() {
        // Utility class
    }

    // Track active sounds by type for duplicate limiting
    private static final Map<ResourceLocation, List<SoundEntry>> activeSounds = new ConcurrentHashMap<>();
    private static long lastCleanupTick = 0;

    // Cached config values
    private static double minVolume = 0.01;
    private static int maxDuplicates = 4;
    private static double duplicateRadius = 16.0;
    private static int cleanupInterval = 10;

    /**
     * Refresh cached config values.
     * Call this when config is reloaded.
     */
    public static void refreshConfigCache() {
        minVolume = SoundConfig.MINIMUM_AUDIBLE_VOLUME.get();
        maxDuplicates = SoundConfig.MAX_DUPLICATE_SOUNDS.get();
        duplicateRadius = SoundConfig.DUPLICATE_SOUND_RADIUS.get();
        cleanupInterval = SoundConfig.DUPLICATE_CHECK_INTERVAL_TICKS.get();
    }

    /**
     * Determines if a sound should be culled based on optimization rules.
     *
     * @param sound       The sound instance to check
     * @param listenerPos The listener (player) position
     * @param currentTick Current game tick
     * @return true if sound should be culled (not played), false otherwise
     */
    public static boolean shouldCullSound(SoundInstance sound, Vec3 listenerPos, long currentTick) {
        if (!SoundConfig.ENABLE_SOUND_OPTIMIZATIONS.get()) {
            return false;
        }

        // Periodic cleanup of old sound entries
        if (currentTick - lastCleanupTick >= cleanupInterval) {
            cleanupOldEntries(currentTick);
            lastCleanupTick = currentTick;
        }

        // 1. Volume-based culling: skip sounds that are too quiet to hear
        if (shouldCullByVolume(sound, listenerPos)) {
            MCOPT.LOGGER.debug("[SoundCull] Culled inaudible sound: {} (volume too low)",
                    sound.getLocation());
            return true;
        }

        // 2. Duplicate sound limiting: limit identical sounds in same area
        if (shouldCullAsDuplicate(sound, listenerPos, currentTick)) {
            MCOPT.LOGGER.debug("[SoundCull] Culled duplicate sound: {} (max {} reached)",
                    sound.getLocation(), maxDuplicates);
            return true;
        }

        // Sound passed all checks - register it for duplicate tracking
        registerSound(sound, listenerPos, currentTick);
        return false;
    }

    /**
     * Check if sound should be culled due to effectively zero volume.
     */
    private static boolean shouldCullByVolume(SoundInstance sound, Vec3 listenerPos) {
        float volume = sound.getVolume();

        // Base volume check
        if (volume < minVolume) {
            return true;
        }

        // Distance attenuation check
        // Minecraft uses linear attenuation: volume decreases with distance
        double distance = listenerPos.distanceTo(new Vec3(sound.getX(), sound.getY(), sound.getZ()));
        float attenuationDistance = sound.getAttenuation() == SoundInstance.Attenuation.LINEAR
                ? 16.0f // Default attenuation distance
                : 1.0f;

        // Calculate effective volume after distance attenuation
        double effectiveVolume = volume * Math.max(0, 1.0 - (distance / attenuationDistance));

        return effectiveVolume < minVolume;
    }

    /**
     * Check if sound should be culled as a duplicate.
     * Limits the number of identical sounds playing simultaneously in an area.
     */
    private static boolean shouldCullAsDuplicate(SoundInstance sound, Vec3 listenerPos, long currentTick) {
        ResourceLocation soundId = sound.getLocation();
        List<SoundEntry> entries = activeSounds.get(soundId);

        if (entries == null || entries.isEmpty()) {
            return false;
        }

        Vec3 soundPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());

        // Count how many of this sound type are playing nearby
        int nearbyCount = 0;
        double closestDistance = Double.MAX_VALUE;

        synchronized (entries) {
            for (SoundEntry entry : entries) {
                // Skip expired entries
                if (currentTick - entry.tick > 20) { // 1 second expiry
                    continue;
                }

                Vec3 entryPos = java.util.Objects.requireNonNull(entry.position, "Entry position cannot be null");
                double dist = entryPos.distanceTo(soundPos);
                if (dist < duplicateRadius) {
                    nearbyCount++;
                    closestDistance = Math.min(closestDistance, entry.position.distanceTo(listenerPos));
                }
            }
        }

        // If we've reached max duplicates, cull sounds farther from player
        if (nearbyCount >= maxDuplicates) {
            double newSoundDistance = listenerPos.distanceTo(soundPos);
            // Only cull if this sound is farther than the closest existing one
            return newSoundDistance > closestDistance;
        }

        return false;
    }

    /**
     * Register a sound for duplicate tracking.
     */
    private static void registerSound(SoundInstance sound, Vec3 listenerPos, long currentTick) {
        ResourceLocation soundId = sound.getLocation();
        Vec3 soundPos = new Vec3(sound.getX(), sound.getY(), sound.getZ());

        activeSounds.computeIfAbsent(soundId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new SoundEntry(soundPos, currentTick));
    }

    /**
     * Clean up old sound entries to prevent memory leaks.
     */
    private static void cleanupOldEntries(long currentTick) {
        activeSounds.forEach((id, entries) -> {
            synchronized (entries) {
                entries.removeIf(entry -> currentTick - entry.tick > 40); // 2 second expiry
            }
        });

        // Remove empty lists
        activeSounds.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Clear all tracked sounds. Called when world unloads.
     */
    public static void clearAllSounds() {
        activeSounds.clear();
        lastCleanupTick = 0;
    }

    /**
     * Simple record to track sound positions and timing.
     */
    private record SoundEntry(Vec3 position, long tick) {
    }
}
