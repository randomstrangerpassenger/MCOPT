/**
 * Utility classes and constants for MCOPT.
 *
 * <p>This package contains shared utilities and constant definitions used across
 * the entire mod to improve code maintainability and reduce magic numbers.</p>
 *
 * <h2>Constants</h2>
 *
 * <h3>{@link com.randomstrangerpassenger.mcopt.util.MCOPTConstants}</h3>
 * <p>Centralized constant definitions organized by category:</p>
 *
 * <h4>Minecraft Constants ({@code MCOPTConstants.Minecraft})</h4>
 * <ul>
 *   <li>{@code CHUNK_SECTION_SIZE} - Block size of chunk section (16)</li>
 *   <li>{@code CHUNK_SECTION_SIZE_DOUBLE} - Double variant for calculations (16.0)</li>
 *   <li>{@code CHUNK_CENTER_OFFSET} - Offset to chunk center (8.0)</li>
 *   <li>{@code ENTITY_BACKFACE_CULLING_DISTANCE_SQ} - Back-face culling threshold (256.0)</li>
 * </ul>
 *
 * <h4>UI Constants ({@code MCOPTConstants.UI})</h4>
 * <ul>
 *   <li>{@code HUD_MARGIN_X} / {@code HUD_MARGIN_Y} - HUD position offsets (5, 5)</li>
 *   <li>{@code COLOR_WHITE} / {@code COLOR_BLACK} - Standard colors (0xFFFFFF, 0x000000)</li>
 *   <li>{@code MEMORY_HUD_UPDATE_INTERVAL_MS} - HUD refresh rate (1000ms)</li>
 *   <li>{@code BYTES_PER_MB} - Memory conversion factor (1024 * 1024)</li>
 * </ul>
 *
 * <h4>Performance Constants ({@code MCOPTConstants.Performance})</h4>
 * <ul>
 *   <li>{@code PANIC_BUTTON_COOLDOWN_MS} - Panic key cooldown (5000ms)</li>
 *   <li>{@code GC_WAIT_TIME_MS} - Wait after GC suggestion (100ms)</li>
 *   <li>{@code BACKFACE_CULLING_DOT_THRESHOLD} - View angle threshold (-0.5)</li>
 * </ul>
 *
 * <h2>Benefits of Centralized Constants</h2>
 * <ul>
 *   <li><b>Maintainability:</b> Change values in one location</li>
 *   <li><b>Readability:</b> Named constants instead of magic numbers</li>
 *   <li><b>Documentation:</b> Constants include JavaDoc explaining their purpose</li>
 *   <li><b>Type Safety:</b> Organized in nested classes by category</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Calculate chunk center position
 * double centerX = sectionX * MCOPTConstants.Minecraft.CHUNK_SECTION_SIZE_DOUBLE
 *                + MCOPTConstants.Minecraft.CHUNK_CENTER_OFFSET;
 *
 * // Position HUD element
 * guiGraphics.drawString(font, text,
 *     MCOPTConstants.UI.HUD_MARGIN_X,
 *     MCOPTConstants.UI.HUD_MARGIN_Y,
 *     MCOPTConstants.UI.COLOR_WHITE);
 *
 * // Convert memory to MB
 * long memoryMB = memoryBytes / MCOPTConstants.UI.BYTES_PER_MB;
 * }</pre>
 *
 * <h2>Future Utilities</h2>
 * <p>This package is designed to grow with additional utility classes as needed:</p>
 * <ul>
 *   <li>Math helpers (if not using MathCache)</li>
 *   <li>Profiling utilities for development</li>
 *   <li>Common validation/assertion helpers</li>
 * </ul>
 *
 * @see com.randomstrangerpassenger.mcopt.util.MCOPTConstants Main constants class
 */
package com.randomstrangerpassenger.mcopt.util;
