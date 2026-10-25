package com.cobbletown.eggbadges;

/** Result of resolving an egg's ability against its species/form ability pool. */
public enum HiddenAbilityStatus {
    /** The species, form, or ability could not be resolved safely. */
    UNRESOLVED,
    /** The ability is a normal (non-hidden) slot. */
    NORMAL,
    /** The ability is the form's hidden ability slot. */
    HIDDEN
}
