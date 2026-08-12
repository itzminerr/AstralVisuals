# AstralVisuals

A client-side **visual & utility mod for Minecraft 1.21.4**, built on the
[Fabric](https://fabricmc.net/) toolchain. AstralVisuals adds a custom ClickGUI,
a configurable HUD, render tweaks, and a set of quality-of-life modules.

> Built against **intermediary** mappings (no Yarn at runtime), so it ships as a
> single self-contained Fabric mod.

---

## Features

A non-exhaustive overview of the included modules:

### Render
- **ClientColor** — animate between 2–3 color stops with adjustable speed; syncs global accent to GUI & modules
- **Predictions** — trajectory visualization for arrows, tridents, ender pearls, and potions
- **Particles** — custom particle effects (spark, star, heart, dollar, snowflake) with physics & trails
- **HitColor** — change hit entity color with per-setting sync to ClientColor
- **HitEffect** — wave ripple effect on block surfaces when attacking
- **SelfNametag** — render your own nametag above head
- **BetterMinecraft** — acrylic dark GUI with custom-styled buttons & sliders
- **Custom Hitbox** — recolor entity hitboxes, hide sight line / eye box; "only players" mode
- **TargetESP** — highlight your current target
- **CrossHair** — fully configurable crosshair
- **Hud** — movable, rounded on-screen HUD elements
- **BlockOverlay**, **NoRender** (with effect particles support)
- **Camera**, **SwingAnimation**, **ViewModel**, **AspectRatio**
- **ChinaHat**, **JumpCircle**, **KillEffect**, **MotionBlur**, **Cosmetic**

### Player / Movement
- **AutoDuel** — auto-accept duel challenges
- **AutoRespawn** — automatically respawn after death
- **ItemSwap** — swap to specified hotbar slot on condition
- **LockSlot** — lock hotbar slot to prevent accidental swaps
- **TapeMouse** — auto-attack on crosshair hover with configurable delay
- **Trails** — render particle trails on player movement
- **AutoSprint**, **FreeLook**, **FakePlayer**
- **ClickPearl**, **CrystalOptimizer**, **ItemScroller**, **CardChecker**

### Misc
- **DiscordPresence** — Discord Rich Presence integration

All modules are configured through the in-game ClickGUI and persisted to a
local config file.

---

## Building

Requirements:
- **JDK 21**
- The Gradle wrapper (included)

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

The built mod jar is written to `build/libs/astralvisuals-1.1.jar`.

## Running (dev)

```bash
./gradlew runClient
```

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft **1.21.4**.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Drop `astralvisuals-1.1.jar` into your `mods/` folder.

---

## Project layout

```
src/main/java/pl/astralvisuals/
├── commands/      chat command framework
├── common/        animations, localization, repositories
├── display/       ClickGUI, HUD, screens
├── events/        event bus & listeners
├── features/      modules + settings
├── main/          mod entrypoint & listeners
├── mixins/        Mixin injections (intermediary, remap = false)
└── utils/         rendering, math, player, client helpers
src/main/resources/
├── assets/        textures, fonts, shaders, sounds, panorama
├── META-INF/jars/ bundled runtime jars (JNA)
├── mixins.json    mixin config
└── accesswidener  access widener
```

> **Note on mixins:** the build runs with `-proc:none`, so the Mixin
> annotation processor does not regenerate the refmap. New mixins must target
> **intermediary** names directly with `remap = false`, and the static
> `AstralVisuals-refmap.json` is maintained by hand.

---

## Contributors

- **[itzminerr](https://github.com/itzminerr)**

## License

See [LICENSE](LICENSE). All rights reserved — published for reference and
educational purposes. Bundled third-party components retain their own licenses.
