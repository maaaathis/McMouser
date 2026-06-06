package de.mathis.macmc.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21 {
import com.mojang.blaze3d.platform.Window;
import de.mathis.macmc.client.KeyCallback;
import de.mathis.macmc.client.Native;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.main.GameConfig;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

// On Minecraft < 1.21 this is an empty no-op mixin (the keyboard fix is 1.21+).
@Mixin(Minecraft.class)
public class MixinMinecraft {
    //? if >=1.21 {
    @Shadow
    private Window window;

    @Shadow
    private KeyboardHandler keyboardHandler;

    // Register the native key callback right after the window has been created.
    @Inject(
            method = "<init>(Lnet/minecraft/client/main/GameConfig;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;window:Lcom/mojang/blaze3d/platform/Window;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void macmc$registerNativeKeyCallback(GameConfig config, CallbackInfo ci) {
        if (!Native.IS_MAC) {
            return;
        }
        long cocoaWindow = GLFWNativeCocoa.glfwGetCocoaWindow(window.handle());
        KeyCallback callback = this::macmc$onNativeKey;
        Native.registerKeyCallback(callback, cocoaWindow);
    }

    // Invoked from the native event monitor (on the main/render thread) for
    // Tab/Escape events that GLFW dropped while Control was held.
    private void macmc$onNativeKey(int key, int scancode, int action, int modifiers) {
        Native.forwardingNativeKey = true;
        try {
            long handle = ((Minecraft) (Object) this).getWindow().handle();
            ((AccessorKeyboardHandler) keyboardHandler).callKeyPress(handle, action, new KeyEvent(key, scancode, modifiers));
        } finally {
            Native.forwardingNativeKey = false;
        }
    }
    //?}
}
