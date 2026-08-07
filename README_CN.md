# QuickShulker — 客户端可选 Fabric Fork

[English](README.md) | **简体中文**

> [!IMPORTANT]
> **本仓库 fork 自 [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker)，而该项目基于 [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker) 原始项目。**
>
> 本 fork 在客户端安装模组时保留现有 QuickShulker 体验，同时允许完全未安装模组的客户端加入 Fabric 服务端，并通过原版分页界面打开收纳袋。

## Fork 来源

| Fork 分支 | Minecraft | 确切上游基线 | 上游提交 | Fork 版本 | 公共 API |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `main` | 26.2 | `3.0.2-26.2` | [`ae16c41`](https://github.com/MoRanpcy/quickshulker/commit/ae16c419206a79c9d1a866e79bd74dd38e3f47a5) | `3.0.3-26.2` | 完全兼容 |
| `26.1` | 26.1 | `3.0.0-26.1` | [`ef2f380`](https://github.com/MoRanpcy/quickshulker/commit/ef2f3808ac8cc9ad469fb0034a8e5135380e032e) | `3.0.3-26.1` | 完全兼容 |

表中记录的是 fork 分支实际派生时使用的确切提交，而不是上游分支目前的版本号。

## 本 Fork 的修改

- **收纳袋客户端可选：** 服务端可以保持收纳袋功能启用，同时允许未安装模组的客户端正常加入。
- **原版备用界面：** 未安装模组的客户端使用服务端分页的 `GENERIC_9x6` 原版容器。
- **保留现有增强界面：** 安装模组的客户端继续使用 QuickShulker 的 64 槽滚动收纳袋界面。
- **使用原版容器协议：** 除第一次 QuickShulker 请求外，开屏、同步、点击、拖拽和关闭均使用原版容器数据包。
- **移除自定义收纳袋 `MenuType`：** 不再注册 `quickshulker:bundle_item`，避免原版客户端因注册表同步而断开连接。
- **支持 Minecraft 26.1 和 26.2：** 本 fork 同时维护这两个 Fabric 版本。
- **自动 CI/CD：** 不同分支的构建产物可以发布到同一个 GitHub Release。
- **新的项目图标：** 本 fork 使用独立的 QuickShulker 图标。

## API 兼容性

公共 API 与对应上游基线完全兼容。`net.kyrptonaught.quickshulker.api` 下的源码结构、全部 public/protected 签名、entrypoint 契约和既有行为均未改变。

在相同 Minecraft 版本上，使用 QuickShulker 公共 API 的现有集成可以直接用本 fork JAR 替换对应的上游 JAR，无需修改代码或重新编译。客户端可选实现完全位于公共 API 之外。

## 下载

请从本 fork 的 [GitHub Releases](https://github.com/water2004/quickshulker/releases/latest) 下载。

| 分支 | Minecraft | 文件 |
| :--- | :--- | :--- |
| `main` | 26.2 | `quickshulker-3.0.3-26.2.jar` |
| `26.1` | 26.1 | `quickshulker-3.0.3-26.1.jar` |

---

## 上游原 README

以下中文 README 原样保留自上游项目的 [`ae16c41`](https://github.com/MoRanpcy/quickshulker/commit/ae16c419206a79c9d1a866e79bd74dd38e3f47a5) 提交。

# Quick Shulker

[English](README.md) | **中文**

用按键快速打开手持的潜影盒！

这个项目将 [kyrptonaught](https://github.com/kyrptonaught) 的 [QuickShulker](https://github.com/kyrptonaught/quickshulker.git) 模组更新到更高的我的世界版本并且修复了一些东西。

---

## Latest Release

### Fabric：

点击下面的链接下载。

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

> 如果你需要 1.20.2~1.20.6，点击[这里](https://moranpcy.lanzouq.com/b004io7t1a)，密码是`1ipd`，它们已经停止维护了，所以存在许多问题。

### Neoforge:

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

## 功能

### 快速打开物品

你可以使用快捷键（默认是<kbd>k</kbd>）或者鼠标右键快速打开玩家手里或者物品栏内物品的界面。

| 支持的物品 |
|  :-----:  |
|工作台|
|切石机|
|潜影盒|
|末影箱|
|铁砧  |
|收纳袋|

> 对于 Fabric 你可以通过在服务端禁用收纳袋来允许未装模组的客户端玩家进入。

### 容器的快捷操作

* 你可以用鼠标拖动容器右键物品来存入容器，也可以用物品右键容器。
* 你可以用鼠标拖动容器右键物品栏内的空槽位来取出容器内的物品。
* 你可以用鼠标拖动并长按鼠标右键来批量存入或者取出容器内的物品。

### 配置菜单

模组提供了一个配置菜单以便你更方便地启用或者禁用某些功能，你可以使用[Mod Menu](https://modrinth.com/mod/modmenu)来打开它，但不是必须的，你可以用一个可配置的按键来打开它，默认是小键盘的<kbd>+</kbd>。

### API

原作者提供了一个API可以让你的模组里的物品也可以做到[快速打开物品](#快速打开物品)和[容器的快捷操作](#容器的快捷操作)。

你需要实现 `RegisterQuickShulker` ，然后在 `registerProviders()` 中注册你的模组，以`26.2`为例：

* 为你的模组注册[快速打开物品](#快速打开物品)。

    <details>
    <summary>点击展开</summary>

    ```java
    import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;

    public class YourClass implements RegisterQuickShulker {
        @Override
        public void registerProviders() {
            if (...) // 你可以在这里设置启用或者禁用的条件
                new QuickOpenableRegistry.Builder()
                    .setItem(YourBlockOrItem.class) // 必需的
                    .ignoreSingleStackCheck(true)  // 可选的。设置物品是否能在堆叠时打开，就像工作台和铁砧，默认是false
                    .setOpenAction((player, stack) -> player.openMenu(new SimpleMenuProvider((i, playerInventory, player) ->
                            new YourItemMenu(...), YourMenuTitle))) // 必需的
                    .register();
        }
    }
    ```

    </details>

* 为你的模组注册[容器的快捷操作](#容器的快捷操作)。

    <details>
    <summary>点击展开</summary>

    ``` java
    import net.kyrptonaught.quickshulker.api.RegisterQuickShulker;

    public class YourClass implements RegisterQuickShulker {
        @Override
        public void registerProviders() {
            new QuickOpenableRegistry.Builder()
                .setItem(YourBlockOrItem.class) // 必需的
                .supportsBundleing(true) // 必需的。默认是false
                .getBundleInv((player, stack) -> new YourItemContainer()) // 必需的
                .register();
        }
    }

   ```

    </details>
