package com.cobbletown.eggbadges;

import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HiddenAbilityResolverTest {
    private static final HiddenAbilityResolver.Lookup BULBASAUR = (species, form, ability) -> {
        if (!"bulbasaur".equalsIgnoreCase(species) || !"normal".equalsIgnoreCase(form)) {
            return HiddenAbilityStatus.UNRESOLVED;
        }
        if ("chlorophyll".equalsIgnoreCase(ability)) return HiddenAbilityStatus.HIDDEN;
        if ("overgrow".equalsIgnoreCase(ability)) return HiddenAbilityStatus.NORMAL;
        return HiddenAbilityStatus.UNRESOLVED;
    };

    private static EggAttributes egg(String ability) {
        return new EggAttributes("Bulbasaur", "normal", "Gentle", "Male",
                new int[]{31, 31, 31, 31, 31, 30}, ability, HiddenAbilityStatus.UNRESOLVED);
    }

    @Test void pairedBulbasaurAbilitiesResolveToHiddenAndNormal() {
        EggAttributes hidden = HiddenAbilityResolver.resolve(egg("Chlorophyll"), BULBASAUR);
        EggAttributes normal = HiddenAbilityResolver.resolve(egg("Overgrow"), BULBASAUR);
        assertEquals(HiddenAbilityStatus.HIDDEN, hidden.hiddenAbility());
        assertEquals(HiddenAbilityStatus.NORMAL, normal.hiddenAbility());
    }

    @Test void canonicalAbilityKeyMatchesTranslatedLoreAndRegistryIdentifiers() {
        assertEquals("roughskin", CobblemonHiddenAbilityLookup.abilityKey("Rough Skin"));
        assertEquals("roughskin", CobblemonHiddenAbilityLookup.abilityKey("cobblemon:rough_skin"));
        assertEquals("roughskin", CobblemonHiddenAbilityLookup.abilityKey("roughskin"));
    }

    @Test void unknownSpeciesFormOrAbilityFailsClosedAsUnresolved() {
        assertEquals(HiddenAbilityStatus.UNRESOLVED,
                HiddenAbilityResolver.resolve(new EggAttributes("Missingno", "normal", "Gentle", "",
                        new int[]{31, 31, 31, 31, 31, 31}, "overgrow", HiddenAbilityStatus.UNRESOLVED),
                        BULBASAUR).hiddenAbility());
        assertEquals(HiddenAbilityStatus.UNRESOLVED,
                HiddenAbilityResolver.resolve(new EggAttributes("Bulbasaur", "missing-form", "Gentle", "",
                        new int[]{31, 31, 31, 31, 31, 31}, "overgrow", HiddenAbilityStatus.UNRESOLVED),
                        BULBASAUR).hiddenAbility());
        assertEquals(HiddenAbilityStatus.UNRESOLVED,
                HiddenAbilityResolver.resolve(egg("Unknown Ability"), BULBASAUR).hiddenAbility());
        assertEquals(HiddenAbilityStatus.UNRESOLVED,
                HiddenAbilityResolver.resolve(egg(""), BULBASAUR).hiddenAbility());
    }

    @Test void missingFormUsesTheLookupStandardFormPath() {
        EggAttributes formOmitted = new EggAttributes("Bulbasaur", "", "Gentle", "Male",
                new int[]{31, 31, 31, 31, 31, 30}, "Chlorophyll", HiddenAbilityStatus.UNRESOLVED);
        HiddenAbilityResolver.Lookup standardFormLookup = (species, form, ability) ->
                "Bulbasaur".equalsIgnoreCase(species) && form.isBlank()
                        && "Chlorophyll".equalsIgnoreCase(ability)
                        ? HiddenAbilityStatus.HIDDEN : HiddenAbilityStatus.UNRESOLVED;
        assertEquals(HiddenAbilityStatus.HIDDEN,
                HiddenAbilityResolver.resolve(formOmitted, standardFormLookup).hiddenAbility());
    }

    @Test void resolverPreservesParsedFields() {
        EggAttributes source = egg("Chlorophyll");
        EggAttributes resolved = HiddenAbilityResolver.resolve(source, BULBASAUR);
        assertEquals(source.species(), resolved.species());
        assertEquals(source.form(), resolved.form());
        assertEquals(source.nature(), resolved.nature());
        assertEquals(source.gender(), resolved.gender());
        assertArrayEquals(source.ivs(), resolved.ivs());
        assertEquals(source.ability(), resolved.ability());
    }

    @Test void allNewLoreParsingReadsAbilitySpeciesAndForm() {
        EggClassifier.ParsedLore parsed = EggClassifier.parseAllLore(List.of(
                Text.literal("Species: Bulbasaur"),
                Text.literal("Form: Normal"),
                Text.literal("Nature: Gentle"),
                Text.literal("Ability: Chlorophyll"),
                Text.literal("Gender: M"),
                Text.literal("HP: 31"),
                Text.literal("Attack: 31"),
                Text.literal("Defence: 31"),
                Text.literal("Sp.Attack: 31"),
                Text.literal("Sp.Defence: 31"),
                Text.literal("Speed: 30")));

        EggAttributes attributes = parsed.attributes();
        assertEquals("Bulbasaur", attributes.species());
        assertEquals("Normal", attributes.form());
        assertEquals("Gentle", attributes.nature());
        assertEquals("Chlorophyll", attributes.ability());
        assertEquals("Male", attributes.gender());
        assertEquals(HiddenAbilityStatus.UNRESOLVED, attributes.hiddenAbility());
        assertArrayEquals(new int[]{31, 31, 31, 31, 31, 30}, attributes.ivs());
    }

    @Test void missingOrBrokenLookupFailsClosedWithoutThrowing() {
        assertEquals(HiddenAbilityStatus.UNRESOLVED,
                HiddenAbilityResolver.resolve(egg("Chlorophyll"), null).hiddenAbility());
        assertEquals(HiddenAbilityStatus.UNRESOLVED,
                HiddenAbilityResolver.resolve(egg("Chlorophyll"),
                        (species, form, ability) -> { throw new IllegalStateException("broken registry"); })
                        .hiddenAbility());
    }

}
