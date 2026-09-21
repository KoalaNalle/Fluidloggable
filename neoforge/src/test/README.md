# Fluid synchronisation regression tests

Run with Java 21:

```text
./gradlew :neoforge:test
./gradlew :neoforge:build :fabric:build
```

The NeoForge tests load the actual mod and its mixins. A test-only source and
flowing fluid are registered during the normal registry event, after vanilla's
fluid-state table is populated. No test fluids are included in the release JAR.

The six tests cover:

- Mapping modded source/flowing/falling states while preserving vanilla IDs.
- Individual fluid-update packet round trips, including empty fluid.
- Grouped section updates using the same state IDs in both directions.
- Initial chunk-section synchronisation and its advertised byte size.
- Stable IDs when the table is rebuilt.
- Rejection of invalid incoming state IDs.

The individual-update test fails with the original packet implementation with
`Can't find id for 'fluidlogged:sync_test[...]'`, matching the reported failure
for Slice & Dice fertiliser.

## Manual modpack check

Use the patched NeoForge build on both client and server. Its network protocol
version is 2; the old build uses 1 and is intentionally rejected at connection.
Singleplayer already uses the same build for both sides.

In a test world with Slice & Dice:

1. Place stairs beside flowing liquid fertiliser, and into existing fertiliser.
2. Fill and empty eligible blocks with fertiliser; check that it remains
   fertiliser and keeps the expected source/flow state.
3. Leave and return to the chunk, then leave and rejoin the world. Check that
   fertiliser inside the blocks is still present and visible.
4. Repeat basic placement/removal with water and lava.

Disk storage still uses the existing per-chunk palette and FluidState codec;
this change affects network IDs, not the world-save format.
