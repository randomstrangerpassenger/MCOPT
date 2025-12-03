package com.randomstrangerpassenger.mcopt.util;

/**
 * Central location for commonly used constants in MCOPT.
 * Extracting these values improves maintainability and reduces magic numbers in
 * the codebase.
 */
public final class MCOPTConstants {

        private MCOPTConstants() {
                // Utility class - prevent instantiation
        }

        /**
         * Minecraft world constants
         */
        public static final class Minecraft {
                private Minecraft() {
                }

                /** Number of blocks in one chunk section (width/length/height) */
                public static final int CHUNK_SECTION_SIZE = 16;

                /** Chunk section size as a double for calculations */
                public static final double CHUNK_SECTION_SIZE_DOUBLE = 16.0;

                /** Half of chunk section size - offset to chunk center */
                public static final double CHUNK_CENTER_OFFSET = 8.0;

                /** Ticks per second in Minecraft (20 ticks = 1 second) */
                public static final double TICKS_PER_SECOND = 20.0;

                /** Distance threshold for entity back-face culling (in blocks squared) */
                public static final double ENTITY_BACKFACE_CULLING_DISTANCE_SQ = 256.0; // 16 blocks squared
        }

        /**
         * UI and rendering constants
         */
        public static final class UI {
                private UI() {
                }

                /** Default horizontal margin for HUD elements */
                public static final int HUD_MARGIN_X = 5;

                /** Default vertical margin for HUD elements */
                public static final int HUD_MARGIN_Y = 5;

                /** White color (ARGB format) */
                public static final int COLOR_WHITE = 0xFFFFFF;

                /** Black color (ARGB format) */
                public static final int COLOR_BLACK = 0x000000;

                /** Memory HUD update interval in milliseconds */
                public static final long MEMORY_HUD_UPDATE_INTERVAL_MS = 1000L;

                /** Bytes per megabyte conversion factor */
                public static final long BYTES_PER_MB = 1024L * 1024L;
        }

        /**
         * Performance and timing constants
         */
        public static final class Performance {
                private Performance() {
                }

                /** Cooldown period for panic button to prevent spam (milliseconds) */
                public static final long PANIC_BUTTON_COOLDOWN_MS = 5000L;

                /** Wait time after triggering garbage collection (milliseconds) */
                public static final long GC_WAIT_TIME_MS = 100L;

                /** Back-face culling dot product threshold */
                public static final double BACKFACE_CULLING_DOT_THRESHOLD = -0.5;

                /** Maximum number of particles that can spawn per tick */
                public static final int PARTICLE_SPAWN_LIMIT_PER_TICK = 256;

                /** Milliseconds per second */
                public static final long MILLIS_PER_SECOND = 1000L;
        }

        /**
         * Input constants
         */
        public static final class Input {
                private Input() {
                }

                /** GLFW Input action: Release */
                public static final int INPUT_ACTION_RELEASE = 0; // GLFW_RELEASE
        }

        /**
         * Fishing constants
         */
        public static final class Fishing {
                private Fishing() {
                }

                /**
                 * Maximum squared distance for fishing hook before it's considered
                 * broken/detached
                 */
                public static final double MAX_FISHING_DISTANCE_SQUARED = 1024.0; // 32 blocks
        }
}
