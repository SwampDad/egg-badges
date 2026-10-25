package com.cobbletown.eggbadges;

import java.util.Objects;

/** Parsed egg fields. Ability is optional; hidden status is resolved separately off-render. */
public record EggAttributes(String species, String form, String nature, String gender, int[] ivs,
                            String ability, HiddenAbilityStatus hiddenAbility) {
    public EggAttributes {
        species = species == null ? "" : species;
        form = form == null ? "" : form;
        nature = nature == null ? "" : nature;
        gender = gender == null ? "" : gender;
        ability = ability == null ? "" : ability;
        hiddenAbility = hiddenAbility == null ? HiddenAbilityStatus.UNRESOLVED : hiddenAbility;
    }

    /** Compatibility constructor for callers that do not read ability data. */
    public EggAttributes(String species, String form, String nature, String gender, int[] ivs) {
        this(species, form, nature, gender, ivs, "", HiddenAbilityStatus.UNRESOLVED);
    }

    public EggAttributes withHiddenAbility(HiddenAbilityStatus status) {
        return new EggAttributes(species, form, nature, gender, ivs, ability,
                Objects.requireNonNullElse(status, HiddenAbilityStatus.UNRESOLVED));
    }
}
