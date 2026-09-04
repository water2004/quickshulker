# QuickShulker 4.0.0 for Minecraft 26.2

QuickShulker 4.0 introduces a stable, screen-independent integration path for carried shulker boxes while preserving the established QuickShulker public API and player-facing features.

## Highlights

- Added a shulker-specific client protocol for moving items between player inventory slots and carried shulker boxes without simulating a container screen.
- Added a server API that exposes carried shulker boxes as standard Fabric Transfer API `SlottedStorage<ItemVariant>` instances.
- Made server-side storage operations transactional: changes become authoritative only on commit, rollback remains isolated, and stale or competing handles cannot overwrite newer contents.
- Serialized client requests and added stable request IDs and retries so lost responses do not duplicate transfers or leave integrations permanently waiting.
- Preserved the existing `QuickOpenableRegistry` public API and routed old and new behavior through the same underlying implementation.
- Added server compatibility for the original MoRanpcy Quick Shulker v3 client on the matching Minecraft version.
- Kept vanilla clients safe on a modded server by using only vanilla menu types for them, including the paged bundle interface.
- Kept the new direct protocol scoped to shulker boxes; bundles continue to use their existing APIs and UI paths.

## Compatibility

- This artifact is for Minecraft 26.2 and requires Java 25, Fabric Loader, and Fabric API.
- Install matching QuickShulker 4.x builds on the client and server to use the direct shulker protocol.
- Original Quick Shulker `3.0.2-26.2` clients can connect to this v4 server and retain their original packet and bundle-menu behavior.
- Vanilla clients can connect to a server running this release; client-only shortcuts are naturally unavailable.
- This fork's discontinued 3.x releases are not a compatibility target.

The release is covered by unit tests, the main server GameTest suite, frozen original-v3 wire-contract tests, and the legacy public-API behavior suite.

This repository is a fork of [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker), based on the original [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker).
