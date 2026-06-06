package de.mathis.macmc.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entry point. The actual fixes are applied via mixins; the native
 * keyboard fix is wired up from the Minecraft mixin on macOS.
 */
public class MacmcClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
