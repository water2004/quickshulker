# QuickShulker

[English](README.md)

QuickShulker 是一个 Fabric 模组，可以直接从玩家手中或物品栏中打开便携容器和功能方块。

## Fork 来源

本仓库 fork 自 [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker)，而该项目维护并更新了 [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker) 原始项目。

原项目实现了快捷打开、容器快捷操作、配置系统和公共 API。本 fork 主要维护新版 Fabric，并让收纳袋界面对客户端变为可选。

## 相比上游的修改

相较于 `MoRanpcy/quickshulker`，本 fork 新增了：

- **收纳袋客户端可选：** Fabric 服务端可以保持收纳袋打开功能启用，同时允许完全未安装模组的客户端加入。
- **原版备用界面：** 未安装模组的客户端使用服务端分页的 `GENERIC_9x6` 原版容器界面。
- **保留增强界面：** 安装 QuickShulker 的客户端继续使用现有的 64 槽滚动收纳袋界面。
- **使用原版容器协议：** 除第一次 QuickShulker 打开请求外，开屏、槽位同步、点击、拖拽和关闭均使用原版容器数据包。
- **移除自定义菜单注册项：** 不再注册 `quickshulker:bundle_item`，避免纯原版客户端因注册表同步而断开连接。
- **支持 Minecraft 26.1 和 26.2：** 两个版本的构建产物会发布到同一个 GitHub Release。
- **自动 CI/CD：** 推送 `main` 时发布 26.2，推送 `26.1` 时把 26.1 JAR 追加到同一个 Release。

`net.kyrptonaught.quickshulker.api` 下的公共 API 保持不变。

## 下载

请从 [GitHub Releases](https://github.com/water2004/quickshulker/releases/latest) 下载最新 Fabric 版本。

| 分支 | Minecraft | 文件 |
| :--- | :--- | :--- |
| `main` | 26.2 | `quickshulker-3.0.3-26.2.jar` |
| `26.1` | 26.1 | `quickshulker-3.0.3-26.1.jar` |

## 安装方式

| 安装位置 | 行为 |
| :--- | :--- |
| 仅服务端 | 允许原版客户端加入；手持收纳袋右键时打开原版分页界面。 |
| 客户端和服务端 | 提供完整快捷操作，以及 64 槽滚动收纳袋界面。 |
| 仅客户端，服务端未安装 | 依赖服务端的快捷打开功能不可用。 |

QuickShulker 需要 [Fabric Loader](https://fabricmc.net/use/installer/) 和 [Fabric API](https://modrinth.com/mod/fabric-api)。Minecraft 26.x 需要 Java 25。

## 功能

### 快捷打开

使用可配置快捷键（默认 <kbd>K</kbd>）、物品栏右键或手持物品右键打开：

- 潜影盒
- 末影箱
- 工作台
- 切石机
- 铁砧
- 收纳袋

### 容器快捷操作

- 使用受支持的容器右击物品以放入。
- 使用受支持的容器右击空物品栏槽位以取出。
- 按住右键拖动以批量放入或取出。
- 在受支持的容器之间转移物品。

### 收纳袋客户端兼容

客户端安装 QuickShulker 时，会主动请求增强收纳袋菜单，并把下一次原版开屏事件替换为滚动界面；服务端与客户端后续仍使用同一套原版容器槽位协议。

客户端未安装模组时，只会发送普通的物品使用交互。服务端将打开一个原版六行容器，每页显示 45 个内容槽位，并由服务端控制翻页。

## 配置

可以通过 [Mod Menu](https://modrinth.com/mod/modmenu) 或配置界面快捷键（默认小键盘 <kbd>+</kbd>）打开设置。

## 构建

```bash
./gradlew clean build
```

构建产物位于 `build/libs`。

## 许可证

QuickShulker 使用 [MIT License](LICENSE)。原作者与项目历史请参阅上游仓库。
