package com.cobbletown.eggbadges;

import java.util.Arrays;

/**
 * Mutable, screen-local draft of the five custom rules.  It deliberately contains no
 * widgets: switching a rule can therefore never write stale text-field state into the
 * newly selected rule.
 */
final class RuleEditorModel {
    private final BadgeRule[] rules = new BadgeRule[5];
    private int selected;

    RuleEditorModel(BadgeRule[] source) {
        for (int i = 0; i < rules.length; i++) {
            rules[i] = source != null && i < source.length && source[i] != null
                    ? source[i].copy() : new BadgeRule();
        }
    }

    BadgeRule selectedRule() { return rules[selected]; }
    BadgeRule[] workingRules() { return rules; }
    int selected() { return selected; }
    void select(int index) { selected = Math.max(0, Math.min(rules.length - 1, index)); }
    BadgeRule[] committedRules() { return Arrays.stream(rules).map(BadgeRule::copy).toArray(BadgeRule[]::new); }

    void resetSelected() { rules[selected] = BadgeRule.defaults()[selected]; }

    void applyPreset(String preset) {
        BadgeRule rule = selectedRule();
        Arrays.fill(rule.ivMinimums, -1);
        Arrays.fill(rule.values, "");
        Arrays.fill(rule.operators, ">=");
        rule.remainderMin = -1;
        rule.perfectCountMode = "none";
        rule.perfectCount = 0;
        rule.preset = "custom";
        switch (preset) {
            case "perfect" -> {
                rule.perfectCountMode = "exactly";
                rule.perfectCount = 6;
                Arrays.fill(rule.ivMinimums, 31);
                Arrays.fill(rule.operators, "=");
            }
            case "atleast1" -> { rule.perfectCountMode = "atleast"; rule.perfectCount = 1; }
            case "atleast3" -> { rule.perfectCountMode = "atleast"; rule.perfectCount = 3; }
            case "nearperfect" -> { rule.perfectCountMode = "exactly"; rule.perfectCount = 5; rule.remainderMin = 29; }
            case "clear" -> { }
            default -> throw new IllegalArgumentException("Unknown preset: " + preset);
        }
    }
}
