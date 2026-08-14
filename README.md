# AstralVisuals

AstralVisuals is a client-side visual and utility mod for **Minecraft 1.21.4**, built with [Fabric](https://fabricmc.net/). Version **1.2** includes 40 configurable modules, a searchable ClickGUI, a movable HUD, chat commands, waypoints, macros, an offline account manager, custom shaders, sounds, and cosmetics.

The project uses intermediary mappings. JNA and Satin are bundled in the built mod; Fabric Loader and Fabric API are required separately.

## What's current in 1.2

- Added **Cooldowns**, **World Tweaks**, **Waypoint ESP**, **Sky Shader**, and **Hand Shader**.
- Expanded **Interface** with HUD scaling, notifications, sounds, and visual settings.
- Consolidated spheres, halo, body trail, and custom capes under **Cosmetic**.
- Removed outdated standalone `Hud` and `Trails` entries from the documentation; their current equivalents are **Interface** and **Cosmetic**.
- Updated the documented artifact from `astralvisuals-1.1.jar` to `astralvisuals-1.2.jar`.

## Features

The list below matches the modules currently registered by the client.

### Render modules

- **Aspect Ratio** — overrides the rendered aspect ratio.
- **Better Minecraft** — restyles vanilla screens, buttons, and sliders.
- **Block Overlay** — configurable block selection fill, outline, and shader effects.
- **Camera** — adjusts third-person distance and adds configurable zoom.
- **China Hat** — renders a customizable hat for the player and friends.
- **Client Color** — provides static or animated two/three-color accents shared by the GUI and modules.
- **Cosmetic** — configurable spheres, halo, body trail, and local PNG capes.
- **Crosshair** — replaces the vanilla crosshair with a configurable design and attack indicator.
- **Custom Hitbox** — recolors entity hitboxes with player/friend filters and optional sight/eye boxes.
- **Hand Shader** — applies configurable wave, outline, glow, fill, and color effects to first-person hands.
- **Hit Color** — changes the damage overlay color and opacity.
- **Hit Effect** — draws a configurable wave effect on attacked block surfaces.
- **Interface** — controls the accent color, HUD and GUI scale, notifications, and interface sounds.
- **Jump Circle** — renders animated circles below jumping players.
- **Kill Effect** — adds visual and sound effects when an entity dies, including custom sounds.
- **Motion Blur** — configurable post-processing motion blur with refresh-rate scaling.
- **No Render** — hides selected overlays, weather, particles, and other visual distractions.
- **Particles** — custom hit and world particles with selectable sprites, physics, and trails.
- **Predictions** — renders trajectories for arrows, tridents, ender pearls, and potions.
- **Self Nametag** — renders the local player's nametag in third person.
- **Sky Shader** — applies animated custom shader effects to the sky.
- **Swing Animation** — changes first-person swing style, speed, and strength.
- **Target ESP** — highlights the current target with configurable effects and colors.
- **View Model** — independently changes the position and scale of both hands.
- **Waypoint ESP** — renders waypoint beams, markers, names, and distances in the world.
- **World Tweaks** — controls brightness, world time, fog distance, and fog color.

### Player and utility modules

- **Auto Duel** — automatically handles supported duel requests.
- **Auto Respawn** — respawns automatically after death.
- **Auto Sprint** — keeps the player sprinting according to the selected conditions.
- **Card Checker** — detects and marks configured entities/carts in the world.
- **Click Pearl** — throws an ender pearl from a bound key and can switch back afterward.
- **Cooldowns** — shows server cooldown timers over hotbar and inventory items.
- **Crystal Optimizer** — improves end-crystal interaction handling.
- **Discord RPC** — displays configurable Rich Presence information in Discord.
- **Fake Player** — spawns a local practice player at a configurable distance.
- **Free Look** — lets the camera rotate independently from the player.
- **Item Scroller** — streamlines moving matching items through inventory slots.
- **Item Swap** — switches to a configured hotbar slot when its condition is met.
- **Lock Slot** — prevents accidental changes to a selected hotbar slot.
- **Tape Mouse** — automatically clicks while its crosshair and delay conditions are met.

## Interface and HUD

The ClickGUI includes module search, categories, key binds, color presets, grouped settings, sliders, text inputs, single-select and multi-select controls. Its default key is **Right Shift** and it can be changed with the `bind set clickgui` command.

The movable HUD contains:

- Watermark
- Target HUD
- Potions
- Hotkeys
- Inventory
- Notifications
- Coordinates
- Waypoints

The main menu also includes an offline account manager. The active launcher account is imported automatically when possible.

## Commands

The default command prefix is `.`. Available commands are:

- `.help` — list commands or show command help.
- `.bind` — manage module binds and the ClickGUI key.
- `.config` — save, load, list, or remove configurations.
- `.friend` — manage the friend list.
- `.macro` — create and manage key-bound chat macros.
- `.way` — create and manage server-specific waypoints.
- `.prefix` — change the command prefix.

Configuration data is stored under `AstralVisuals/Files`. Custom capes and kill sounds belong in `AstralVisuals/Custom/Capes` and `AstralVisuals/Custom/KillSounds`.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft **1.21.4**.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Place `astralvisuals-1.2.jar` in the Minecraft `mods` directory.

## Building

Requirements:

- JDK 21
- The included Gradle wrapper

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

The built mod is written to `build/libs/astralvisuals-1.2.jar`.

For a development client:

```bash
./gradlew runClient
```

## Project layout

```text
src/main/java/pl/astralvisuals/
|-- commands/      chat commands and argument parsing
|-- common/        animations, localization, and repositories
|-- display/       ClickGUI, HUD, main menu, and account manager
|-- events/        event definitions
|-- features/      modules and settings
|-- main/          client information and listeners
|-- mixins/        Minecraft injections
`-- utils/         rendering, shaders, math, player, and client helpers
src/main/resources/
|-- assets/        textures, fonts, shaders, sounds, and panorama
|-- META-INF/jars/ bundled JNA runtime libraries
|-- mixins.json    Mixin configuration
`-- accesswidener  access widener
```

> The build uses `-proc:none`. Mixins target intermediary names with `remap = false`, and the static `AstralVisuals-refmap.json` is maintained manually.

## Contributors

- [itzminerr](https://github.com/itzminerr)

## License

See [LICENSE](LICENSE). All rights reserved; the source is published for reference and educational purposes. Bundled third-party components retain their own licenses.
