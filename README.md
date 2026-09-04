# Quick Shulker

[简体中文](README_CN.md)

[![GitHub release](https://img.shields.io/github/v/release/water2004/quickshulker?include_prereleases)](https://github.com/water2004/quickshulker/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1%20%7C%2026.2-blue)](#downloads)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

> [!IMPORTANT]
> This repository is a maintained fork of [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker). It has its own releases, compatibility range, APIs, issue tracker, and documentation. Download this fork only from the [water2004/quickshulker Releases page](https://github.com/water2004/quickshulker/releases).

Quick Shulker is a Fabric mod for opening useful items directly from the player's hand or inventory and moving items into or out of carried containers. This fork supports Minecraft 26.1 and 26.2, preserves the established public extension API, makes server-side bundle support safe for unmodded clients, and adds a screen-independent shulker API for other mods.

## Downloads

### Stable 3.0.4

The stable release preserves the existing Quick Shulker UI, quick-open behavior, container actions, public API, and client-optional server fallback.

| Minecraft | Artifact |
| --- | --- |
| 26.1 | `quickshulker-3.0.4-26.1.jar` |
| 26.2 | `quickshulker-3.0.4-26.2.jar` |

Download both artifacts from the [3.0.4 release](https://github.com/water2004/quickshulker/releases/tag/3.0.4).

### 4.0 prerelease

The 4.0 alpha adds the new screen-independent shulker protocol and Fabric Transfer API server access while retaining the legacy public API and user-facing behavior.

| Minecraft | Release |
| --- | --- |
| 26.1 | [4.0.0-alpha.1-26.1](https://github.com/water2004/quickshulker/releases/tag/4.0.0-alpha.1-26.1) |
| 26.2 | [4.0.0-alpha.1-26.2](https://github.com/water2004/quickshulker/releases/tag/4.0.0-alpha.1-26.2) |

The two Minecraft artifacts are not interchangeable. Prerelease builds are intended for integration testing; back up important inventories before using them on a production server.

## Requirements

- Minecraft 26.1 or 26.2
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25

[Mod Menu](https://modrinth.com/mod/modmenu) is optional. The built-in configuration screen can also be opened with the keypad `+` key by default.

## Installation modes

| Client | Server | Behavior |
| --- | --- | --- |
| Installed | Installed | Full quick-open UI, inventory actions, enhanced bundle screen, and the 4.0 direct shulker protocol when both sides support it. |
| Not installed | Installed | Vanilla clients can still join. Server-side right-click behavior uses vanilla menus, and bundles use a paged `9 x 6` vanilla container instead of a custom menu type. Client keybind and hover actions are unavailable. |
| Installed | Not installed | The client does not pretend the protocol is available. Unsupported Quick Shulker interactions pass through to vanilla or other mods. |

For the 4.0 direct protocol, keep the Quick Shulker client and server on matching compatible releases. Integrations must capability-detect the protocol; API submission never silently falls back to screen simulation.

## Player features

### Quick open

Open a supported item held in the hand or hovered in the inventory by using the configured key or right-click action.

Default controls:

- `K`: open the held item, or the item under the cursor in an inventory;
- right-click: open supported held or hovered items when the corresponding options are enabled;
- keypad `+`: open Quick Shulker settings.

Built-in openable items:

- shulker boxes;
- crafting tables;
- stonecutters;
- ender chests;
- anvils;
- bundles.

Each item type and activation method can be enabled or disabled independently.

### Quick container actions

- Drag an item and right-click a supported container to insert it.
- Drag a container and right-click an item to insert that item.
- Drag a container and right-click an empty inventory slot to extract an item.
- Right-click one supported container with another to transfer compatible contents.
- Hold right-click while dragging to perform repeated insert or extract actions.

Shulker boxes cannot be nested by the built-in policy.

### Client-optional bundle screen

Installed clients retain the enhanced scrolling 64-slot bundle interface. A server with this fork installed does not register the old custom bundle `MenuType`; unmodded clients therefore remain compatible and receive a server-paged vanilla `GENERIC_9x6` screen instead.

After the initial open request, normal container synchronization, clicking, dragging, and closing use vanilla container packets.

## Integration APIs

Quick Shulker 4.0 contains two deliberately separate API surfaces:

- the existing registry API for teaching Quick Shulker how to open or manipulate another mod's container item;
- the new shulker-specific API for exact, screen-independent transfers and standard server-side Fabric storage access.

The new API uses the existing `QuickOpenableRegistry` as its capability source. It does not maintain a second registry or a separate authoritative container cache.

### Existing registry API

The `quickshulker` entrypoint, `RegisterQuickShulker`, `QuickOpenableRegistry`, and existing public/protected signatures remain compatible with the corresponding 3.0 API. Existing integrations can move to the matching 4.0 Minecraft artifact without rewriting their registry code.

Register an entrypoint in `fabric.mod.json`:

```json
{
  "entrypoints": {
    "quickshulker": [
      "com.example.MyQuickShulkerIntegration"
    ]
  }
}
```

Then register the supported item:

```java
public final class MyQuickShulkerIntegration implements RegisterQuickShulker {
    @Override
    public void registerProviders() {
        new QuickOpenableRegistry.Builder()
                .setItem(MyContainerItem.class)
                .setOpenAction((player, stack) -> openMyMenu(player, stack))
                .supportsBundleing(true)
                .getBundleInv((player, stack) -> createMyContainer(stack))
                .canBundleInsertItem((player, inventory, host, inserted) ->
                        accepts(host, inserted))
                .register();
    }
}
```

The historical `supportsBundleing` spelling is retained for source and binary compatibility.

### Screen-independent client shulker API

The client protocol performs one bounded transfer between exactly one player inventory slot and exactly one slot inside one carried shulker box. It does not ask the server to search containers and does not return container snapshots.

An integration normally:

1. inspects the client's existing player inventory and shulker components;
2. chooses the player slot, carried shulker host slot, and internal shulker slot;
3. checks `ShulkerTransferClient.isAvailable()` on the client thread;
4. submits the exact transfer and polls the returned handle.

```java
if (ShulkerTransferClient.isAvailable()) {
    ShulkerTransferRequest request = new ShulkerTransferRequest(
            new CarriedShulkerSlotEndpoint(shulkerHostSlot, shulkerSlot),
            new PlayerSlotEndpoint(destinationPlayerSlot),
            ShulkerItemFilter.sameItemAndComponents(expectedStack),
            64);

    ShulkerTransferHandle handle = ShulkerTransferClient.submit(request);
    // On later client ticks:
    ShulkerTransferResult result = handle.resultOrNull();
}
```

Requests are serialized per connection. Retries use a stable sequence, duplicate execution returns the same fixed-size receipt, and the server validates the live player and shulker slots before committing. Results contain only a status and moved count—not copied inventory or shulker state.

The direct protocol is currently limited to carried shulker boxes. Bundles, ender chests, arbitrary containers, server-side searches, and automatic legacy fallback are intentionally outside this API.

### Server-side shulker storage API

Server integrations can resolve one carried shulker as a standard Fabric Transfer API `SlottedStorage<ItemVariant>`:

```java
SlottedStorage<ItemVariant> shulker = ShulkerStorages
        .findCarried(player, shulkerHostSlot)
        .orElseThrow();

SingleSlotStorage<ItemVariant> destination = PlayerInventoryStorage
        .of(player)
        .getSlot(destinationPlayerSlot);

try (Transaction transaction = Transaction.openOuter()) {
    long moved = StorageUtil.move(
            shulker,
            destination,
            variant -> variant.equals(expectedVariant),
            64,
            transaction);
    if (moved > 0) transaction.commit();
}
```

All access must occur on the owning player's server thread. The returned storage follows Fabric transaction commit and rollback semantics, enforces Quick Shulker insertion policy, and becomes unusable when its host slot no longer contains the supported shulker. Callers own discovery, ordering, batching, simulation, and transaction boundaries.

The standard Fabric storage identity is shared across repeated resolutions of the same player slot, avoiding independent detached container handles and last-writer-wins behavior.

## Protocol design

The 4.0 direct protocol is intentionally small and bounded:

- one player slot and one exact carried-shulker slot per request;
- a maximum requested amount of 4,096 items;
- at most eight new requests per player per server tick;
- serial client submission and per-player server sequencing;
- idempotent retry of the current request;
- one cached fixed-size receipt per connected player, with no cached inventory state;
- server-authoritative validation and a single Fabric transaction per successful move.

These constraints keep work proportional to the request, isolate multiplayer state by player lifecycle, and avoid exposing a remote storage database whose snapshots could conflict with the server inventory.

## Troubleshooting

### The key or right-click action does nothing

Check that Quick Shulker and Fabric API match the Minecraft version and that the server also supports the requested Quick Shulker action. Confirm the activation method and item type are enabled in the config screen.

### A vanilla client cannot join a modded server

Use this fork on the server and ensure no other installed mod registers a client-required custom menu or payload for the same feature. This fork's bundle fallback itself uses only a vanilla menu type for unmodded clients.

### A direct transfer integration reports `UNSUPPORTED`

Verify that both sides use a compatible 4.0 build, `ShulkerTransferClient.isAvailable()` returned true for the current connection, the host slot still contains a supported shulker, and the endpoint indices refer to live non-equipment inventory slots.

For reproducible bugs, open an issue in [this repository](https://github.com/water2004/quickshulker/issues) and include the Minecraft version, Quick Shulker versions on both sides, installed mods, client/server logs, and exact interaction or API status.

## Building and testing

The project requires JDK 25 and includes the Gradle wrapper.

```bash
git clone https://github.com/water2004/quickshulker.git
cd quickshulker
./gradlew clean test runGameTest build
```

On Windows, use `gradlew.bat`. The release jar is written to `build/libs/`.

The main GameTest suite covers current quick-open and shulker transaction behavior. `legacy-gametest/` separately runs the built jar against the locked 3.0 public behavior contract. Tagged commits on `main` publish Minecraft 26.2 artifacts; tagged commits on `26.1` publish Minecraft 26.1 artifacts.

## License

This project is distributed under the [MIT License](LICENSE).
