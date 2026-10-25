package com.cobbletown.eggbadges;

import com.cobblemon.mod.common.api.abilities.AbilityPool;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Production lookup against Cobblemon's client species/form registry. It runs only from
 * tick-time classification, never from rendering.
 */
final class CobblemonHiddenAbilityLookup implements HiddenAbilityResolver.Lookup {
    private static final int CACHE_LIMIT = 128;
    /** Registry results are immutable for a running client; bound this so server lore cannot grow it unbounded. */
    private final Map<LookupKey, HiddenAbilityStatus> cache = new LinkedHashMap<>(CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<LookupKey, HiddenAbilityStatus> eldest) {
            return size() > CACHE_LIMIT;
        }
    };

    @Override
    public HiddenAbilityStatus resolve(String speciesName, String formName, String abilityName) {
        String speciesKey = normalize(speciesName);
        String formKey = normalize(formName);
        String abilityKey = abilityKey(abilityName);
        if (speciesKey.isEmpty() || abilityKey.isEmpty()) return HiddenAbilityStatus.UNRESOLVED;
        LookupKey key = new LookupKey(speciesKey, formKey, abilityKey);
        HiddenAbilityStatus cached = cache.get(key);
        if (cached != null) return cached;
        HiddenAbilityStatus result = resolveUncached(speciesName, formName, speciesKey, formKey, abilityKey);
        cache.put(key, result);
        return result;
    }

    private static HiddenAbilityStatus resolveUncached(String speciesName, String formName,
                                                        String speciesKey, String formKey, String abilityKey) {
        Species species = findSpecies(speciesName, speciesKey);
        if (species == null) return HiddenAbilityStatus.UNRESOLVED;

        FormData form = findForm(species, formName, formKey);
        if (form == null) return HiddenAbilityStatus.UNRESOLVED;

        AbilityPool pool = form.getAbilities();
        if (pool == null) return HiddenAbilityStatus.UNRESOLVED;
        for (PotentialAbility potential : pool) {
            if (potential == null || potential.getTemplate() == null) continue;
            if (abilityKey(potential.getTemplate().getName()).equals(abilityKey)) {
                return potential.getType() == HiddenAbilityType.INSTANCE
                        ? HiddenAbilityStatus.HIDDEN
                        : HiddenAbilityStatus.NORMAL;
            }
        }
        return HiddenAbilityStatus.UNRESOLVED;
    }

    private static Species findSpecies(String original, String normalized) {
        Species species = PokemonSpecies.getByName(normalized);
        if (species != null) return species;
        Identifier identifier = Identifier.tryParse(original.trim());
        if (identifier == null) identifier = Identifier.tryParse("cobblemon:" + normalized);
        return identifier == null ? null : PokemonSpecies.getByIdentifier(identifier);
    }

    private static FormData findForm(Species species, String original, String normalized) {
        if (normalized.isEmpty() || "normal".equals(normalized)) return species.getStandardForm();
        FormData form = species.getFormByShowdownId(normalized);
        if (form != null) return form;
        form = species.getFormByName(original == null ? "" : original.trim());
        if (form != null) return form;
        for (FormData candidate : species.getForms()) {
            if (candidate != null && normalize(candidate.getName()).equals(normalized)) return candidate;
        }
        return null;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        int separator = normalized.indexOf(':');
        if (separator >= 0 && separator + 1 < normalized.length()) normalized = normalized.substring(separator + 1);
        return normalized;
    }

    /** Matches translated lore such as "Rough Skin" to registry identifiers such as "roughskin". */
    static String abilityKey(String value) {
        return normalize(value).replaceAll("[^a-z0-9]", "");
    }

    private record LookupKey(String species, String form, String ability) {}
}
