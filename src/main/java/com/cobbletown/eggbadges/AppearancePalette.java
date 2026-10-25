package com.cobbletown.eggbadges;

/** Fixed palettes for the simplified Background and Circle controls. */
final class AppearancePalette {
    /** Dark slot-fill colors: Forest Green, Slate Blue, and Deep Purple. */
    static final int BACKGROUND_GREEN = 0xFF2F6B3A;
    static final int BACKGROUND_BLUE = 0xFF3E557A;
    static final int BACKGROUND_PURPLE = 0xFF45245F;
    static final int[] BACKGROUNDS = {BACKGROUND_GREEN, BACKGROUND_BLUE, BACKGROUND_PURPLE};

    /** Bright icon colors: Gold, Cyan, and White. Never shared with Background. */
    static final int CIRCLE_GOLD = 0xFFFFD54A;
    static final int CIRCLE_CYAN = 0xFF55E6FF;
    static final int CIRCLE_WHITE = 0xFFFFFFFF;
    static final int[] CIRCLES = {CIRCLE_GOLD, CIRCLE_CYAN, CIRCLE_WHITE};

    private AppearancePalette() {}

    static boolean supportedBackground(int color) {
        return color == BACKGROUND_GREEN || color == BACKGROUND_BLUE || color == BACKGROUND_PURPLE
                || color == 0xFF55DD77 || color == 0xFF3F6FA8 || color == 0xFFB388FF
                || color == 0xFF5599FF || color == 0xFF55AAFF;
    }

    static boolean supportedCircle(int color) {
        return color == CIRCLE_GOLD || color == CIRCLE_CYAN || color == CIRCLE_WHITE
                || color == 0xFF55AAFF || color == 0xFF5599FF || color == 0xFFB388FF;
    }

    static int canonicalBackground(int color) {
        if (color == 0xFF55DD77) return BACKGROUND_GREEN;
        if (color == 0xFF3F6FA8 || color == 0xFF5599FF || color == 0xFF55AAFF) return BACKGROUND_BLUE;
        if (color == 0xFFB388FF) return BACKGROUND_PURPLE;
        return color;
    }

    static int canonicalCircle(int color) {
        if (color == 0xFF55AAFF || color == 0xFF5599FF) return CIRCLE_CYAN;
        if (color == 0xFFB388FF) return CIRCLE_WHITE;
        return color;
    }

    static String name(int color) {
        if (color == BACKGROUND_GREEN) return "Forest Green";
        if (color == BACKGROUND_BLUE) return "Slate Blue";
        if (color == BACKGROUND_PURPLE) return "Deep Purple";
        if (color == CIRCLE_GOLD) return "Gold";
        if (color == CIRCLE_CYAN) return "Cyan";
        if (color == CIRCLE_WHITE) return "White";
        return "Color";
    }
}
