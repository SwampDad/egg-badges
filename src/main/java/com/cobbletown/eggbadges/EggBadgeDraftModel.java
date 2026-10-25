package com.cobbletown.eggbadges;

/** Screen-local transactional configuration. No method mutates INSTANCE. */
final class EggBadgeDraftModel {
    ChannelConfig background;
    ChannelConfig icon;
    GenderConfig gender;
    boolean showPerfectIvCount;
    boolean showHiddenAbility;

    EggBadgeDraftModel(EggBadgesConfig source) {
        background = source.background.copy();
        icon = source.icon.copy();
        gender = source.gender.copy();
        showPerfectIvCount = source.show_perfect_iv_count;
        showHiddenAbility = source.show_hidden_ability;
    }

    void reset() {
        background = ChannelConfig.backgroundDefault();
        icon = ChannelConfig.iconDefault();
        gender = new GenderConfig();
        showPerfectIvCount = false;
        showHiddenAbility = false;
    }

    void commit(EggBadgesConfig target) {
        target.background = background.copy();
        target.icon = icon.copy();
        target.gender = gender.copy();
        target.show_perfect_iv_count = showPerfectIvCount;
        target.show_hidden_ability = showHiddenAbility;
    }

    boolean differsFrom(EggBadgesConfig target) {
        return target == null || !background.sameAs(target.background) || !icon.sameAs(target.icon)
                || !gender.sameAs(target.gender) || showPerfectIvCount != target.show_perfect_iv_count
                || showHiddenAbility != target.show_hidden_ability;
    }
}
