# QuickShulker — Client-Optional Fabric Fork

**English** | [简体中文](README_CN.md)

> [!IMPORTANT]
> **This repository is a fork of [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker), which is based on the original [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker).**
>
> This fork keeps the existing QuickShulker experience for installed clients while allowing completely unmodded clients to join Fabric servers and open bundles through a vanilla paged screen.

## Fork lineage

| Fork branch | Minecraft | Exact upstream baseline | Upstream commit | Fork version | Public API |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `main` | 26.2 | `3.0.2-26.2` | [`ae16c41`](https://github.com/MoRanpcy/quickshulker/commit/ae16c419206a79c9d1a866e79bd74dd38e3f47a5) | `3.0.3-26.2` | Fully compatible |
| `26.1` | 26.1 | `3.0.0-26.1` | [`ef2f380`](https://github.com/MoRanpcy/quickshulker/commit/ef2f3808ac8cc9ad469fb0034a8e5135380e032e) | `3.0.3-26.1` | Fully compatible |

The table records the exact commits from which the fork branches were derived, not the current versions of the upstream branches.

## Changes in this fork

- **Client-optional bundle opening:** bundle support may remain enabled on the server while unmodded clients join normally.
- **Vanilla fallback UI:** unmodded clients receive a server-side paged `GENERIC_9x6` container.
- **Existing enhanced UI preserved:** installed clients retain QuickShulker's scrolling 64-slot bundle screen.
- **Vanilla container transport:** after the initial QuickShulker request, opening, synchronization, clicking, dragging, and closing use vanilla container packets.
- **No custom bundle `MenuType`:** removing `quickshulker:bundle_item` prevents registry-sync disconnects for vanilla clients.
- **Minecraft 26.1 and 26.2 builds:** both Fabric versions are maintained by this fork.
- **Automated CI/CD:** branch builds can be published as assets of the same GitHub Release.
- **New project icon:** this fork uses its own QuickShulker artwork.

## API compatibility

The public API is fully compatible with the corresponding upstream baseline. The source tree under `net.kyrptonaught.quickshulker.api`, all public and protected signatures, entrypoint contracts, and established behavior remain unchanged.

Existing integrations that use the public QuickShulker API can replace the corresponding upstream JAR on the same Minecraft version without code changes or recompilation. The client-optional implementation is entirely outside the public API.

## Downloads

Download this fork from [GitHub Releases](https://github.com/water2004/quickshulker/releases/latest).

| Branch | Minecraft | Artifact |
| :--- | :--- | :--- |
| `main` | 26.2 | `quickshulker-3.0.3-26.2.jar` |
| `26.1` | 26.1 | `quickshulker-3.0.3-26.1.jar` |

---

## Original upstream README

The following English README is preserved from the upstream project at [`ae16c41`](https://github.com/MoRanpcy/quickshulker/commit/ae16c419206a79c9d1a866e79bd74dd38e3f47a5).

# Quick Shulker

**English** | [中文](README_CN.md)

Quickly open a held shulker box with the press of a key!

This project updates [kyrptonaught](https://github.com/kyrptonaught)'s [QuickShulker](https://github.com/kyrptonaught/quickshulker.git) mod to higher Minecraft versions，and fixes something.

---

## Latest Release

### Fabric:

Click the links below to download.

<details>
<summary>1.21.x</summary>

* [3.0.0-1.21.1](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.0-1.21.1/quickshulker-3.0.0-1.21.1.jar)
* [3.0.1-1.21.2](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.2/quickshulker-3.0.1-1.21.2.jar)
* [3.0.1-1.21.3](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.3/quickshulker-3.0.1-1.21.3.jar)
* [3.0.1-1.21.4](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.4/quickshulker-3.0.1-1.21.4.jar)
* [3.0.1-1.21.5](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.5/quickshulker-3.0.1-1.21.5.jar)
* [3.0.1-1.21.6](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.6/quickshulker-3.0.1-1.21.6.jar)
* [3.0.1-1.21.7](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.7/quickshulker-3.0.1-1.21.7.jar)
* [3.0.1-1.21.8](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.8/quickshulker-3.0.1-1.21.8.jar)
* [3.0.1-1.21.9](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.9/quickshulker-3.0.1-1.21.9.jar)
* [3.0.1-1.21.10](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.10/quickshulker-3.0.1-1.21.10.jar)
* [3.0.1-1.21.11](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-1.21.11/quickshulker-3.0.1-1.21.11.jar)

</details>

<details>
    <summary>26.x</summary>

* [3.0.1-26.1](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.1-26.1/quickshulker-3.0.1-26.1.jar)
* [3.0.2-26.2](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.2-26.2/quickshulker-3.0.2-26.2.jar)

</details>

> If you need 1.20.2 ~ 1.20.6, click [here](https://moranpcy.lanzouq.com/b004io7t1a) (password: `1ipd`). They are no longer maintained and have several issues.

### NeoForge:

<details>
<summary>26.x</summary>

* [3.0.2-26.1](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.2-26.1-neo/quickshulker-neo-3.0.2-26.1.jar)
* [3.0.2-26.2](https://github.com/MoRanpcy/quickshulker/releases/download/3.0.2-26.2-neo/quickshulker-neo-3.0.2-26.2.jar)

</details>

## Beta

### Fabric:

<details>
<summary>1.21.6-1.21.8</summary>

* [1.21.6-1.21.8-beta.1](https://github.com/MoRanpcy/quickshulker/releases/download/1.21.6-1.21.8-beta.1/quickshulker-1.21.6-1.21.8-beta.1.jar)

</details>

---

## Features

### Quick Open Item

Use a hotkey (default: <kbd>k</kbd>) or right-click to quickly open the screen of an item held in your hand or stored in your inventory.

| Supported Items  |
| :--------------: |
| Crafting Table   |
| Stonecutter      |
| Shulker Box      |
| Ender Chest      |
| Anvil            |
| Bundle           |

> On Fabric, you can disable the bundle on the server side to allow clients without mod to join.

### Quick Container Actions

* Drag a container item and right‑click another item to store that item into the container; you can also right‑click the container with an item.
* Drag a container item and right‑click an empty slot in your inventory to extract items from the container.
* Drag a container item and hold the right mouse button to batch‑store or batch‑extract items.

### Config Menu

A config menu is provided so you can easily enable or disable certain features. You can open it via [Mod Menu](https://modrinth.com/mod/modmenu), but it is not required – you can also use a configurable hotkey (default: numpad<kbd>+</kbd>).

### API

The original author provides an API that allows items from your own mod to also support [Quick Open Item](#quick-open-item) and [Quick Container Actions](#quick-container-actions).

You need to implement `RegisterQuickShulker` and register your mod in `registerProviders()`. Here is an example for version `26.2`:

* Register [Quick Open Item](#quick-open-item) for your mod.

    <details>
    <summary>Click to expand</summary>

    ```java
    import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;

    public class YourClass implements RegisterQuickShulker {
        @Override
        public void registerProviders() {
            if (...) // You can add conditions to enable/disable here
                new QuickOpenableRegistry.Builder()
                    .setItem(YourBlockOrItem.class) // Required
                    .ignoreSingleStackCheck(true)  // Optional. Set whether the item can be opened even when stacked (like Crafting Table or Anvil). Default is false.
                    .setOpenAction((player, stack) -> player.openMenu(new SimpleMenuProvider((i, playerInventory, player) ->
                            new YourItemMenu(...), YourMenuTitle))) // Required
                    .register();
        }
    }
    ```

    </details>

* Register [Quick Container Actions](#quick-container-actions) for your mod.

    <details>
    <summary>Click to expand</summary>

    ```java
    import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;

    public class YourClass implements RegisterQuickShulker {
        @Override
        public void registerProviders() {
            new QuickOpenableRegistry.Builder()
                .setItem(YourBlockOrItem.class) // Required
                .supportsBundleing(true) // Required. Default is false.
                .getBundleInv((player, stack) -> new YourItemContainer()) // Required
                .register();
        }
    }
    ```

    </details>
