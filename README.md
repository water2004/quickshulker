# QuickShulker

[简体中文](README_CN.md)

QuickShulker is a Fabric mod for opening portable containers and utility blocks directly from a player's hand or inventory.

## Fork lineage

This repository is a fork of [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker), which updates and maintains the original [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker).

The upstream projects created the original quick-open behavior, container actions, configuration system, and public API. This fork focuses on modern Fabric compatibility and client-optional bundle screens.

## Changes in this fork

Compared with `MoRanpcy/quickshulker`, this fork adds:

- **Client-optional bundle opening:** a Fabric server can keep bundle opening enabled while allowing completely unmodded clients to join.
- **Vanilla fallback screen:** unmodded clients open bundles through a server-side paged `GENERIC_9x6` container.
- **Enhanced installed-client screen:** clients with QuickShulker keep the existing 64-slot scrolling bundle interface.
- **Vanilla container transport:** after the initial QuickShulker open request, screen opening, slot synchronization, clicks, drag actions, and closing use vanilla container packets.
- **No custom menu registry entry:** the bundle screen no longer registers `quickshulker:bundle_item`, avoiding registry-sync disconnects for vanilla clients.
- **Minecraft 26.1 and 26.2 builds:** both versions are published as assets of the same GitHub Release.
- **Automated CI/CD:** pushes to `main` publish the 26.2 asset, and pushes to `26.1` append the 26.1 asset.

The public QuickShulker API under `net.kyrptonaught.quickshulker.api` remains unchanged.

## Downloads

Download the newest Fabric builds from [GitHub Releases](https://github.com/water2004/quickshulker/releases/latest).

| Branch | Minecraft | Artifact |
| :--- | :--- | :--- |
| `main` | 26.2 | `quickshulker-3.0.3-26.2.jar` |
| `26.1` | 26.1 | `quickshulker-3.0.3-26.1.jar` |

## Installation modes

| Installation | Behavior |
| :--- | :--- |
| Server only | Vanilla clients may join. Right-clicking a held bundle opens the vanilla paged screen. |
| Client and server | Full QuickShulker controls and the scrolling 64-slot bundle screen are available. |
| Client without a QuickShulker server | Server-backed quick-open features are unavailable. |

QuickShulker requires [Fabric Loader](https://fabricmc.net/use/installer/) and [Fabric API](https://modrinth.com/mod/fabric-api). Java 25 is required for Minecraft 26.x.

## Features

### Quick open

Use the configurable hotkey (default: <kbd>K</kbd>), inventory right-click, or held-item right-click to open supported items:

- Shulker boxes
- Ender chests
- Crafting tables
- Stonecutters
- Anvils
- Bundles

### Quick container actions

- Right-click an item with a supported container to insert it.
- Right-click an empty inventory slot with a supported container to extract an item.
- Drag while holding right-click to insert or extract multiple stacks.
- Transfer items between supported containers.

### Bundle client compatibility

When the client has QuickShulker, it explicitly requests the enhanced bundle menu and replaces the next vanilla open-screen event with the scrolling interface. The server and client still use the same vanilla container slot protocol.

When the client is unmodded, it sends only the normal use-item interaction. The server opens a six-row vanilla container with 45 content slots per page and server-controlled navigation.

## Configuration

Open the configuration screen through [Mod Menu](https://modrinth.com/mod/modmenu) or the configurable settings key (default: numpad <kbd>+</kbd>).

## Building

```bash
./gradlew clean build
```

Built JARs are written to `build/libs`.

## License

QuickShulker is distributed under the [MIT License](LICENSE). See the upstream repositories for the original authors and project history.
