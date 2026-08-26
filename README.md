Flat Colored Blocks
===================

A Minecraft mod about building with vast quantities of colored blocks.


Requirements
------------

- Minecraft 26.2 and Java 25
- Fabric Loader 0.19.3 or newer
- Fabric API 0.155.2+26.2 or newer
- Chisels & Bits 1.3.10+26.2 or newer
- Fzzy Config 0.7.6+26.2 or newer

Build with `bash gradlew clean build`. Chisels & Bits is a separate runtime dependency
and is not bundled into the Flat Colored Blocks JAR.

Tests
-----

- `bash gradlew check` validates all 1,290 palette entries and config persistence.
- `bash gradlew runGametest` validates registration, placement, drops, Crafter
  clicks/shift-crafting, export, recipes, beacon colors, legacy world/C&B data,
  and Chisels & Bits support on a server.
- `xvfb-run -a bash gradlew runClientGametest` validates client models, tints,
  mixins, Crafter scrolling/screenshots, and an actual Chisels & Bits chisel
  operation in a headless client.


Want to play with the mod?
--------------------------

Downloads: http://minecraft.curseforge.com/projects/flat-colored-blocks/files

Discussion Thread: http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2576750

Contributing
------------

If your interested in adding a feature, localization, or fixing a bug feel free to submit a PR, if you are unsure if it fits you can open an issue to discuss it first.
