/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterBeacons;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends HandledScreen<BeaconScreenHandler> {
    @Shadow
    protected abstract <T extends ClickableWidget> void addButton(T button);

    public BeaconScreenMixin(BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/BeaconScreen;addButton(Lnet/minecraft/client/gui/widget/ClickableWidget;)V", ordinal = 1, shift = At.Shift.AFTER), cancellable = true)
    private void changeButtons(CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) return;
        List<StatusEffect> effects = Arrays.stream(BeaconBlockEntity.EFFECTS_BY_LEVEL).flatMap(Arrays::stream).toList();

        if (MinecraftClient.getInstance().currentScreen instanceof BeaconScreen beaconScreen) {
            for (int x = 0; x<3;x++) {
                for (int y = 0; y<2; y++) {
                    addButton(beaconScreen.new EffectButtonWidget(this.x+x*25+27,this.y+y*25+32,effects.get(x*2+y),true,-1));
                    addButton(beaconScreen.new EffectButtonWidget(this.x+x*25+133,this.y+y*25+32,effects.get(x*2+y),false,-1));
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void onDrawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) return;
        //this will clear the background from useless pyramid graphics
        DrawableHelper.fill(matrices,this.x+10,this.y+7,this.x+220,this.y+98, 0xFF212121);
    }

    @Inject(method = "drawForeground", at = @At("HEAD"))
    private void onDrawForeground(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) return;
        int beaconLvl = getScreenHandler().getProperties();
        if (beaconLvl < 4) {
            drawCenteredText(matrices, this.textRenderer, "Secondary power inactive at beacon lvl " + beaconLvl, backgroundWidth/2, 86, 14737632);
        }
    }
}
