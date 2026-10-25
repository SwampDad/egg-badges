package com.cobbletown.eggbadges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;

/** Schema 16 persistence. Legacy fields are retained solely as Gson migration input. */
public final class EggBadgesConfig {
    public static final EggBadgesConfig INSTANCE = new EggBadgesConfig();
    static final Logger LOG = LoggerFactory.getLogger("EggBadges/DEBUG");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final int CURRENT_SCHEMA_VERSION = 16;
    static final String DEBUG_VERSION = "0.5.0-dev.10";

    public int schema_version = CURRENT_SCHEMA_VERSION;
    public boolean debug_diagnostics = false;
    public ChannelConfig background = ChannelConfig.backgroundDefault();
    public ChannelConfig icon = ChannelConfig.iconDefault();
    public GenderConfig gender = new GenderConfig();
    /** Independent perfect-IV-count indicator, separate from both channels. */
    public boolean show_perfect_iv_count = false;
    /** New in schema 11; defaults off for existing and new configurations. */
    public boolean show_hidden_ability = false;
    public Integer migrated_from_schema;

    // Gson migration input only; old configs can deserialize safely.
    public BadgeRule[] badges;
    public AutoIndicatorConfig automatic_indicators;
    public boolean performance_diagnostics, diagnostics, diagnosticsVerbose;

    private transient boolean readOnlyNewerSchema;

    private Path path() { return FabricLoader.getInstance().getConfigDir().resolve("egg-badges/config.json"); }

    public boolean debug() { return debug_diagnostics || Boolean.getBoolean("egg-badges.debug"); }

    static void debug(String message) { if (INSTANCE.debug()) LOG.info("[EggBadges/DEBUG] {}", message); }

    public void load() {
        try {
            if (!Files.exists(path())) {
                defaults();
                save();
                debug("init version=" + DEBUG_VERSION + " schema=" + CURRENT_SCHEMA_VERSION + " config=created");
                return;
            }
            EggBadgesConfig x = GSON.fromJson(Files.readString(path()), EggBadgesConfig.class);
            if (x == null) throw new IllegalArgumentException("config was null");
            if (x.schema_version > CURRENT_SCHEMA_VERSION) {
                copyNewer(x);
                readOnlyNewerSchema = true;
                LOG.warn("[EggBadges/DEBUG] config schema {} is newer than {}; loaded read-only",
                        x.schema_version, CURRENT_SCHEMA_VERSION);
                return;
            }
            int fromSchema = x.schema_version;
            if (fromSchema < 9) {
                backup("config.pre-v9-backup.json");
                migrateLegacy(x);
            } else {
                if (fromSchema < 10) {
                    backup("config.pre-v10-backup.json");
                    migrateV9ToV10(x);
                }
                if (fromSchema < 11) {
                    backup("config.pre-v11-backup.json");
                    migrateV10ToV11(x);
                }
                if (fromSchema < 12) {
                    backup("config.pre-v12-backup.json");
                    migrateV11ToV12(x);
                }
                if (fromSchema < 13) {
                    backup("config.pre-v13-backup.json");
                    migrateV12ToV13(x);
                }
                if (fromSchema < 14) {
                    backup("config.pre-v14-backup.json");
                    migrateV13ToV14(x);
                }
                if (fromSchema < 15) {
                    backup("config.pre-v15-backup.json");
                    migrateV14ToV15(x);
                }
                if (fromSchema < 16) {
                    backup("config.pre-v16-backup.json");
                    migrateV15ToV16(x);
                }
            }
            copyFrom(x);
            normalize();
            save();
            debug("init version=" + DEBUG_VERSION + " schema=" + CURRENT_SCHEMA_VERSION
                    + " config=loaded from=" + fromSchema
                    + " gender=" + gender.show + " count=" + show_perfect_iv_count
                    + " hiddenAbility=" + show_hidden_ability);
        } catch (Exception e) {
            LOG.error("[EggBadges/DEBUG] config load failed; using in-memory defaults without overwrite", e);
            defaults();
        }
    }

