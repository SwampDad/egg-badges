package com.cobbletown.eggbadges;

/** Pure hidden-ability resolution seam; production lookup is supplied by the client. */
final class HiddenAbilityResolver {
    interface Lookup {
        HiddenAbilityStatus resolve(String species, String form, String ability);
    }

    private HiddenAbilityResolver() {}

    static EggAttributes resolve(EggAttributes attributes, Lookup lookup) {
        if (attributes == null) return null;
        // A Form line is optional in server egg lore. The Cobblemon lookup deliberately
        // interprets an absent form as the species' standard form.
        if (lookup == null || blank(attributes.species()) || blank(attributes.ability())) {
            return attributes.withHiddenAbility(HiddenAbilityStatus.UNRESOLVED);
        }
        try {
            HiddenAbilityStatus status = lookup.resolve(attributes.species(), attributes.form(), attributes.ability());
            return attributes.withHiddenAbility(status == null ? HiddenAbilityStatus.UNRESOLVED : status);
        } catch (Throwable unavailableOrBroken) {
            return attributes.withHiddenAbility(HiddenAbilityStatus.UNRESOLVED);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
