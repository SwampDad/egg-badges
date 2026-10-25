package com.cobbletown.eggbadges;

import net.minecraft.text.Text;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EggClassifierTest {
    @Test void shippedDefaultsHaveCorrectStructure() {
        BadgeRule[] rules = BadgeRule.defaults();
        assertFalse(rules[0].enabled); assertFalse(rules[0].isIvClassification);
        assertFalse(rules[1].enabled); assertFalse(rules[1].isIvClassification);
        assertTrue(rules[2].enabled);  assertTrue(rules[2].isIvClassification);
        assertTrue(rules[3].enabled);  assertTrue(rules[3].isIvClassification);
        assertFalse(rules[4].enabled); assertTrue(rules[4].isIvClassification);
        assertEquals("\u25CF", rules[2].glyph);
        assertEquals(0xFF55DD77, rules[2].color);
        assertEquals(0xFF5599FF, rules[0].color);
        assertEquals(0xFFFF5555, rules[1].color);
    }

    @Test void legacyDefaultGenderColorsMigrateWithoutChangingCustomColors() {
        BadgeRule[] legacy = BadgeRule.defaults();
        legacy[0].color = 0xFFFF5555; legacy[1].color = 0xFF5599FF;
        assertTrue(EggBadgesConfig.migrateLegacyGenderColors(legacy));
        assertEquals(0xFF5599FF, legacy[0].color); assertEquals(0xFFFF5555, legacy[1].color);
        legacy[0].color = 0xFF123456;
        assertFalse(EggBadgesConfig.migrateLegacyGenderColors(legacy));
        assertEquals(0xFF123456, legacy[0].color);
    }

    @Test void ruleFingerprintChangesWhenAnOptionChanges() {
        BadgeRule[] rules = BadgeRule.defaults();
        int before = EggBadgeRenderer.Cache.rulesHash(rules);
        rules[2].enabled = false;
        assertNotEquals(before, EggBadgeRenderer.Cache.rulesHash(rules));
    }

    @Test void savedSpeciesFilterChangesTheCacheFingerprint() {
        BadgeRule[] rules=BadgeRule.defaults();
        int before=EggBadgeRenderer.Cache.configHash(rules,new AutoIndicatorConfig());
        rules[4].enabled=true;rules[4].species="lapras";
        assertNotEquals(before,EggBadgeRenderer.Cache.configHash(rules,new AutoIndicatorConfig()));
    }

    @Test void formattingStripperIsDeterministicWithoutRegex() {
        assertEquals("HP: 31", EggClassifier.stripFormatting("\u00a7aHP: \u00a7f31"));
    }

    @Test void compactGenderValuesNormalizeToBadgeValues() {
        assertEquals("Male", EggClassifier.normalizeGender("M"));
        assertEquals("Female", EggClassifier.normalizeGender("F"));
    }

    private static EggAttributes attrs(int... ivs) { return new EggAttributes("Eevee", "", "Jolly", "Male", ivs); }

    // --- Perfect count (new model) ---

    @Test void exactlySixPerfectIVs() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 6;
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,30,31), r));
    }

    @Test void exactlyFivePerfectIVs() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 5;
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,30), r));
        assertTrue(EggClassifier.matches(attrs(30,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,30,30,31), r));
    }

    @Test void atLeastThreePerfectIVs() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "atleast"; r.perfectCount = 3;
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(31,31,0,0,0,0), r));
    }

    @Test void atLeastOnePerfectIV() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "atleast"; r.perfectCount = 1;
        assertTrue(EggClassifier.matches(attrs(31,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(0,0,0,0,0,0), r));
    }

    // --- Remainder threshold ---

    @Test void fivePerfectWithRemainderThreshold() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 5; r.remainderMin = 29;
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,30), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,29), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,28), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,30,30,31), r));
    }

    @Test void nearPerfectSupportsAConfigurableMinimumRemainingIv() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 5; r.remainderMin = 27;
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,27), r));
        assertTrue(EggClassifier.matches(attrs(27,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,26), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,30,29), r));
    }

    @Test void remainderThresholdAppliesToAnyPosition() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 5; r.remainderMin = 30;
        assertTrue(EggClassifier.matches(attrs(30,31,31,31,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,30,31,31,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,30,31,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,30,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,30,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,30), r));
        assertFalse(EggClassifier.matches(attrs(29,31,31,31,31,31), r));
    }

    @Test void remainderFailsClosedWhenCountModeNotExactly() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "atleast"; r.perfectCount = 5; r.remainderMin = 29;
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,30), r),
            "atleast + remainder should fail closed");
    }

    @Test void remainderFailsClosedWhenZeroRemainder() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 6; r.remainderMin = 29;
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,31), r),
            "exactly 6 + remainder should fail closed");
    }

    // --- Specific stat requirements (new model with operators) ---

    @Test void physicalAttackerWithDefaultGte() {
        BadgeRule r = new BadgeRule(); r.enabled = true;
        r.ivMinimums = new int[]{31,31,31,-1,31,31};
        r.operators = new String[]{">=",">=",">=",">=",">=",">="};
        assertTrue(EggClassifier.matches(attrs(31,31,31,0,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,17,31,31), r));
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,30,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(30,31,31,31,31,31), r));
    }

    @Test void equalsOperatorOnStat() {
        BadgeRule r = new BadgeRule(); r.enabled = true;
        r.ivMinimums = new int[]{30,-1,-1,-1,-1,-1};
        r.operators = new String[]{"=",">=",">=",">=",">=",">="};
        assertTrue(EggClassifier.matches(attrs(30,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(31,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(29,0,0,0,0,0), r));
    }

    @Test void lessThanOperatorOnStat() {
        BadgeRule r = new BadgeRule(); r.enabled = true;
        r.ivMinimums = new int[]{20,-1,-1,-1,-1,-1};
        r.operators = new String[]{"<",">=",">=",">=",">=",">="};
        assertTrue(EggClassifier.matches(attrs(19,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(20,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(21,0,0,0,0,0), r));
    }

    @Test void blankStatFieldsAreUnconstrained() {
        BadgeRule r = new BadgeRule(); r.enabled = true;
        r.ivMinimums = new int[]{-1,-1,-1,-1,-1,-1};
        assertTrue(EggClassifier.matches(attrs(0,0,0,0,0,0), r));
    }

    // --- Combination ---

    @Test void countAndSpecificStatsAndTogether() {
        BadgeRule r = new BadgeRule(); r.enabled = true;
        r.perfectCountMode = "exactly"; r.perfectCount = 5;
        r.ivMinimums = new int[]{-1,-1,-1,-1,-1,31};
        r.operators = new String[]{">=",">=",">=",">=",">=",">="};
        assertTrue(EggClassifier.matches(attrs(31,31,31,0,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,0,30,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,30), r));
    }

    // --- Precedence ---

    @Test void earlierIvBadgeWinsOverLater() {
        BadgeRule a = new BadgeRule(); a.enabled = true; a.isIvClassification = true;
        a.ivMinimums = new int[]{31,31,31,-1,31,31};
        a.operators = new String[]{">=",">=",">=",">=",">=",">="};
        BadgeRule b = new BadgeRule(); b.enabled = true; b.isIvClassification = true;
        b.perfectCountMode = "exactly"; b.perfectCount = 6;
        BadgeRule[] rules = new BadgeRule[]{a, b};
        int mask = 0;
        for (int i = 0; i < rules.length; i++) if (EggClassifier.matches(attrs(31,31,31,31,31,31), rules[i])) mask |= 1 << i;
        int display = EggBadgeRenderer.applyPrecedence(mask, rules);
        assertEquals(1, display, "Earlier badge (index 0) should win");
    }

    @Test void laterIvBadgeWinsWhenEarlierDoesNotMatch() {
        BadgeRule a = new BadgeRule(); a.enabled = true; a.isIvClassification = true;
        a.ivMinimums = new int[]{31,31,31,-1,31,31};
        a.operators = new String[]{">=",">=",">=",">=",">=",">="};
        BadgeRule b = new BadgeRule(); b.enabled = true; b.isIvClassification = true;
        b.ivMinimums = new int[]{31,31,-1,-1,-1,-1};
        b.operators = new String[]{">=",">=",">=",">=",">=",">="};
        BadgeRule[] rules = new BadgeRule[]{a, b};
        int mask = 0;
        for (int i = 0; i < rules.length; i++) if (EggClassifier.matches(attrs(31,31,0,0,0,0), rules[i])) mask |= 1 << i;
        int display = EggBadgeRenderer.applyPrecedence(mask, rules);
        assertEquals(1 << 1, display, "Later badge should show when earlier doesn't match");
    }

    @Test void qualityBadgeCoexistsWithInformationalBadge() {
        BadgeRule quality = new BadgeRule(); quality.enabled = true; quality.isIvClassification = true;
        quality.perfectCountMode = "exactly"; quality.perfectCount = 6;
        BadgeRule info = new BadgeRule(); info.enabled = true; info.isIvClassification = false;
        info.gender = "Male";
        BadgeRule[] rules = new BadgeRule[]{quality, info};
        int mask = 0;
        for (int i = 0; i < rules.length; i++) if (EggClassifier.matches(attrs(31,31,31,31,31,31), rules[i])) mask |= 1 << i;
        int display = EggBadgeRenderer.applyPrecedence(mask, rules);
        assertEquals((1 << 0) | (1 << 1), display, "Both quality and informational should show");
    }

    @Test void speciesOnlyCustomBadgeCoexistsWithFivePerfectIvBadge() {
        BadgeRule[] rules=BadgeRule.defaults();
        BadgeRule dreepy=rules[4];
        dreepy.enabled=true;dreepy.species="Dreepy";
        int mask=0;
        EggAttributes dreepyFivePerfect=new EggAttributes("Dreepy","Normal","Timid","Male",new int[]{31,13,31,31,31,31});
        for(int i=0;i<rules.length;i++)if(EggClassifier.matches(dreepyFivePerfect,rules[i]))mask|=1<<i;
        assertEquals((1<<3)|(1<<4),EggBadgeRenderer.applyPrecedence(mask,rules));
        assertFalse(EggBadgeRenderer.isQualityRule(dreepy));
        assertTrue(EggBadgeRenderer.isQualityRule(rules[3]));
    }

    @Test void disabledIvRuleDoesNotSuppressLaterMatchingRule() {
        BadgeRule a = new BadgeRule(); a.enabled = false; a.isIvClassification = true;
        a.perfectCountMode = "exactly"; a.perfectCount = 6;
        BadgeRule b = new BadgeRule(); b.enabled = true; b.isIvClassification = true;
        b.ivMinimums = new int[]{31,31,31,-1,31,31};
        b.operators = new String[]{">=",">=",">=",">=",">=",">="};
        BadgeRule[] rules = new BadgeRule[]{a, b};
        int mask = 0;
        for (int i = 0; i < rules.length; i++) if (EggClassifier.matches(attrs(31,31,31,0,31,31), rules[i])) mask |= 1 << i;
        int display = EggBadgeRenderer.applyPrecedence(mask, rules);
        assertEquals(1 << 1, display, "Disabled earlier rule should not suppress later matching rule");
    }

    // --- Legacy matching (backward compat) ---

    @Test void legacyPerfectPresetStillWorks() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "perfect";
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,30,31), r));
    }

    @Test void legacyFivePresetStillWorks() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "five";
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,30), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
    }

    @Test void legacyValuesWithEqualsOperator() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"30","","","","",""};
        r.operators = new String[]{"=","=","=","=","=","="};
        assertTrue(EggClassifier.matches(attrs(30,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(31,0,0,0,0,0), r));
    }

    @Test void legacyValuesWithLtOperator() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"20","","","","",""};
        r.operators = new String[]{"<","=","=","=","=","="};
        assertTrue(EggClassifier.matches(attrs(19,0,0,0,0,0), r));
        assertFalse(EggClassifier.matches(attrs(20,0,0,0,0,0), r));
    }

    @Test void legacyCombinedPresetAndValues() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "perfect";
        r.values = new String[]{"31","31","31","31","31","31"};
        r.operators = new String[]{"=","=","=","=","=","="};
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,30,31), r));
    }

    @Test void hasLegacyIvOpsDetectsValues() {
        BadgeRule r = new BadgeRule(); r.values = new String[]{"30","","","","",""};
        assertTrue(EggClassifier.hasLegacyIvOps(r));
    }

    @Test void hasLegacyIvOpsDetectsKnownPresets() {
        BadgeRule r = new BadgeRule(); r.preset = "perfect";
        assertTrue(EggClassifier.hasLegacyIvOps(r));
        r.preset = "five"; assertTrue(EggClassifier.hasLegacyIvOps(r));
        r.preset = "custom"; assertFalse(EggClassifier.hasLegacyIvOps(r));
    }

    @Test void countPerfectAtLeastCountsCorrectly() {
        assertEquals(0, EggClassifier.countPerfectAtLeast(new int[]{0,0,0,0,0,0}, 31));
        assertEquals(1, EggClassifier.countPerfectAtLeast(new int[]{31,0,0,0,0,0}, 31));
        assertEquals(6, EggClassifier.countPerfectAtLeast(new int[]{31,31,31,31,31,31}, 31));
    }

    @Test void unknownIvFailsClosed() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.perfectCountMode = "exactly"; r.perfectCount = 6;
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,31,-1}), r));
    }

    @Test void disabledRuleNeverMatches() {
        BadgeRule r = new BadgeRule(); r.enabled = false; r.perfectCountMode = "exactly"; r.perfectCount = 1;
        assertFalse(EggClassifier.matches(attrs(31,0,0,0,0,0), r));
    }

    @Test void genderFilterAndSpeciesFilter() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.gender = "Male"; r.species = "eevee";
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Pikachu","","Jolly","Male",new int[]{31,31,31,31,31,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Female",new int[]{31,31,31,31,31,31}), r));
    }

    @Test void laprasSpeciesFilterIsCaseInsensitiveAndDoesNotMatchOtherEggs() {
        BadgeRule r=new BadgeRule();r.enabled=true;r.species="lapras";
        assertTrue(EggClassifier.matches(new EggAttributes("Lapras","","Calm","",new int[]{-1,-1,-1,-1,-1,-1}),r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Calm","",new int[]{-1,-1,-1,-1,-1,-1}),r));
    }

    @Test void filterTextIsTrimmedAndStillRequiresAnExactAttributeMatch() {
        BadgeRule r=new BadgeRule();r.enabled=true;r.species="  lapras  ";r.form="  galarian ";r.nature=" calm  ";
        assertTrue(EggClassifier.matches(new EggAttributes(" Lapras ","Galarian","Calm","",new int[]{-1,-1,-1,-1,-1,-1}),r));
        assertFalse(EggClassifier.matches(new EggAttributes("Lapras","Alolan","Calm","",new int[]{-1,-1,-1,-1,-1,-1}),r));
    }

    @Test void speciesFormAndNatureAreAllRequiredWhenSpecified() {
        BadgeRule r=new BadgeRule();r.enabled=true;r.species="lapras";r.form="galarian";r.nature="calm";
        assertTrue(EggClassifier.matches(new EggAttributes("Lapras","Galarian","Calm","",new int[]{-1,-1,-1,-1,-1,-1}),r));
        assertFalse(EggClassifier.matches(new EggAttributes("Lapras","","Calm","",new int[]{-1,-1,-1,-1,-1,-1}),r));
        assertFalse(EggClassifier.matches(new EggAttributes("Lapras","Galarian","Jolly","",new int[]{-1,-1,-1,-1,-1,-1}),r));
        assertFalse(EggClassifier.matches(new EggAttributes("Lapras","Galarian","","",new int[]{-1,-1,-1,-1,-1,-1}),r));
    }

    @Test void emptyAttributeFieldsAreUnrestrictedButUnknownRequestedDataFailsClosed() {
        BadgeRule unrestricted=new BadgeRule();unrestricted.enabled=true;
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{-1,-1,-1,-1,-1,-1}),unrestricted));
        BadgeRule required=new BadgeRule();required.enabled=true;required.nature="Calm";
        assertFalse(EggClassifier.matches(new EggAttributes("Lapras","Galarian","","",new int[]{-1,-1,-1,-1,-1,-1}),required));
    }

    @Test void explicitlyLabelledLoreFieldsAreParsedForRequestedFiltersOnly() {
        BadgeRule r=new BadgeRule();r.enabled=true;r.species="lapras";r.form="galarian";r.nature="calm";
        EggAttributes parsed=EggClassifier.parseLore(List.of(Text.literal("Species: Lapras"),Text.literal("Form: Galarian"),Text.literal("Nature: Calm")),new BadgeRule[]{r},null).attributes();
        assertTrue(EggClassifier.matches(parsed,r));
        EggAttributes incomplete=EggClassifier.parseLore(List.of(Text.literal("Species: Lapras")),new BadgeRule[]{r},null).attributes();
        assertFalse(EggClassifier.matches(incomplete,r));
    }

    @Test void labelledEggLoreIdentifiesDisguisedServerEggsWithoutGuessing() {
        assertTrue(EggClassifier.hasExplicitEggLore(List.of(Text.literal("Species: Rowlet"),Text.literal("HP: 31"))));
        assertFalse(EggClassifier.hasExplicitEggLore(List.of(Text.literal("Species: Rowlet"))));
        assertFalse(EggClassifier.hasExplicitEggLore(List.of(Text.literal("HP: 31"))));
    }
}
