package com.cobbletown.eggbadges;

/** Pure packed-mask contract for automatic indicators; bits 0–4 remain custom rules. */
final class AutoIndicators {
    static final int GENDER_MALE = 1 << 5, GENDER_FEMALE = 1 << 6, GENDERLESS = 1 << 7;
    static final int COUNT_PRESENT = 1 << 8, COUNT_SHIFT = 9, COUNT_MASK = 7 << COUNT_SHIFT;
    private AutoIndicators() {}
    static boolean complete(int[] ivs) { if (ivs == null || ivs.length != 6) return false; for (int iv : ivs) if (iv < 0 || iv > 31) return false; return true; }
    static int mask(EggAttributes attributes, AutoIndicatorConfig config) {
        if (attributes == null || config == null) return 0;
        int mask = 0;
        mask |= switch (attributes.gender()) { case "Male" -> config.show_gender ? GENDER_MALE : 0; case "Female" -> config.show_gender ? GENDER_FEMALE : 0; case "Genderless" -> config.show_gender ? GENDERLESS : 0; default -> 0; };
        if (!complete(attributes.ivs())) return mask;
        int count = EggClassifier.countPerfectAtLeast(attributes.ivs(), 31);
        if (config.show_perfect_iv_count && count >= config.minimum_perfect_ivs) mask |= COUNT_PRESENT | (count << COUNT_SHIFT);
        return mask;
    }
    static int count(int mask) { return (mask & COUNT_MASK) >>> COUNT_SHIFT; }
}
