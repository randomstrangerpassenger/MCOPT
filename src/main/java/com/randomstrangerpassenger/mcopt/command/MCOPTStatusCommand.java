package com.randomstrangerpassenger.mcopt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.randomstrangerpassenger.mcopt.config.GameplayConfig;
import com.randomstrangerpassenger.mcopt.config.PerformanceConfig;
import com.randomstrangerpassenger.mcopt.config.RenderingConfig;
import com.randomstrangerpassenger.mcopt.config.SafetyConfig;
import com.randomstrangerpassenger.mcopt.util.FeatureKey;
import com.randomstrangerpassenger.mcopt.util.FeatureToggles;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

/**
 * Command to display MCOPT's current status and configuration.
 * <p>
 * Usage: /mcopt status
 * <p>
 * Displays:
 * - Memory usage
 * - Active optimization modules
 * - Performance statistics (if available)
 * - Detected incompatible mods
 */
public class MCOPTStatusCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mcopt")
                        .then(Commands.literal("status")
                                .executes(MCOPTStatusCommand::executeStatus))
                        .then(Commands.literal("report")
                                .executes(MCOPTStatusCommand::executeStatus)));
    }

    private static int executeStatus(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // Header
        source.sendSuccess(() -> Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("MCOPT Status Report")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);

        // Memory Status
        displayMemoryStatus(source);

        // Active Modules
        displayActiveModules(source);

        // Performance Statistics
        displayPerformanceStats(source);

        // Incompatible Mods
        displayIncompatibleMods(source);

        // Footer
        source.sendSuccess(() -> Component.literal("═══════════════════════════════════")
                .withStyle(ChatFormatting.GOLD), false);

        return 1;
    }

    private static void displayMemoryStatus(CommandSourceStack source) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        int memoryPercent = (int) ((usedMemory * 100) / maxMemory);

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Memory Usage:")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);
        source.sendSuccess(() -> Component.literal(String.format("  Used: %dMB / %dMB (%d%%)",
                usedMemory, maxMemory, memoryPercent))
                .withStyle(memoryPercent > 90 ? ChatFormatting.RED :
                        memoryPercent > 75 ? ChatFormatting.YELLOW :
                                ChatFormatting.GREEN),
                false);
    }

    private static void displayActiveModules(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Active Modules:")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);

        // Rendering
        displayModule(source, "Chunk Optimizations",
                RenderingConfig.ENABLE_CHUNK_OPTIMIZATIONS.get());
        displayModule(source, "Entity Culling",
                RenderingConfig.ENABLE_ENTITY_CULLING.get());
        displayModule(source, "Block Entity Culling",
                RenderingConfig.ENABLE_BLOCK_ENTITY_CULLING.get());
        displayModule(source, "Particle Optimizations",
                RenderingConfig.ENABLE_PARTICLE_OPTIMIZATIONS.get());
        displayModule(source, "Smart Leaves",
                RenderingConfig.ENABLE_SMART_LEAVES.get());

        // Performance
        displayModule(source, "AI Optimizations",
                FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS));
        displayModule(source, "Dynamic FPS",
                FeatureToggles.isEnabled(FeatureKey.DYNAMIC_FPS));
        displayModule(source, "XP Orb Merging",
                FeatureToggles.isEnabled(FeatureKey.XP_ORB_MERGING));

        // Safety
        displayModule(source, "Leak Guard",
                FeatureToggles.isEnabled(FeatureKey.LEAK_GUARD));
        displayModule(source, "Action Guard",
                FeatureToggles.isEnabled(FeatureKey.ACTION_GUARD));
        displayModule(source, "Clear Lag",
                SafetyConfig.ENABLE_CLEAR_LAG.get());
        displayModule(source, "Per-Chunk Entity Limit",
                SafetyConfig.ENABLE_PER_CHUNK_ENTITY_LIMIT.get());

        // Fixes
        displayModule(source, "Login Timeout Fix",
                GameplayConfig.ENABLE_LOGIN_TIMEOUT_FIX.get());
    }

    private static void displayModule(CommandSourceStack source, String name, boolean enabled) {
        String status = enabled ? "ON" : "OFF";
        ChatFormatting color = enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        source.sendSuccess(() -> Component.literal(String.format("  %s: %s", name, status))
                .withStyle(color), false);
    }

    private static void displayPerformanceStats(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("Configuration:")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), false);

        if (RenderingConfig.ENABLE_ENTITY_CULLING.get()) {
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Entity Culling Distance: %d blocks",
                    RenderingConfig.ENTITY_CULLING_DISTANCE.get()))
                    .withStyle(ChatFormatting.GRAY), false);
        }

        if (RenderingConfig.ENABLE_BLOCK_ENTITY_CULLING.get()) {
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Block Entity Culling Distance: %d blocks",
                    RenderingConfig.BLOCK_ENTITY_CULLING_DISTANCE.get()))
                    .withStyle(ChatFormatting.GRAY), false);
        }

        if (SafetyConfig.ENABLE_PER_CHUNK_ENTITY_LIMIT.get()) {
            source.sendSuccess(() -> Component.literal(String.format(
                    "  Max Entities Per Chunk: %d",
                    SafetyConfig.MAX_ENTITIES_PER_CHUNK.get()))
                    .withStyle(ChatFormatting.GRAY), false);
        }

        if (PerformanceConfig.ENABLE_MEMORY_OPTIMIZATIONS.get()) {
            source.sendSuccess(() -> Component.literal("  Memory Optimizations: Active")
                    .withStyle(ChatFormatting.GRAY), false);
        }
    }

    private static void displayIncompatibleMods(CommandSourceStack source) {
        boolean hasIncompatible = false;

        // Check for known mod conflicts
        if (ModList.get().isLoaded("clumps") && FeatureToggles.isEnabled(FeatureKey.XP_ORB_MERGING)) {
            if (!hasIncompatible) {
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.literal("Detected Conflicts:")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                hasIncompatible = true;
            }
            source.sendSuccess(() -> Component.literal("  [Clumps] -> XP Merging Disabled")
                    .withStyle(ChatFormatting.YELLOW), false);
        }

        if (ModList.get().isLoaded("dynamic_fps") && FeatureToggles.isEnabled(FeatureKey.DYNAMIC_FPS)) {
            if (!hasIncompatible) {
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.literal("Detected Conflicts:")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                hasIncompatible = true;
            }
            source.sendSuccess(() -> Component.literal("  [Dynamic FPS] -> MCOPT DynamicFPS Disabled")
                    .withStyle(ChatFormatting.YELLOW), false);
        }

        if (ModList.get().isLoaded("aiimprovements") && FeatureToggles.isEnabled(FeatureKey.AI_OPTIMIZATIONS)) {
            if (!hasIncompatible) {
                source.sendSuccess(() -> Component.literal(""), false);
                source.sendSuccess(() -> Component.literal("Detected Conflicts:")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
                hasIncompatible = true;
            }
            source.sendSuccess(() -> Component.literal("  [AI Improvements] -> MCOPT AI Disabled")
                    .withStyle(ChatFormatting.YELLOW), false);
        }

        if (!hasIncompatible) {
            source.sendSuccess(() -> Component.literal(""), false);
            source.sendSuccess(() -> Component.literal("No Conflicts Detected")
                    .withStyle(ChatFormatting.GREEN), false);
        }
    }
}