    private void copyNewer(EggBadgesConfig x) {
        schema_version = x.schema_version;
        background = x.background == null ? ChannelConfig.backgroundDefault() : x.background;
        icon = x.icon == null ? ChannelConfig.iconDefault() : x.icon;
        gender = x.gender == null ? new GenderConfig() : x.gender;
        show_perfect_iv_count = x.show_perfect_iv_count;
        show_hidden_ability = x.show_hidden_ability;
        normalize();
    }

    private void copyFrom(EggBadgesConfig x) {
        schema_version = CURRENT_SCHEMA_VERSION;
        debug_diagnostics = x.debug_diagnostics;
        background = x.background == null ? ChannelConfig.backgroundDefault() : x.background;
        icon = x.icon == null ? ChannelConfig.iconDefault() : x.icon;
        gender = x.gender == null ? new GenderConfig() : x.gender;
        show_perfect_iv_count = x.show_perfect_iv_count;
        show_hidden_ability = x.show_hidden_ability;
        if (icon.configured && icon.condition == BadgeCondition.IV_COUNT) {
            show_perfect_iv_count = true;
            icon.configured = false;
            icon.condition = BadgeCondition.FIVE_5;
        }
        migrated_from_schema = x.migrated_from_schema;
    }

    private void defaults() {
        schema_version = CURRENT_SCHEMA_VERSION;
        debug_diagnostics = false;
        background = ChannelConfig.backgroundDefault();
        icon = ChannelConfig.iconDefault();
        gender = new GenderConfig();
        show_perfect_iv_count = false;
        show_hidden_ability = false;
        migrated_from_schema = null;
    }

    private void normalize() {
        if (background == null) background = ChannelConfig.backgroundDefault();
        if (icon == null) icon = ChannelConfig.iconDefault();
        if (gender == null) gender = new GenderConfig();
        if (background.condition == BadgeCondition.GENDER) gender.show = true;
        if (background.configured && (background.condition != BadgeCondition.PERFECT_6
                || !AppearancePalette.supportedBackground(background.color))) {
            background.configured = false;
        }
        if (background.configured) background.color = AppearancePalette.canonicalBackground(background.color);
        background.normalize(true);
        if (icon.configured && (icon.condition != BadgeCondition.FIVE_5
                && icon.condition != BadgeCondition.CUSTOM
                || !AppearancePalette.supportedCircle(icon.color))) {
            icon.configured = false;
        }
        if (icon.configured) icon.color = AppearancePalette.canonicalCircle(icon.color);
        icon.normalize(false);
        gender.normalize();
        // Circle and IV Count share the upper-right position; the circle assignment wins.
        if (icon.configured) show_perfect_iv_count = false;
    }

