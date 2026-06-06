package de.mathis.macmc.client.mixin;

//? if >=1.21 {
import net.minecraft.Util;
//?} else {
/*import net.minecraft.client.Minecraft;
*///?}
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouse {
    private double deltaX;

    // Disable macOS right-click remapping. The quirk lives in a different place
    // depending on the version: InputQuirks in 1.21+, Minecraft.ON_OSX before.
    //? if >=1.21 {
    @Redirect(method = "simulateRightClick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/InputQuirks;SIMULATE_RIGHT_CLICK_WITH_LONG_LEFT_CLICK:Z"))
    private boolean simulateRightClick() {
        return false;
    }
    //?} else {
    /*@Redirect(method = "onPress", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;ON_OSX:Z"))
    private boolean onPress() {
        return false;
    }
    *///?}

    @Inject(method = "onScroll", at = @At(value = "HEAD"))
    private void onScroll(long windowHandle, double deltaX, double deltaY, CallbackInfo ci) {
        this.deltaX = deltaX;
    }

    // Fix horizontal scrolling on macOS by restoring the dropped horizontal delta.
    @ModifyVariable(method = "onScroll", ordinal = 1, at = @At(value = "LOAD"), argsOnly = true)
    private double onScroll_deltaX(double deltaY) {
        //? if >=1.21 {
        if (Util.getPlatform() == Util.OS.OSX && deltaY == 0) {
        //?} else {
        /*if (Minecraft.ON_OSX && deltaY == 0) {
        *///?}
            return deltaX;
        }
        return deltaY;
    }
}
