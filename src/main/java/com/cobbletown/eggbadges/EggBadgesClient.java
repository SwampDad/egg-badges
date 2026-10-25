package com.cobbletown.eggbadges;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class EggBadgesClient implements ClientModInitializer {
    @Override public void onInitializeClient(){
        EggBadgesConfig.INSTANCE.load();
        ClientTickEvents.END_CLIENT_TICK.register(EggBadgeRenderer::tick);
        ScreenEvents.AFTER_INIT.register((client,screen,width,height)->{
            if(screen instanceof OptionsScreen){ButtonWidget done=null;for(var c:Screens.getButtons(screen))if(c instanceof ButtonWidget b&&"Done".equals(b.getMessage().getString())){done=b;break;}
                int w=done==null?200:done.getWidth(),x=done==null?width/2-w/2:done.getX(),y=done==null?height-58:done.getY()-24;
                String version=FabricLoader.getInstance().getModContainer("egg-badges").map(c->c.getMetadata().getVersion().getFriendlyString()).orElse("?");
                String label="★ Egg Badges ("+version+")…";
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal(label),b->client.setScreen(new EggBadgeSettingsScreen(screen))).dimensions(x,y,Math.max(w,client.textRenderer.getWidth(label)+20),20).build());}
            ScreenEvents.afterRender(screen).register((current,context,mouseX,mouseY,delta)->EggBadgeRenderer.render(current,context,mouseX,mouseY));
        });
    }
}
