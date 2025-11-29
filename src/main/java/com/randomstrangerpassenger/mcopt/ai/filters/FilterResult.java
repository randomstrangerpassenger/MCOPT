package com.randomstrangerpassenger.mcopt.ai.filters;

/**
 * Result enum for entity filtering operations.
 *
 * Used by the filtering system to determine how to proceed after a filter check.
 */
public enum FilterResult {
    /**
     * Filter did nothing - continue to next filter in chain.
     */
    PASS,

    /**
     * Entity matched this filter - skip remaining filters and process modifiers.
     */
    MATCH,

    /**
     * Entity was rejected - skip all processing for this entity.
     */
    REJECT
}
