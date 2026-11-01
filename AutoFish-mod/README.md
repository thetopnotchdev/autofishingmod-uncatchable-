# AutoFish (Fabric mod)

Casts your fishing rod, watches the bobber, reels in the moment a fish bites,
and recasts automatically.

- **J** — toggle AutoFish on/off
- **K** — open the AutoFish settings screen (toggle button + sliders for
  bite sensitivity, settle delay, recast delay)
- A small "AutoFish: ON/OFF" label shows in the top-left corner of your HUD

Both keys are rebindable in Controls once it's installed. This is a
client-side-only mod — no server-side files needed.

## How it actually casts/reels

It doesn't send any special "catch fish" packet — there isn't one in
vanilla. Casting and reeling both go through the same client→server
"use item" packet a real right-click sends
(`interactionManager.interactItem(...)`); the mod just triggers that call
for you at the right moments instead of you clicking.

Targets **Minecraft 1.21.1** with **Fabric Loader** + **Fabric API**.

## Important: this needs to be built, and I couldn't build it for you

I wrote this in a sandboxed environment without access to the Fabric/Mojang
Maven repositories, so I could not run `gradlew build` or verify it compiles
against the real Minecraft mappings. The code follows the standard Fabric
mod-dev conventions and should build cleanly, but you'll want to build it
yourself and fix any small API-name mismatches if Loom complains (mapping
names occasionally shift between Yarn builds).

## How to build it

There is no Gradle wrapper in this folder, so the easiest way is GitHub Actions:

1. Upload everything in this folder to a GitHub repo.
2. Open the Actions tab, wait for "Build mod jar" to get a green tick.
3. Open that run's Summary page, download the AutoFish artifact at the bottom, unzip it:
   you get autofish-1.0.0.jar.

(Local build: install JDK 21 and Gradle 8.8+, then run `gradle build`;
the jar appears in build/libs/autofish-1.0.0.jar.)

## How to install it

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Download **Fabric API** for 1.21.1 from Modrinth/CurseForge and drop it in
   your `.minecraft/mods` folder.
3. Drop `autofish-1.0.0.jar` into the same `mods` folder.
4. Launch the Fabric profile, hold a fishing rod, cast once or just press
   **J** to let AutoFish take over.

## How it detects a bite

Rather than reaching into Minecraft's internal/obfuscated fields (fragile —
breaks on every mapping or version bump), it watches the bobber's vertical
position each tick. A bite causes a sharp, distinctive downward jerk right
before the splash — the same visual cue you react to by eye. If bites are
being missed or it's triggering too early/late on your server, tweak the
constants at the top of `AutoFishClient.java`:

- `BITE_VELOCITY_THRESHOLD` — how sharp the dip must be to count as a bite
- `MIN_SETTLE_TICKS` — ignores the initial splash when the bobber lands
- `RECAST_DELAY_TICKS` — pause after reeling in before casting again

## A heads-up

Auto-fishing is a QoL/AFK tool for singleplayer or private servers. Many
public servers ban autoclickers/auto-farming mods as a rule, so check your
server's rules before using this there — getting caught can mean a ban.
