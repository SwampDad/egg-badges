package com.cobbletown.eggbadges;

/** User-facing wording is deliberately kept separate from persisted enum identifiers. */
final class SettingsLabels {
    private SettingsLabels() {}

    static String condition(BadgeCondition c) {
        if (c == null) return "Not configured";
        return switch (c) {
            case PERFECT_6 -> "Perfect (6×31)";
            case FIVE_5 -> "Five Perfect IVs (5×31)";
            case GENDER -> "Gender";
            case IV_COUNT -> "Perfect IV Count";
            case CUSTOM -> "Custom IV requirements";
        };
    }

    static String shape(IconShape s) {
        if (s == null) return "Circle";
        return switch (s) {
            case CIRCLE -> "Circle";
            case TRIANGLE -> "Triangle";
            case DIAMOND -> "Diamond";
            case STAR -> "Star";
        };
    }

    /** Palette values are exact; unlisted values get an explicit descriptive fallback. */
    static String color(int c) {
        return switch (c) {
            case 0xFF55DD77 -> "Green";
            case 0xFF5599FF, 0xFF55AAFF -> "Blue";
            case 0xFFFF5555 -> "Red";
            case 0xFFFFD54A -> "Yellow";
            case 0xFFB388FF -> "Purple";
            case 0xFFFFAA55 -> "Orange";
            case 0xFFE0E0E0 -> "White";
            case 0xFF9E9E9E -> "Gray";
            default -> "Custom color";
        };
    }

    static String genderStyle(GenderStyle style) {
        return style == null ? GenderStyle.CORNER.label : style.label;
    }

    static String statName(int index) {
        return switch (index) {
            case 0 -> "HP";
            case 1 -> "Attack";
            case 2 -> "Defense";
            case 3 -> "Sp. Attack";
            case 4 -> "Sp. Defense";
            default -> "Speed";
        };
    }

    static String stat(StatCondition condition, int index) {
        String name = statName(index);
        if (condition == null || condition.operator == null || condition.operator == StatCondition.Operator.ANY)
            return name + ": Any";
        return name + ": " + switch (condition.operator) {
            case EQ -> "exactly " + condition.value;
            case GT -> "above " + condition.value;
            case LT -> "below " + condition.value;
            default -> "Any";
        };
    }
}
