### 2.0.1-beta.9.1 - Minecraft 1.21.1

- Retain all beta.9 modded-fluid synchronization and Sable chunk-construction fixes.
- Use consistent fluidloggable-<loader>-<version>-mc1.21.1.jar filenames.
- Package CREDITS.md alongside the unchanged original MIT licence and copyright notice; credit original authors, upstream contributors and KoalaNalle's fork work.

### 2.0.1-beta.9 - Minecraft 1.21.1

- Initialize fluid storage for both chunk-section constructors, including sections created directly from palettes. This fixes the null fluid-map crash on that construction path used by Sable.
- Use the platform fluid-state mapper consistently for individual updates, grouped updates, and chunk synchronization, and reject invalid fluid-state IDs.

### Compatibility

- Requires Java 21, Fabric Loader 0.18.2 or newer, Fabric API. Yet Another Config Lib is optional for the configuration screen.
- The world-save format is unchanged.
