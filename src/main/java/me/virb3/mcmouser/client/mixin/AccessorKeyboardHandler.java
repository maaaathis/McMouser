package me.virb3.mcmouser.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardHandler.class)
public interface AccessorKeyboardHandler {
    @Invoker("keyPress")
    void callKeyPress(long window, int action, KeyEvent event);
}
