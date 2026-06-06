package de.mathis.macmc.client.mixin;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21 {
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.gen.Invoker;
//?}

// On Minecraft < 1.21 this is an empty no-op mixin: the keyboard fix targets
// APIs (KeyEvent / keyPress(...KeyEvent)) that only exist on 1.21+.
@Mixin(KeyboardHandler.class)
public interface AccessorKeyboardHandler {
    //? if >=1.21 {
    @Invoker("keyPress")
    void callKeyPress(long window, int action, KeyEvent event);
    //?}
}
