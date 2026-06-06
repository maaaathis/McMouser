<p align="center">
    <img width="160" height="160" src="logo.svg" alt="MacMc logo">
</p>
<h1 align="center">MacMc</h1>
<p align="center">
    A Fabric mod that fixes the most annoying macOS input bugs in Minecraft.
</p>

## Why

Minecraft on macOS ships with a handful of long standing input bugs that make the game feel broken: the mouse scrolls the wrong way, holding `Ctrl` turns every click into a right click, and you cannot open the player list while sprinting. MacMc patches all of these so the game behaves like it does everywhere else. Install it and forget about it.

## Features

| Fix | Bug | Versions |
| --- | --- | --- |
| Correct `Shift` + `Scroll` direction | [MC-121772](https://bugs.mojang.com/browse/MC-121772) | all |
| Disable `Ctrl` + `Left Click` acting as a right click | macOS quirk | all |
| Open the player list with `Tab` while sprinting (holding `Ctrl`) | [MC-54194](https://bugs.mojang.com/browse/MC/issues/MC-54194) | 1.21+ |

## Compatibility

MacMc is a single codebase that targets multiple Minecraft versions through [Stonecutter](https://stonecutter.kikugie.dev/).

| Minecraft | Loader | Notes |
| --- | --- | --- |
| 1.21.9 | Fabric | full feature set |
| 1.16.5 | Fabric | scroll and right click fixes |

The legacy LWJGL2 era (1.8.9) lives on its own `1.8.9-fabric` branch and is not part of this codebase.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for your Minecraft version.
2. Drop the MacMc JAR into your `mods` folder.
3. Launch the game. There is nothing to configure.

No Fabric API required.

## Building

The native macOS keyboard fix ships as a precompiled universal `dylib`, so a normal build (even on Linux or Windows) just bundles it.

```bash
# build the currently active version (1.21.9 by default)
./gradlew build

# build every supported version at once
./gradlew chiseledBuild
```

The finished JAR lands in `versions/<mc>/build/libs/`. Switch the active version with the Stonecutter task `Set active project to <version>`.

To rebuild the native component after editing `src/main/native/macmc.mm`, run `./gradlew compileNatives` on macOS (requires the Xcode command line tools).

## How it works

The scroll and right click fixes live in regular Minecraft code on modern (LWJGL3) versions, so a single mixin (`MixinMouse`) patches them. The version specific bits (the field that holds the right click quirk, the macOS check) are toggled per version with Stonecutter.

The `Tab` while sprinting bug is different. On macOS, Cocoa never delivers the `Tab` and `Escape` key events to GLFW while `Ctrl` is held, so it cannot be fixed in Java alone. A small native `NSEvent` monitor captures these keys at the operating system level and feeds them back into Minecraft's keyboard handling. This part is active on Minecraft 1.21+ only.

## Credits

Based on [McMouser](https://github.com/MinecraftMachina/McMouser) by ViRb3, rebuilt as a single multi version codebase.
