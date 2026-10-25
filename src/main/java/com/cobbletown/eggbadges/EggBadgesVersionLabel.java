package com.cobbletown.eggbadges;

import net.fabricmc.loader.api.FabricLoader;

/** Builds UI-STD-001's label from the installed Fabric metadata, never a build constant. */
final class EggBadgesVersionLabel {
    private static final String MOD_ID = "egg-badges";
    private static final String MOD_NAME = "Egg Badges";

    private EggBadgesVersionLabel() {
    }

    static String text() {
        String version = FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        return format(version);
    }

    static String format(String version) {
        String displayed = version == null || version.isBlank() ? "unknown" : version;
        return MOD_NAME + " · v" + displayed;
    }
}
