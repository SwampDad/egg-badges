package com.cobbletown.eggbadges.mixin;

import com.cobbletown.eggbadges.EggBadgeRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Places translucent indicator backgrounds beneath vanilla's item draw, never in the renderer callback. */
@Mixin(HandledScreen.class)
abstract class HandledScreenBackgroundMixin {
    @Inject(method="drawSlot",at=@At("HEAD"))
    private void eggBadges$drawSlotBackground(DrawContext context, Slot slot, CallbackInfo ci) {
        EggBadgeRenderer.renderSlotBackground((HandledScreen<?>)(Object)this,context,slot);
    }
}
