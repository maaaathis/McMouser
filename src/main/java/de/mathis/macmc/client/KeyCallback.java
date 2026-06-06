package de.mathis.macmc.client;

/**
 * Receives keyboard events from the native macOS event monitor.
 * Parameters mirror GLFW's key callback: key, scancode, action and modifiers.
 */
@FunctionalInterface
public interface KeyCallback {
    void accept(int key, int scancode, int action, int modifiers);
}
