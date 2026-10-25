package com.cobbletown.eggbadges.mixin;

import com.cobbletown.eggbadges.EggBadgeRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws cursor-held egg overlays at vanilla's cursor item origin, under then over the sprite. */
@Mixin(HandledScreen.class)
abstract class HandledScreenCursorMixin {
    @Inject(method = "drawItem", at = @At("HEAD"))
    private void eggBadges$cursorBackground(DrawContext context, ItemStack stack, int x, int y, String amountText, CallbackInfo ci) {
        EggBadgeRenderer.renderCursorBackground(context, (HandledScreen<?>)(Object)this, stack, x, y);
    }

    @Inject(method = "drawItem", at = @At("RETURN"))
    private void eggBadges$cursorOverlays(DrawContext context, ItemStack stack, int x, int y, String amountText, CallbackInfo ci) {
        EggBadgeRenderer.renderCursorOverlays(context, (HandledScreen<?>)(Object)this, stack, x, y);
    }
}
