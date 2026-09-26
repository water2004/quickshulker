# Minecraft 1.21.1 移植与验证

本分支从 4.0.1 主线完整回迁，目标为 Minecraft **1.21.1**、Java 21、Fabric Loader 0.16.14+、Fabric API 0.116.7+1.21.1。Mod Menu 11.0.3 为可选客户端集成。元数据精确限定 1.21.1，不将后续 1.21.x 版本误报为兼容。

保留快捷打开、潜影盒/收纳袋转移、末影箱同步、无客户端模组时的分页收纳袋菜单，以及公开的 `ShulkerStorages.findCarried`、客户端转移 API 和旧扩展 API。适配包括 GLFW 输入、GuiGraphics 渲染、物品组件、容器槽位、Mixin 目标及 Fabric 网络和存储 API。网络通道 ID、请求序号、端点与状态编码保持主线协议格式；物品堆编码使用 Minecraft 1.21.1 协议，不宣称跨 Minecraft 版本联机。

## 构建和验证

使用 JDK 21，Windows 将 `./gradlew` 换为 `gradlew.bat`。

```sh
./gradlew build
./gradlew -p legacy-gametest runGameTest \
  -PquickShulkerJar=../build/libs/quickshulker-4.0.1+1.21.1.jar \
  -PlegacyBehaviorProfile=current
# Linux 全客户端矩阵需要 Xvfb/Mesa，Windows 可按 compat-test/README.md 分别启动服务器与客户端。
bash compat-test/run-matrix.sh build/libs/quickshulker-4.0.1+1.21.1.jar
```

安装文件：`build/libs/quickshulker-4.0.1+1.21.1.jar`。

2026-09-26，Windows / JDK 21.0.5 的验证结果：

| 验证 | 结果 |
|---|---|
| 主项目 build、重映射、access widener 检查 | 通过 |
| 单元测试：新旧公开 API、协议字段/边界、请求队列 | 13/13 通过 |
| 主 GameTests：事务提交/回滚、多个句柄组合、容量、禁止嵌套、请求去重和限流、分页菜单 | 30/30 通过 |
| 独立旧 API 行为 GameTests：注册、打开、物品库存、转移与兼容行为 | 41/41 通过 |
| 原版 Mojang 客户端，无 Fabric Loader/Mixin | 真实联机通过 |
| Fabric 客户端，未安装 QuickShulker | 真实联机通过 |
| 独立 legacy-wire 客户端，无 v4 类 | 旧通道协商、打开与分页菜单通过 |
| QuickShulker v4 客户端 | 直接转移与增强收纳袋界面通过 |

四类客户端均从潜影盒取出 4 个石头、从收纳袋取出 4 个钻石；客户端和独立服务器分别确认数量正确、容器清空。服务器仅绑定回环地址，测试使用离线账号。单元测试报告输出到 `build/reports/tests/test/`；兼容矩阵结果与日志输出到 `compat-test/build/matrix-results/`，CI 将其上传为 `connection-tests-*` 构件。

测试迁移还修正了 1.21.1 注册表启动顺序、打开菜单与内容包分离时的初始快照等待，以及 Loom 测试工程对本地 JAR 嵌套依赖的显式加载。生产 JAR 已内嵌 conditional-mixin，无需额外安装该库。

`legacy-wire` 是冻结旧通道的独立测试实现，并非运行历史 26.x 二进制。历史版本 JAR 只能在匹配的 Minecraft 版本下运行；矩阵支持通过 `QUICKSHULKER_LEGACY_JAR` 额外提供实现原版 v3 API 且适用于 1.21.1 的版本。未进行长时间多人压力测试。
