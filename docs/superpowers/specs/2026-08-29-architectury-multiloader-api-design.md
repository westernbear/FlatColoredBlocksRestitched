# Architectury Multiloader + API C Design

Date: 2026-08-29  
Branch: `feature/architectury-multiloader`

## Summary

Migrate Flat Colored Blocks Restitched from Fabric-only to Architectury `common` / `fabric` / `neoforge` for Minecraft 26.2, expose a Level C public API, and treat Chisels & Bits as optional.

## Architecture

- **common**: shared logic, assets, mixins, `mod.flatcoloredblocks.api`
- **fabric**: Fabric entrypoints, `ClientPlatformImpl`, gametests
- **neoforge**: NeoForge `@Mod` entry, NeoForge client stub, ModDevGradle

Toolchain mirrors westernbear Chisels & Bits 26.2: Architectury Loom no-remap for common/fabric, NeoForge ModDev for neoforge, Architectury API 21.0.7.

## API

Package `mod.flatcoloredblocks.api`:

- Read: color types, configs, blocks, shade→RGB
- Hooks: block registered, palette built, crafter opened
- Register: `registerColorType(ColorTypeRegistration)` until `LifecycleEvent.SETUP`

## Soft C&B

`compileOnly` C&B on common with direct `BlockBitInfo` / `ModUtil` calls, gated by `Platform.isModLoaded`. Mixin plugin skips C&B-only mixins otherwise. Metadata marks C&B optional/suggested.

## Testing

Fabric `runGametest` / `runClientGametest` remain the primary functional and UI gates. C&B assertions skip when absent. API smoke GameTest covers builtins.
