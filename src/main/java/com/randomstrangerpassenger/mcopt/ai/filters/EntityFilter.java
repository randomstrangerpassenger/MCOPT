package com.randomstrangerpassenger.mcopt.ai.filters;

import net.minecraft.world.entity.Entity;

/**
 * Functional interface for filtering entities before AI goal processing.
 *
 * Filters determine whether an entity should have its AI goals modified.
 * Multiple filters can be chained together for complex filtering logic.
 */
@FunctionalInterface
public interface EntityFilter {

    /**
     * Check if this filter applies to the given entity.
     *
     * @param entity The entity to check
     * @return FilterResult indicating how to proceed
     */
    FilterResult test(Entity entity);

    /**
     * Create a filter that matches entities of a specific class.
     *
     * @param entityClass The class to match
     * @return Filter that matches the specified class
     */
    static EntityFilter matchClass(Class<? extends Entity> entityClass) {
        return entity -> entityClass.isInstance(entity) ? FilterResult.MATCH : FilterResult.PASS;
    }

    /**
     * Create a filter that matches entities by registry name.
     *
     * @param registryName The registry name to match (e.g., "minecraft:cow")
     * @return Filter that matches the specified registry name
     */
    static EntityFilter matchRegistryName(String registryName) {
        return entity -> {
            String entityId = entity.getType().toString();
            return entityId.equals(registryName) ? FilterResult.MATCH : FilterResult.PASS;
        };
    }

    /**
     * Combine this filter with another using AND logic.
     *
     * @param other The other filter
     * @return Combined filter
     */
    default EntityFilter and(EntityFilter other) {
        return entity -> {
            FilterResult first = this.test(entity);
            if (first == FilterResult.REJECT) return FilterResult.REJECT;
            if (first == FilterResult.PASS) return other.test(entity);
            // first is MATCH - check second filter too
            FilterResult second = other.test(entity);
            return second == FilterResult.REJECT ? FilterResult.REJECT : FilterResult.MATCH;
        };
    }

    /**
     * Combine this filter with another using OR logic.
     *
     * @param other The other filter
     * @return Combined filter
     */
    default EntityFilter or(EntityFilter other) {
        return entity -> {
            FilterResult first = this.test(entity);
            if (first == FilterResult.MATCH) return FilterResult.MATCH;
            if (first == FilterResult.REJECT) return FilterResult.REJECT;
            return other.test(entity);
        };
    }
}