    /**
     * Pre-schema-9 five-rule configurations are converted once. This is a redesign
     * migration, not the new-install defaults path; existing enabled choices are retained
     * where they have a three-channel equivalent.
     */
    private void migrateLegacy(EggBadgesConfig x) {
        int from = x.schema_version;
        x.migrated_from_schema = from;
        x.schema_version = CURRENT_SCHEMA_VERSION;
        x.background = ChannelConfig.backgroundDefault();
        x.icon = ChannelConfig.iconDefault();
        x.gender = new GenderConfig();
        x.gender.show = false;
        x.show_perfect_iv_count = false;
        x.show_hidden_ability = false;

        BadgeRule[] old = x.badges;
        AutoIndicatorConfig auto = x.automatic_indicators;
        int dropped = 0;
        if (old != null) for (BadgeRule r : old) {
            if (r == null || !r.enabled) continue;
            if ("male".equals(r.id) || "female".equals(r.id)
                    || "male".equals(r.preset) || "female".equals(r.preset)) {
                x.gender.show = true;
                continue;
            }
            boolean perfect = "perfect".equals(r.id) || "perfect".equals(r.preset)
                    || ("exactly".equals(r.perfectCountMode) && r.perfectCount == 6);
            boolean five = "five".equals(r.id) || "five".equals(r.preset)
                    || ("exactly".equals(r.perfectCountMode) && r.perfectCount == 5);
            if (perfect && IndicatorPresentation.from(r.presentation) == IndicatorPresentation.BACKGROUND) {
                x.background.configured = true;
                x.background.condition = BadgeCondition.PERFECT_6;
                x.background.color = r.color;
                continue;
            }
            if (five) {
                x.icon.configured = true;
                x.icon.condition = BadgeCondition.FIVE_5;
                x.icon.color = r.color;
                x.icon.five_iv_minimum = r.remainderMin >= 0 ? r.remainderMin : -1;
                continue;
            }
            if (hasCustom(r)) {
                x.icon.configured = true;
                x.icon.condition = BadgeCondition.CUSTOM;
                x.icon.color = r.color;
                copyCustom(r, x.icon);
                continue;
            }
            dropped++;
        }
        if (auto != null) {
            if (auto.show_gender) x.gender.show = true;
            if (auto.show_perfect_iv_count) x.show_perfect_iv_count = true;
        }
        normalizeFiveThreshold(x.background);
        normalizeFiveThreshold(x.icon);
        if (x.icon.configured) x.show_perfect_iv_count = false;
        x.badges = null;
        x.automatic_indicators = null;
        x.performance_diagnostics = false;
        x.diagnostics = false;
        x.diagnosticsVerbose = false;
        LOG.info("[EggBadges/DEBUG] migration from={} background={} icon={} gender={} count={} dropped={}",
                from, x.background.condition, x.icon.condition, x.gender.show,
                x.show_perfect_iv_count, dropped);
    }

    /**
     * Schema 9 already used the three-channel model. Preserve every saved channel and the
     * gender visibility choice; only lift the old icon-embedded IV count into the new
     * independent toggle. The gender style is new, so it starts at the approved CORNER
     * trial style because no older saved style exists to preserve.
     */
    static void migrateV9ToV10(EggBadgesConfig x) {
        int from = x.schema_version;
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();
        if (x.icon.configured && x.icon.condition == BadgeCondition.IV_COUNT) {
            x.show_perfect_iv_count = true;
            x.icon.configured = false;
            x.icon.condition = BadgeCondition.FIVE_5;
        }
        if (x.automatic_indicators != null && x.automatic_indicators.show_perfect_iv_count)
            x.show_perfect_iv_count = true;
        if (x.gender.style == null || x.gender.style.isBlank())
            x.gender.style = GenderStyle.CORNER.name();
        if (x.migrated_from_schema == null) x.migrated_from_schema = from;
        x.schema_version = 10;
    }

    /** Schema 10 -> 11 adds only the independent hidden-ability visibility choice. */
    static void migrateV10ToV11(EggBadgesConfig x) {
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();
        if (x.migrated_from_schema == null) x.migrated_from_schema = x.schema_version;
        x.schema_version = 11;
    }

    /**
     * Schema 11 -> 12 introduces the radio-style Badge/Background selection model. Existing
     * visibility, hidden-ability, channel colors, and custom thresholds are preserved.
     * Existing exact IV comparisons remain for compatibility but are no longer offered by
     * the operator control. The retired gender background becomes the independent gender
     * checkbox.
     */
    static void migrateV11ToV12(EggBadgesConfig x) {
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();
        if (x.background.condition == BadgeCondition.GENDER) {
            x.background.configured = false;
            x.gender.show = true;
            x.background.condition = BadgeCondition.PERFECT_6;
        }
        if (x.background.condition == BadgeCondition.IV_COUNT) {
            x.background.configured = false;
            x.background.condition = BadgeCondition.PERFECT_6;
        }
        x.background.normalize(true);
        x.icon.normalize(false);
        x.gender.normalize();
        if (x.icon.configured) x.show_perfect_iv_count = false;
        if (x.migrated_from_schema == null) x.migrated_from_schema = x.schema_version;
        x.schema_version = 12;
    }

