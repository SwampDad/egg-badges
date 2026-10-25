package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RuleEditorModelTest {
    @Test void presetSurvivesSelectionRoundTrip() {
        RuleEditorModel model = new RuleEditorModel(BadgeRule.defaults());
        model.select(3);
        model.applyPreset("nearperfect");
        model.select(1);
        model.select(3);
        BadgeRule rule = model.selectedRule();
        assertEquals("exactly", rule.perfectCountMode);
        assertEquals(5, rule.perfectCount);
        assertEquals(29, rule.remainderMin);
    }

    @Test void resetRestoresTheEntireDefaultRule() {
        RuleEditorModel model = new RuleEditorModel(BadgeRule.defaults());
        model.select(2);
        model.applyPreset("clear");
        model.resetSelected();
        BadgeRule rule = model.selectedRule();
        assertEquals("exactly", rule.perfectCountMode);
        assertEquals(6, rule.perfectCount);
        assertArrayEquals(new int[]{31, 31, 31, 31, 31, 31}, rule.ivMinimums);
    }
}
