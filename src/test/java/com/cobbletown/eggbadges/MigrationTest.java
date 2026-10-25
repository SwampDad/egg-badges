package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MigrationTest {
    @Test void schema2PerfectPresetPreservedAsLegacy() {
        BadgeRule r = new BadgeRule(); r.preset = "perfect"; r.enabled = true;
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertEquals("perfect", r.preset);
        assertTrue(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,31,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,30,31}), r));
    }

    @Test void schema2FivePresetPreservedAsLegacy() {
        BadgeRule r = new BadgeRule(); r.preset = "five"; r.enabled = true;
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertEquals("five", r.preset);
        assertTrue(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,31,30}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,31,31}), r));
    }

    @Test void schema2MalePresetPreservedAsLegacy() {
        BadgeRule r = new BadgeRule(); r.preset = "male"; r.enabled = true;
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertEquals("male", r.preset);
        assertTrue(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,31,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Female",new int[]{31,31,31,31,31,31}), r));
    }

    @Test void schema2FemalePresetPreservedAsLegacy() {
        BadgeRule r = new BadgeRule(); r.preset = "female"; r.enabled = true;
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertEquals("female", r.preset);
        assertTrue(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Female",new int[]{31,31,31,31,31,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{31,31,31,31,31,31}), r));
    }

    @Test void schema2EqualsOperatorPreservedExactly() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"30","","","","",""};
        r.operators = new String[]{"=","=","=","=","=","="};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{30,0,0,0,0,0}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{31,0,0,0,0,0}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{29,0,0,0,0,0}), r));
    }

    @Test void schema2GteOperatorPreserved() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"","","","","","30"};
        r.operators = new String[]{"=","=","=","=",">=",">="};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{0,0,0,0,0,30}), r));
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{0,0,0,0,0,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{0,0,0,0,0,29}), r));
    }

    @Test void schema2GtOperatorPreserved() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"","","","","","20"};
        r.operators = new String[]{"=","=","=",">",">",">"};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{0,0,0,0,0,21}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{0,0,0,0,0,20}), r));
    }

    @Test void schema2LtOperatorPreserved() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"20","","","","",""};
        r.operators = new String[]{"<","=","=","=","=","="};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{19,0,0,0,0,0}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{20,0,0,0,0,0}), r));
    }

    @Test void schema2LteOperatorPreserved() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.values = new String[]{"20","","","","",""};
        r.operators = new String[]{"<=","=","=","=","=","="};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{20,0,0,0,0,0}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{21,0,0,0,0,0}), r));
    }

    @Test void alreadyMigratedSchema3NotDoubleMigrated() {
        BadgeRule r = new BadgeRule(); r.perfectCountMode = "exactly"; r.perfectCount = 6;
        r.ivMinimums = new int[]{31,31,31,31,31,31};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertEquals("exactly", r.perfectCountMode); assertEquals(6, r.perfectCount);
    }

    @Test void nullRulesArrayDoesNotThrow() {
        assertDoesNotThrow(() -> BadgeRule.migrateToSchema3(null));
    }

    @Test void combinedPresetAndValuesBothPreserved() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "perfect";
        r.values = new String[]{"31","31","31","31","31","31"};
        r.operators = new String[]{"=","=","=","=","=","="};
        BadgeRule.migrateToSchema3(new BadgeRule[]{r});
        assertEquals("perfect", r.preset);
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{31,31,31,31,31,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{31,31,31,31,30,31}), r));
    }

    @Test void roundTripNewModelPreservesOperators() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.preset = "custom";
        r.perfectCountMode = "exactly"; r.perfectCount = 5; r.remainderMin = 29;
        r.ivMinimums = new int[]{31,31,31,-1,31,31};
        r.operators = new String[]{"=",">=",">=",">=",">=",">="};
        BadgeRule copy = r.copy();
        assertTrue(EggClassifier.matches(new EggAttributes("","","","",new int[]{31,31,31,30,31,31}), copy));
        assertFalse(EggClassifier.matches(new EggAttributes("","","","",new int[]{30,31,31,30,31,31}), copy));
        assertEquals("=", copy.operators[0]); assertEquals(">=", copy.operators[1]);
    }

    @Test void genderDefaultsAreInformational() {
        BadgeRule[] rules = BadgeRule.defaults();
        assertFalse(rules[0].isIvClassification, "Male should be informational");
        assertFalse(rules[1].isIvClassification, "Female should be informational");
    }

    @Test void qualityDefaultsAreQuality() {
        BadgeRule[] rules = BadgeRule.defaults();
        assertTrue(rules[2].isIvClassification, "Perfect should be quality");
        assertTrue(rules[3].isIvClassification, "Five should be quality");
    }
}
