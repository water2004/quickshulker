# QuickShulker 4.0.0-alpha.1 for Minecraft 26.1

This alpha introduces a screen-independent, shulker-specific integration API while preserving QuickShulker's existing public API and legacy screen behavior.

- Client integrations can transfer items between player slots and carried shulker boxes without opening a simulated container screen.
- Server integrations can discover carried shulker boxes as standard Fabric Transfer API `SlottedStorage<ItemVariant>` instances with transactional commit and rollback semantics.
- Client requests are serialized and retried with stable request IDs, preventing duplicate transfers after a lost response.
- Storage leases prevent stale or concurrent handles from overwriting authoritative shulker contents.
- The existing `QuickOpenableRegistry` API and legacy compatibility path remain available.
- The new protocol is currently scoped to shulker boxes; bundle support remains on the existing path.

This is an alpha release for Minecraft 26.1 and requires Java 25.

This repository is a fork of [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker), based on the original [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker).
