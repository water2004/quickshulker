# QuickShulker 4.0.0

This release adds a screen-independent storage transfer API and network protocol for client-side integrations.

- Integrations can move items between the player inventory and carried storage without opening or simulating a container screen.
- Clients detect protocol support from the connected server before selecting the direct path.
- Requests use stable IDs, server-side idempotency, and bounded client retries, so a lost response does not duplicate a transfer or leave an integration waiting indefinitely.
- The existing public API and legacy screen protocol remain available and use the same storage backend.
- Compatibility was verified with 3.0.4 and 4.0.0 on both sides: new/new, new/old, old/new, and old/old.
- Builds are provided for Minecraft 26.2.x and 26.1.x.

This repository is a fork of [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker), based on the original [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker).
