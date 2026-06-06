package me.virb3.mcmouser.client;

import net.minecraft.Util;

import java.io.IOException;

/**
 * Bridge to the native macOS keyboard fix (see src/main/native/mcmouser.mm).
 *
 * <p>On macOS the bundled dylib is loaded on first access. The native code
 * installs an NSEvent monitor and calls {@link KeyCallback} for Tab/Escape
 * events that GLFW would otherwise miss while Control is held (MC-54194).
 */
public final class Native {

    public static final boolean IS_MAC = Util.getPlatform() == Util.OS.OSX;

    /**
     * When the native callback feeds a key event into Minecraft, this is set so
     * the {@code KeyboardHandler} mixin lets that single event through instead
     * of cancelling it. All key handling runs on the main/render thread, so a
     * plain flag is sufficient.
     */
    public static boolean forwardingNativeKey = false;

    private Native() {
    }

    static {
        if (IS_MAC) {
            try {
                NativeUtils.loadLibraryFromJar("/natives/mcmouser.dylib");
            } catch (IOException e) {
                throw new RuntimeException("McMouser: failed to load native library", e);
            }
        }
    }

    /**
     * Registers the key callback with the native event monitor.
     *
     * @param keyCallback callback invoked for Tab/Escape key events
     * @param cocoaWindow the NSWindow pointer (from {@code glfwGetCocoaWindow})
     */
    public static native void registerKeyCallback(KeyCallback keyCallback, long cocoaWindow);
}
