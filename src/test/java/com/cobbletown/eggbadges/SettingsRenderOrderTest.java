package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsRenderOrderTest {
    @Test void customContentIsBetweenTheSingleScreenBackgroundAndWidgets(){
        assertArrayEquals(new SettingsRenderOrder.Layer[]{SettingsRenderOrder.Layer.BACKGROUND,SettingsRenderOrder.Layer.CUSTOM_CONTENT,SettingsRenderOrder.Layer.WIDGETS},SettingsRenderOrder.layers());
    }
}