    /**
     * Schema 12 -> 13 introduces the category-first appearance model, circle-only badges,
     * and an explicit sixth-IV restriction flag. Existing colors, custom filters, legacy
     * exact comparisons, visibility, and channel assignments are preserved where the new
     * UI can represent them.
     */
    static void migrateV12ToV13(EggBadgesConfig x) {
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();
        normalizeFiveThreshold(x.background);
        normalizeFiveThreshold(x.icon);
        x.icon.shape = IconShape.CIRCLE;
        x.icon.normalize(false);
        x.background.normalize(true);
        x.gender.normalize();
        if (x.icon.configured) x.show_perfect_iv_count = false;
        if (x.background.configured && x.icon.configured
                && x.background.condition == x.icon.condition) {
            LOG.warn("[EggBadges/DEBUG] legacy same-category background+badge configuration preserved; "
                    + "the category UI shows the badge appearance until the user changes it");
        }
        if (x.migrated_from_schema == null) x.migrated_from_schema = x.schema_version;
        x.schema_version = 13;
    }

    /** Schema 13 -> 14 is retained as a structural step; v14->15 owns the palette migration. */
    static void migrateV13ToV14(EggBadgesConfig x) {
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();
        if (x.migrated_from_schema == null) x.migrated_from_schema = x.schema_version;
        x.schema_version = 14;
    }

    /**
     * Schema 14 -> 15 simplifies the UI to one Perfect background and one Circle/Count
     * icon mode. Unsupported conditions/colors are deactivated, not substituted; their
     * legacy data stays in the config and the pre-v15 backup.
     */
    static void migrateV14ToV15(EggBadgesConfig x) {
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();

        if (x.background.condition == BadgeCondition.GENDER) x.gender.show = true;
        if (x.background.configured && x.background.condition != BadgeCondition.PERFECT_6) {
            x.background.configured = false;
            LOG.warn("[EggBadges/DEBUG] legacy background condition {} is unsupported; background disabled",
                    x.background.condition);
        }
        if (x.background.configured) {
            if (!AppearancePalette.supportedBackground(x.background.color)) {
                x.background.configured = false;
                LOG.warn("[EggBadges/DEBUG] legacy background color {} is unsupported; background disabled",
                        Integer.toHexString(x.background.color));
            } else {
                int canonical = AppearancePalette.canonicalBackground(x.background.color);
                if (canonical != x.background.color) {
                    LOG.warn("[EggBadges/DEBUG] mapping legacy background blue {} to {}",
                            Integer.toHexString(x.background.color), Integer.toHexString(canonical));
                    x.background.color = canonical;
                }
            }
        }

        if (x.icon.configured && x.icon.condition != BadgeCondition.FIVE_5
                && x.icon.condition != BadgeCondition.CUSTOM) {
            x.icon.configured = false;
            LOG.warn("[EggBadges/DEBUG] legacy circle condition {} is unsupported; circle disabled",
                    x.icon.condition);
        }
        if (x.icon.configured) {
            if (!AppearancePalette.supportedCircle(x.icon.color)) {
                x.icon.configured = false;
                LOG.warn("[EggBadges/DEBUG] legacy circle color {} is unsupported; circle disabled",
                        Integer.toHexString(x.icon.color));
            } else {
                int canonical = AppearancePalette.canonicalCircle(x.icon.color);
                if (canonical != x.icon.color) {
                    LOG.warn("[EggBadges/DEBUG] mapping legacy circle blue {} to {}",
                            Integer.toHexString(x.icon.color), Integer.toHexString(canonical));
                    x.icon.color = canonical;
                }
            }
        }
        if (x.icon.configured && x.show_perfect_iv_count) {
            LOG.warn("[EggBadges/DEBUG] legacy circle+count conflict; circle wins and count is disabled");
            x.show_perfect_iv_count = false;
        }
        x.icon.shape = IconShape.CIRCLE;
        x.background.normalize(true);
        x.icon.normalize(false);
        x.gender.normalize();
        if (x.migrated_from_schema == null) x.migrated_from_schema = x.schema_version;
        x.schema_version = 15;
    }

