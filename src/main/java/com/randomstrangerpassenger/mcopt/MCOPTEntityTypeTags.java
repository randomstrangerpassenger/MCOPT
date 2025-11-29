package com.randomstrangerpassenger.mcopt;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Custom entity type tags for MCOPT mod.
 *
 * Using tags instead of hardcoded class references improves:
 * - Mod compatibility (other mods can add their entities to our tags)
 * - Data pack compatibility (users can customize which entities are affected)
 * - Maintainability (easier to add/remove entities)
 */
public class MCOPTEntityTypeTags {

    /**
     * Farm animals that typically have breeding, tempting, and strolling behaviors.
     * Includes: Cow, Pig, Chicken, Sheep, etc.
     */
    public static final TagKey<EntityType<?>> FARM_ANIMALS = tag("farm_animals");

    /**
     * Sheep specifically, for wool-eating behavior optimization.
     */
    public static final TagKey<EntityType<?>> WOOL_GROWING_ANIMALS = tag("wool_growing_animals");

    /**
     * Fish that have swimming behaviors.
     * Includes: Cod, Salmon, Tropical Fish, Pufferfish
     */
    public static final TagKey<EntityType<?>> FISH = tag("fish");

    /**
     * Squid and similar aquatic mobs with random movement patterns.
     */
    public static final TagKey<EntityType<?>> SQUID_LIKE = tag("squid_like");

    /**
     * Helper method to create a tag key.
     */
    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("mcopt", name));
    }
}
