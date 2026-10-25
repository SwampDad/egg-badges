package com.cobbletown.eggbadges;

/**
 * Lazily loads the Cobblemon-backed lookup so Egg Badges still loads when Cobblemon is
 * absent. Missing or broken Cobblemon support fails closed as UNRESOLVED.
 */
final class HiddenAbilityLookupProvider {
    private static boolean attempted;
    private static HiddenAbilityResolver.Lookup lookup;

    private HiddenAbilityLookupProvider() {}

    static HiddenAbilityResolver.Lookup get() {
        if (!attempted) {
            attempted = true;
            try {
                // Prove the Cobblemon API is actually available before loading our adapter.
                Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies", false,
                        HiddenAbilityLookupProvider.class.getClassLoader());
                Class<?> type = Class.forName("com.cobbletown.eggbadges.CobblemonHiddenAbilityLookup");
                lookup = (HiddenAbilityResolver.Lookup) type.getDeclaredConstructor().newInstance();
            } catch (Throwable unavailable) {
                lookup = null;
            }
        }
        return lookup;
    }
}
