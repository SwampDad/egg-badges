package com.cobbletown.eggbadges;

/** Documents the only safe ordering for an in-world Screen: one blur, then content, then controls. */
final class SettingsRenderOrder {
    enum Layer { BACKGROUND, CUSTOM_CONTENT, WIDGETS }
    private SettingsRenderOrder() {}
    static Layer[] layers() { return new Layer[]{Layer.BACKGROUND, Layer.CUSTOM_CONTENT, Layer.WIDGETS}; }
}
