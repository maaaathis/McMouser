// Native macOS input fix for MacMc.
//
// Works around MC-54194: on macOS, when the Control key is held down, Cocoa
// never delivers the keyDown event for Tab (and Escape) to GLFW, so Minecraft
// never learns the key was pressed. This means the player list (Tab) cannot be
// opened while sprinting (Left Control).
//
// There is no way to recover this purely in Java/GLFW because the event simply
// never reaches GLFW. Instead we install an NSEvent local monitor that *does*
// see every keyDown/keyUp at the Cocoa level (regardless of held modifiers),
// translate the event into GLFW key/scancode/action/modifier values, and hand
// it back to Java so it can be fed into Minecraft's KeyboardHandler.keyPress.
//
// Compiled (default, non-ARC) ObjC++ into a universal dylib. See build.gradle.kts.

#include <jni.h>
#import <Cocoa/Cocoa.h>

// Cached JVM + callback state. We only ever keep a single registration; calling
// registerKeyCallback again replaces the previous callback.
static JavaVM* g_jvm = NULL;
static jobject g_keyCallback = NULL;
static jmethodID g_acceptMethod = NULL;
static void* g_cocoaWindow = NULL;
static bool g_monitorAdded = false;

// Attaches the current (Cocoa main) thread to the JVM and returns its JNIEnv.
static JNIEnv* getEnv() {
    JNIEnv* env = NULL;
    g_jvm->AttachCurrentThread((void**)&env, NULL);
    return env;
}

// Translate a Cocoa key event for Tab/Escape into GLFW values and forward it.
static void handleKey(NSEvent* event) {
    if (g_keyCallback == NULL || g_acceptMethod == NULL) return;

    // Only the keys that macOS swallows while Control is held.
    unsigned short scancode = event.keyCode;
    int key;
    if (scancode == 0x30 /* kVK_Tab */)         key = 258; // GLFW_KEY_TAB
    else if (scancode == 0x35 /* kVK_Escape */) key = 256; // GLFW_KEY_ESCAPE
    else return;

    // Determine the GLFW action.
    int action;
    NSEventType type = event.type;
    if (type == NSEventTypeKeyDown)    action = event.ARepeat ? 2 /* GLFW_REPEAT */ : 1 /* GLFW_PRESS */;
    else if (type == NSEventTypeKeyUp) action = 0; // GLFW_RELEASE
    else return;

    // Translate the active modifiers into the GLFW modifier bitfield.
    int mods = 0;
    NSEventModifierFlags flags = event.modifierFlags;
    if (flags & NSEventModifierFlagShift)    mods |= 0x01; // GLFW_MOD_SHIFT
    if (flags & NSEventModifierFlagControl)  mods |= 0x02; // GLFW_MOD_CONTROL
    if (flags & NSEventModifierFlagOption)   mods |= 0x04; // GLFW_MOD_ALT
    if (flags & NSEventModifierFlagCommand)  mods |= 0x08; // GLFW_MOD_SUPER
    if (flags & NSEventModifierFlagCapsLock) mods |= 0x10; // GLFW_MOD_CAPS_LOCK

    JNIEnv* env = getEnv();
    env->CallVoidMethod(g_keyCallback, g_acceptMethod, key, (jint)scancode, action, mods);
}

extern "C" JNIEXPORT void JNICALL
Java_de_mathis_macmc_client_Native_registerKeyCallback(JNIEnv* env, jclass, jobject keyCallback, jlong cocoaWindow) {
    // Replace any previously held global ref so the old callback can be GC'd.
    if (g_keyCallback != NULL) env->DeleteGlobalRef(g_keyCallback);
    g_keyCallback = keyCallback == NULL ? NULL : env->NewGlobalRef(keyCallback);

    jclass cbClass = env->FindClass("de/mathis/macmc/client/KeyCallback");
    g_acceptMethod = env->GetMethodID(cbClass, "accept", "(IIII)V");
    g_cocoaWindow = (void*)cocoaWindow;
    env->GetJavaVM(&g_jvm);

    // Install the monitor exactly once; it keeps using the latest global state.
    if (!g_monitorAdded) {
        g_monitorAdded = true;
        [NSEvent addLocalMonitorForEventsMatchingMask:(NSEventMaskKeyDown | NSEventMaskKeyUp)
                                              handler:^NSEvent* (NSEvent* event) {
            // Only handle events belonging to the Minecraft window.
            if (event.window == g_cocoaWindow) {
                handleKey(event);
            }
            // Return the event unchanged so normal processing continues.
            return event;
        }];
    }
}
