# NeoForge 26.3 core port

This branch produces an experimental NeoForge build for Minecraft 26.3 and Java 25.
It currently targets NeoForge 26.3.0.33-beta and ModDevGradle 2.0.147.
It does not require Fabric Loader, Fabric API, Mod Menu, Cloth Config or a connector.

## Completed core

- NeoForge common and client entrypoints, metadata and access transformers.
- Early JSON configuration loading before block-state mixins run. Build-version lookup does not depend on a runtime ModList.
- NeoForge play payloads for stored-fluid updates, with handlers restricted to the client.
- Configuration-phase compatibility exchange, acknowledgement and missing-support rejection.
- Client-only compatibility mode retains its startup snapshot and pre-world connection guard.
- A separate protocol from the Fabric build prevents accepting its different block-state registry.
- Existing storage, persistence, water/lava flow and vanilla-renderer mixins.
- Chunk rendering targets NeoForge's five-argument SectionCompiler overload; the old Fabric hook fails there.
- NeoForge gameplay-test registration and an isolated client startup smoke test.

Configuration is in `config/fluidloggable.json`. Restart after changing startup settings.
The graphical settings screen is not included in this core build.

## Issues checked before porting

Upstream [#6](https://github.com/NightEpiphany/Fluidloggable/issues/6) did not list affected blocks.
A runtime scan found several full-collision containers, but full collision alone does not
establish a bug: some are intentional features. Spawners, shulker boxes (all colours),
pistons/sticky pistons (including retracted states) and vaults have explicit exceptions and
retain water/lava logging. Piston fluidlogging is also shown in the
[mod-page illustration](https://cdn.modrinth.com/data/cached_images/b3fc71518de04ccc1336382b479bf4b36b9b4485.png).
Beacons remain restricted pending clarification.

Other full collision cubes reject newly inserted water/lava through flow, placement and vanilla
bucket/dispenser paths. Default block/mod selections do not bypass this shape rule.
Partial blocks such as dirt paths, farmland and end portal frames remain eligible.
Vanilla deliberately waterloggable full-collision blocks (leaves, mangrove roots, copper grates
and barriers) retain that behaviour. Existing stored fluid ticks preserve their containing block,
including blocks which are no longer eligible for new fluid. Regression tests cover ordinary
building blocks, a full cube in a configured namespace, all restored containers' water placement,
and an actual lava bucket used on a retracted piston.

Upstream [#11](https://github.com/NightEpiphany/Fluidloggable/issues/11) shows
3.1.2-beta.5 on Minecraft 26.2, with C2ME also present. It contains no shareable Spark call tree,
seed or reproducible world. This branch already had upstream commit `2964bde`, which suppresses
unchanged flow updates and maintains the random-ticking fluid count incrementally.
The new enclosed pond regression mixes grass and mangrove roots, checks that fluid ticks settle,
then changes a neighbour and checks that they settle again. Existing water/lava channel tests
also check settling and drainage. These checks pass; they do not establish the cause of the
reporter's CPU spike or replace profiling the original modpack.

The initial pre-port checkpoint was validated on Fabric (54 gameplay tests) and committed as
`ec0241a`. That checkpoint predates the clarified intentional full-block exceptions above;
the current behaviour and its added regression tests are validated on NeoForge.

## Build and validation

Use a Java 25 JDK:

```text
gradlew.bat build runGameTestServer
gradlew.bat runClientSmoke
```

On Linux/macOS use `./gradlew` instead. The smoke task opens an isolated development client,
waits for resource loading and exits automatically. It does not open a user world.

Validation on Windows:
- 34 JUnit tests passed.
- 53 core gameplay tests passed on the NeoForge dedicated GameTest server.
- NeoForge client startup smoke test passed, including explicit loading of every client mixin target.
- Fabric's three Farmer's Delight gameplay tests are excluded pending that integration.

Development tests use their own mod and package so they are not packaged into the release JAR.
CI builds and runs the unit and server gameplay tests. Client smoke needs a graphical session.
The resulting mod JAR is in `build/libs/`, prefixed `fluidloggable-neoforge-`.

## Remaining stages

1. In-game client validation: join/create a world, render and update fluidlogged blocks, save/reload,
   and exercise matching, missing and incompatible multiplayer peers. Startup and server tests
   do not replace these end-to-end checks.
2. NeoForge settings screen and Sodium integration, including visual fluid flow and lighting checks.
3. Port and test optional integrations (Farmer's Delight, Comforts, Create/Copycats and others)
   against versions actually available for NeoForge 26.3. Their existing source and mixin resources
   are retained but excluded from this core build; they are not advertised as supported.
4. Arbitrary modded fluids, including liquid fertiliser, require work beyond this core port.
   The current 26.3 implementation supports water and lava only. Test the fluid-state codec,
   persistence, bucket handling, rendering and any extra fluid-state properties before enabling
   those fluids.
5. Reproduce/profile the reported swamp CPU spike if a world or Spark profile becomes available.

This is a local experimental test build.
