package de.mathis.macmc.client.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21 {
import de.mathis.macmc.client.Native;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

// On Minecraft < 1.21 this is an empty no-op mixin (the keyboard fix is 1.21+).
@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    //? if >=1.21 {
    // On macOS, Tab/Escape are delivered through our native monitor (which sees
    // them even while Control is held, working around MC-54194). GLFW also
    // delivers them in the normal case, so we drop GLFW's own delivery for these
    // keys to avoid handling each press twice. Events forwarded by our native
    // callback set the flag and are let through.
    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At("HEAD"), cancellable = true)
    private void macmc$dropDuplicateKeys(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (!Native.IS_MAC || Native.forwardingNativeKey) {
            return;
        }
        int key = event.input();
        if (key == GLFW.GLFW_KEY_TAB || key == GLFW.GLFW_KEY_ESCAPE) {
            ci.cancel();
        }
    }
    //?}
}
