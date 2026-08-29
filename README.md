Flat Colored Blocks
===================

A Minecraft mod about building with vast quantities of colored blocks.


Requirements
------------

- Minecraft 26.2 and Java 25
- Fabric Loader 0.19.3+ **or** NeoForge 26.2+
- Architectury API 21.0.7+
- Fabric builds also need Fabric API 0.155.2+26.2+
- Chisels & Bits is **optional** (soft dependency). When present, colored blocks are forced chiselable and legacy C&B remaps apply.

Build with `bash gradlew clean build`. Artifacts:

- `fabric/build/libs/flatcoloredblocksrestitched-fabric-*.jar`
- `neoforge/build/libs/flatcoloredblocksrestitched-neoforge-*.jar`

Addon authors can depend on the mod jar and use `mod.flatcoloredblocks.api.FlatColoredBlocksAPI`
(read lookups, callbacks, and `registerColorType` before Architectury `LifecycleEvent.SETUP`).


Tests
-----

- `bash gradlew :common:check` validates all 1,290 palette entries and config persistence.
- `bash gradlew :fabric:runGametest` validates registration, placement, drops, Crafter
  clicks/shift-crafting, export, recipes, beacon colors, API surface, legacy world/C&B data
  (when C&B is present), and Chisels & Bits support on a server.
- `xvfb-run -a bash gradlew :fabric:runClientGametest` validates client models, tints,
  mixins, Crafter scrolling/screenshots, and (when C&B is present) an actual chisel operation.
- NeoForge: `bash gradlew :neoforge:build` (GameTests may be added later; Fabric remains the UI/UX gate).


Want to play with the mod?
--------------------------

Downloads: http://minecraft.curseforge.com/projects/flat-colored-blocks/files

Discussion Thread: http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2576750

Contributing
------------

If your interested in adding a feature, localization, or fixing a bug feel free to submit a PR, if you are unsure if it fits you can open an issue to discuss it first.