    /**
     * Schema 15 -> 16 replaces the overlapping palettes with the approved distinct
     * Background and Circle palettes.  Each prior selectable color has a documented
     * closest semantic mapping; the first-load backup preserves the exact old value.
     */
    static void migrateV15ToV16(EggBadgesConfig x) {
        if (x.background == null) x.background = ChannelConfig.backgroundDefault();
        if (x.icon == null) x.icon = ChannelConfig.iconDefault();
        if (x.gender == null) x.gender = new GenderConfig();
        if (x.background.configured) x.background.color = AppearancePalette.canonicalBackground(x.background.color);
        if (x.icon.configured) x.icon.color = AppearancePalette.canonicalCircle(x.icon.color);
        x.background.normalize(true);
        x.icon.normalize(false);
        x.gender.normalize();
        if (x.migrated_from_schema == null) x.migrated_from_schema = x.schema_version;
        x.schema_version = 16;
    }

    static void normalizeFiveThreshold(ChannelConfig channel) {
        if (channel.five_iv_minimum > 0) {
            channel.five_iv_restricted = true;
        } else {
            channel.five_iv_minimum = 30;
            channel.five_iv_restricted = false;
        }
    }

    private static boolean hasCustom(BadgeRule r) {
        if (r.values != null) for (String s : r.values) if (s != null && !s.isBlank()) return true;
        if (r.ivMinimums != null) for (int n : r.ivMinimums) if (n >= 0) return true;
        return false;
    }

    private static void copyCustom(BadgeRule r, ChannelConfig c) {
        for (int i = 0; i < 6; i++) {
            int v = r.ivMinimums != null && i < r.ivMinimums.length ? r.ivMinimums[i] : -1;
            if (v < 0 && r.values != null && i < r.values.length) try {
                v = Integer.parseInt(r.values[i]);
            } catch (Exception ignored) {
            }
            if (v >= 0) {
                String op = r.operators != null && i < r.operators.length ? r.operators[i] : "=";
                c.stats[i] = new StatCondition(switch (op) {
                    case ">", ">=" -> StatCondition.Operator.GT;
                    case "<", "<=" -> StatCondition.Operator.LT;
                    default -> StatCondition.Operator.EQ;
                }, Math.max(0, Math.min(31, ">=".equals(op) ? v - 1 : "<=".equals(op) ? v + 1 : v)));
            }
        }
    }

    public void save() {
        if (readOnlyNewerSchema) return;
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(this));
            debug("save result=ok background=" + background.condition + " icon=" + icon.condition
                    + " gender=" + gender.show + " count=" + show_perfect_iv_count
                    + " hiddenAbility=" + show_hidden_ability);
        } catch (Exception e) {
            LOG.error("[EggBadges/DEBUG] save failed", e);
        }
    }

    private void backup(String fileName) {
        try {
            Path backup = path().resolveSibling(fileName);
            if (!Files.exists(backup)) Files.copy(path(), backup);
        } catch (Exception e) {
            LOG.warn("[EggBadges/DEBUG] migration backup failed", e);
        }
    }

    public void reset() {
        defaults();
        save();
    }

    /** Test seam: restore defaults without touching disk. */
    void resetInMemory() { defaults(); }

    /** Test seam: apply the persisted-shape normalization used after load/migration. */
    void normalizeForTest() { normalize(); }

    static boolean migrateLegacyGenderColors(BadgeRule[] rules) {
        if (rules == null || rules.length < 2) return false;
        boolean changed = false;
        if ("male".equals(rules[0].id) && rules[0].color == 0xFFFF5555) {
            rules[0].color = 0xFF5599FF;
            changed = true;
        }
        if ("female".equals(rules[1].id) && rules[1].color == 0xFF5599FF) {
            rules[1].color = 0xFFFF5555;
            changed = true;
        }
        return changed;
    }
}
