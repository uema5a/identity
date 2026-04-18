# 🎭 Identity-v3.0.0-26.1

### Added
- MC 26.1 support (Fabric loader 0.18.6+, Fabric API 0.145.1+)
- Baby mode toggle in identity selection screen — spawn as baby variant of the chosen mob

### Changed
- Migrated Yarn mappings (1.20.1) to Mojang mappings (26.1), unobfuscated
- Dropped Architectury API — Fabric-only now
- Rebased onto xGabou's active fork (villager profession, trades, and sync features preserved)
- NBT read/write migrated from `NbtCompound` to `ValueInput`/`ValueOutput`
- Networking migrated to Fabric `ServerPlayNetworking`/`ClientPlayNetworking` with `CustomPacketPayload` records
- Event system migrated from Architectury events to Fabric API events
- Build: Java 17 → Java 25 toolchain, loom 1.15, Gradle 9.4.1

### Fixed
- Player shadow scales to identity entity size
- Scissor stack balance in AbilityOverlayRenderer (prevented crash in 26.1)
- Blur-once-per-frame crash in IdentityHelpScreen

### Removed
- Forge loader support (Fabric only)
- Architectury API dependency
- Several mixins obsolete in 26.1 render system (`InGameHudMixin`, `PiglinBruteBrainMixin`, `accessor.BiomeAccessor`, `accessor.OcelotEntityModelAccessor`)

---

# 🎭 Identity-v2.9.2

### Added

### Changed
- Migrated classes to use the new Gabou's Libraries classes and utilities.

### Fixed
