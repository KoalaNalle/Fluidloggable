### 2.0.1-beta.9.1 - Minecraft 1.21.1

- Retain all beta.9 modded-fluid synchronization and Sable chunk-construction fixes.
- Use consistent fluidloggable-<loader>-<version>-mc1.21.1.jar filenames.
- Package CREDITS.md alongside the unchanged original MIT licence and copyright notice; credit original authors, upstream contributors and KoalaNalle's fork work.

### 2.0.1-beta.9 - Minecraft 1.21.1

- Fix synchronization of modded fluids, including source, flowing, and falling states, by building a complete, deterministic fluid-state ID table after registration.
- Use that table for individual updates, grouped updates, and initial chunk synchronization, and reject invalid fluid-state IDs.
- Fix the null fluid-map crash when Sable creates chunk sections directly from palettes, including the reported getSerializedSize crash during assembly.

### Compatibility

- Requires Java 21, NeoForge 21.1.134 or newer. Yet Another Config Lib is optional for the configuration screen.
- Update both the client and server. NeoForge network protocol 2 intentionally rejects older protocol-1 builds.
- The world-save format is unchanged.
- Automated regression tests cover fluid synchronization and both chunk-section constructors. Full Sable/Aeronautics assembly, fluid transfer, and reload behavior still require in-game verification.
