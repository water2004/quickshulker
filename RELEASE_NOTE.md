# QuickShulker 4.0.1 for Minecraft 26.1

Fixes the 4.0.0 regression that prevented clients without Quick Shulker from joining a modded server.

- Removed the unconditional `quickshulker:bundle_item` menu registration. Choosing a vanilla menu after login was insufficient: Fabric checked the custom registry entry during the handshake.
- Vanilla clients and Fabric clients without Quick Shulker can join and use server-side quick-open actions again.
- Original upstream v3 clients remain supported. Their bundle interface now uses the vanilla paged container instead of the custom 64-slot menu. All bundle contents remain accessible; v4 clients keep the enhanced scrolling interface.
- Existing legacy integration APIs, v3 packet formats, and the v4 direct shulker API/protocol are unchanged.
- Added a handshake-registry GameTest and dedicated-server connection tests with real vanilla, Fabric-without-QS, upstream-v3 and v4 clients. Tests extract items from both shulker boxes and bundles and verify the authoritative server inventory. The connection matrix now gates CI releases.

Requires Minecraft 26.1, Java 25, Fabric Loader and Fabric API on the server. Install Quick Shulker on both sides to use client shortcuts and the direct shulker protocol; it is not required on the client just to join.

## 中文

修复 4.0.0 中未安装 Quick Shulker 的客户端无法进入服务器的问题。

- 移除无条件注册的旧收纳袋菜单，消除登录阶段的“未知注册表项”错误，不修改 Fabric 全局注册表校验。
- 纯原版客户端、未安装 QS 的 Fabric 客户端均可进服，并使用服务端快速打开功能。
- 原版 v3 客户端继续兼容；收纳袋界面改为原版分页布局，全部内容仍可访问。v4 客户端保留增强滚动界面。
- legacy 集成 API、v3 数据包格式、新版潜影盒 API 与协议保持不变。
- 新增真实客户端连接与取物测试，核对服务端最终背包；四种客户端测试加入 CI，失败时不发布。

4.0.0 服务端建议升级至此版本。
