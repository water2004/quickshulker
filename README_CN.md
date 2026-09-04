# Quick Shulker

[English](README.md)

[![GitHub release](https://img.shields.io/github/v/release/water2004/quickshulker?include_prereleases)](https://github.com/water2004/quickshulker/releases)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1%20%7C%2026.2-blue)](#下载)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

> [!IMPORTANT]
> 本项目是 [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker) 的持续维护分支。本仓库拥有独立的发布、兼容范围、API、问题追踪和文档；请只从 [water2004/quickshulker Releases](https://github.com/water2004/quickshulker/releases) 下载本分支。

Quick Shulker 是一个 Fabric 模组，可以直接打开玩家手中或物品栏内的实用物品，并在玩家携带的容器中快速存取物品。本分支支持 Minecraft 26.1 与 26.2，保留既有公共扩展 API，允许未安装客户端模组的玩家安全使用服务端收纳袋兼容界面，并为其他模组增加不依赖界面的潜影盒 API。

## 下载

### 4.0 正式版

Quick Shulker 4.0 在保留既有公共 API 与玩家功能的同时，增加无界面的潜影盒协议和基于 Fabric Transfer API 的服务端访问。

| Minecraft | Release |
| --- | --- |
| 26.1 | [4.0.0-26.1](https://github.com/water2004/quickshulker/releases/tag/4.0.0-26.1) |
| 26.2 | [4.0.0-26.2](https://github.com/water2004/quickshulker/releases/tag/4.0.0-26.2) |

两个 Minecraft 版本的文件不能混用。

## 运行要求

- Minecraft 26.1 或 26.2
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25

[Mod Menu](https://modrinth.com/mod/modmenu) 是可选前置。没有 Mod Menu 时，也可使用默认的小键盘 `+` 打开内置设置界面。

## 安装方式

| 客户端 | 服务端 | 行为 |
| --- | --- | --- |
| 本分支 4.x | 本分支 4.x | 完整的快速打开界面、物品栏操作、增强收纳袋界面，以及 4.0 潜影盒直接协议。 |
| 原版 Quick Shulker 3.x | 本分支 4.x | 服务端接受已冻结的原版 v3 数据包，并使用原版 `quickshulker:bundle_item` 菜单及完全一致的 64 槽布局。 |
| 未安装 | 已安装 | 原版客户端仍可加入。服务端右键功能使用原版菜单，收纳袋使用分页的原版 `9 x 6` 容器，客户端不会收到自定义菜单类型；快捷键和悬停操作不可用。 |
| 本分支 4.x | 未安装 | 客户端不会假装协议可用。不受支持的 Quick Shulker 交互会交还给原版或其他模组处理。 |

“原版 v3”特指匹配 Minecraft 版本的 MoRanpcy Quick Shulker `3.0.0-26.1` 或 `3.0.2-26.2`。本分支自身已经停止维护的 3.x 版本不属于协议兼容目标。

使用 4.0 直接协议时，Quick Shulker 客户端与服务端应保持相互匹配的版本。集成方必须先探测能力；新 API 不会在提交失败后暗中回退到模拟界面的旧路径。

## 玩家功能

### 快速打开

使用配置的按键或右键动作，直接打开手中或物品栏鼠标所指的受支持物品。

默认控制：

- `K`：打开手持物品，或物品栏内鼠标指向的物品；
- 鼠标右键：对应选项开启时，打开手持或鼠标指向的受支持物品；
- 小键盘 `+`：打开 Quick Shulker 设置。

内置支持：

- 潜影盒；
- 工作台；
- 切石机；
- 末影箱；
- 铁砧；
- 收纳袋。

每种物品与每种触发方式都可以单独启用或关闭。

### 容器快捷操作

- 拖动物品并右键受支持容器，将物品放入容器。
- 拖动容器并右键物品，将该物品放入容器。
- 拖动容器并右键物品栏空槽，从容器取出物品。
- 用一个受支持容器右键另一个容器，转移兼容内容。
- 按住右键拖动，连续执行批量放入或取出。

内置策略禁止潜影盒嵌套。

### 客户端可选的收纳袋界面

本分支客户端继续使用可滚动的 64 槽增强收纳袋界面。服务端也保留原版 v3 的自定义收纳袋 `MenuType`，但只会发给声明支持原版 v3 协议的客户端；原版客户端绝不会收到该类型，而是使用服务端分页的原版 `GENERIC_9x6` 界面。

第一次打开请求之后，容器同步、点击、拖拽和关闭均使用原版容器数据包。

## 集成 API

Quick Shulker 4.0 有两组边界清晰的 API：

- 既有注册 API，用来告诉 Quick Shulker 如何打开或操作其他模组的容器物品；
- 新的潜影盒专用 API，用于精确的无界面传输，以及标准的服务端 Fabric Storage 访问。

新 API 仍以已有的 `QuickOpenableRegistry` 作为能力来源，不会额外维护第二套注册表或权威容器缓存。

### 既有注册 API

`quickshulker` entrypoint、`RegisterQuickShulker`、`QuickOpenableRegistry` 以及已有 public/protected 签名继续兼容对应的 3.0 API。既有集成升级到相同 Minecraft 版本的 4.0 文件时，不需要重写注册逻辑。

先在 `fabric.mod.json` 注册 entrypoint：

```json
{
  "entrypoints": {
    "quickshulker": [
      "com.example.MyQuickShulkerIntegration"
    ]
  }
}
```

然后注册受支持物品：

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

历史拼写 `supportsBundleing` 为保持源码与二进制兼容而继续保留。

### 无界面客户端潜影盒 API

客户端协议每次只在一个玩家物品栏槽位与一个随身潜影盒的一个确定槽位之间执行有界传输。它不会要求服务端搜索容器，也不会返回容器快照。

集成方通常需要：

1. 根据客户端已有的玩家物品栏和潜影盒组件查找目标；
2. 确定玩家槽位、潜影盒所在玩家槽位和潜影盒内部槽位；
3. 在客户端线程调用 `ShulkerTransferClient.isAvailable()`；
4. 提交精确传输，并轮询返回的句柄。

```java
if (ShulkerTransferClient.isAvailable()) {
    ShulkerTransferRequest request = new ShulkerTransferRequest(
            new CarriedShulkerSlotEndpoint(shulkerHostSlot, shulkerSlot),
            new PlayerSlotEndpoint(destinationPlayerSlot),
            ShulkerItemFilter.sameItemAndComponents(expectedStack),
            64);

    ShulkerTransferHandle handle = ShulkerTransferClient.submit(request);
    // 在后续客户端 tick 中：
    ShulkerTransferResult result = handle.resultOrNull();
}
```

请求按连接串行执行。重试使用稳定序列，重复执行只会返回同一个定长回执；服务端会在提交前校验实时玩家槽位和潜影盒槽位。结果只包含状态和移动数量，不携带复制的物品栏或潜影盒状态。

直接协议目前只支持玩家携带的潜影盒。收纳袋、末影箱、任意容器、服务端搜索和自动回退旧界面均明确不属于这组 API。

### 服务端潜影盒 Storage API

服务端集成可以将一个玩家携带的潜影盒解析为标准 Fabric Transfer API `SlottedStorage<ItemVariant>`：

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

所有访问必须发生在该玩家所属的服务端线程。返回的 Storage 遵循 Fabric 事务提交与回滚语义，执行 Quick Shulker 的插入策略，并在宿主槽不再包含受支持潜影盒时失效。发现、排序、批处理、模拟和事务边界均由调用方控制。

重复解析同一个玩家槽位时会共享标准 Fabric Storage 事务身份，从而避免多个相互独立的分离容器句柄以及“最后写入者覆盖”问题。

## 协议设计

4.0 直接协议有意保持小而有界：

- 每个请求只包含一个玩家槽位和一个确定的随身潜影盒槽位；
- 单次请求数量上限为 4,096；
- 每名玩家每个服务端 tick 最多接收八个新请求；
- 客户端串行提交，服务端按玩家维护序列；
- 当前请求可以幂等重试；
- 每名在线玩家只缓存一个定长回执，不缓存物品栏或容器状态；
- 服务端权威校验，每次成功移动只使用一个 Fabric 事务。

这些约束使计算量与请求规模成正比，按玩家生命周期隔离多人状态，并避免引入会与服务端物品栏权威状态冲突的远程存储数据库。

## 常见问题

### 按键或右键没有反应

确认 Quick Shulker 和 Fabric API 与 Minecraft 版本对应，且服务端也支持请求的 Quick Shulker 操作；同时检查设置中是否启用了对应触发方式和物品类型。

### 原版客户端无法加入安装模组的服务器

确认服务端使用的是本分支，并检查其他模组是否为同一功能注册了客户端必需的自定义菜单或数据包。本分支自己的收纳袋备用界面对原版客户端只使用原版菜单类型。

### 直接传输集成返回 `UNSUPPORTED`

确认双方均使用兼容的 4.0 构建，当前连接的 `ShulkerTransferClient.isAvailable()` 返回 `true`，宿主槽仍包含受支持的潜影盒，并且端点下标指向实时的非装备物品栏槽位。

遇到可复现问题时，请在[本仓库 Issues](https://github.com/water2004/quickshulker/issues)提交，并附上 Minecraft 版本、客户端与服务端 Quick Shulker 版本、已安装模组、两端日志以及具体交互或 API 状态。

## 构建与测试

项目需要 JDK 25，并已包含 Gradle Wrapper。

```bash
git clone https://github.com/water2004/quickshulker.git
cd quickshulker
./gradlew clean test runGameTest build
```

Windows 使用 `gradlew.bat`。发布 jar 输出到 `build/libs/`。

主测试会锁定当前的快速打开与潜影盒事务行为、原版 v3 数据包/菜单线契约，并分别验证 v3、v4 与原版客户端的收纳袋槽位布局；`legacy-gametest/` 另外检查继续保留的公共扩展 API。`main` 分支上的标签发布 Minecraft 26.2 文件，`26.1` 分支上的标签发布 Minecraft 26.1 文件。

## 许可证

本项目使用 [MIT License](LICENSE) 发布。
