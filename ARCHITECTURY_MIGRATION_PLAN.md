# Architectury 迁移计划

目标：在 `architectury` 分支将 NeoGuanNiao 迁移为 Minecraft 1.21.1 的 Architectury 多平台项目，同时支持 Fabric 与 NeoForge。

## 当前进度

- [x] 创建 `architectury` 分支。
- [x] 建立 `common`、`fabric`、`neoforge` 三模块骨架。
- [x] 配置 Architectury Loom、Fabric 1.21.1 和 NeoForge 1.21.1。
- [x] 修复模块依赖配置并通过 Gradle 构建。
- [ ] 拆分公共代码与平台实现。

## 1. 盘点代码依赖

- 标记所有 NeoForge、Fabric 和 Minecraft API 引用。
- 按注册、事件、网络、配置、客户端渲染、Mixin、模组兼容进行分类。

## 2. 模块职责

- `common`：实体、方块、物品、配方、数据组件、通用逻辑、资源和通用 Mixin。
- `fabric`：Fabric 初始化、Fabric 事件、网络、配置和客户端入口。
- `neoforge`：NeoForge Mod 入口、注册器、事件、网络、配置和客户端入口。

## 3. 拆分公共代码

下一阶段从这里开始。

- 将不依赖平台 API 的 `registry`、`content`、`util` 和数据生成代码移入 `common`。
- 公共初始化改为 Architectury 公共入口，并通过平台桥接调用平台实现。
- 公共逻辑不得直接引用 NeoForge 或 Fabric 专属类。

## 4. 拆分注册系统

- `common` 声明注册对象和通用行为。
- Fabric 使用 `Registry.register` 完成平台注册。
- NeoForge 使用 `DeferredRegister` 完成平台注册。
- 两个平台通过统一的公共注册入口访问对象。

## 5. 拆分事件与网络

- 公共模块保留数据包结构、业务逻辑和处理器。
- Fabric 与 NeoForge 分别实现事件监听、数据包注册和发送。
- 使用平台桥接隔离不同网络 API。

## 6. 拆分配置

- 公共模块定义配置访问接口。
- Fabric 使用 Fabric 配置实现。
- NeoForge 保留 `ModConfigSpec` 实现。
- 公共逻辑只依赖配置接口。

## 7. 拆分客户端代码

- 公共模块保留模型、渲染逻辑和资源。
- Fabric 与 NeoForge 分别注册渲染器、粒子、按键和屏幕。
- 客户端入口分别放入对应平台模块。

## 8. 迁移 Mixin

- 通用 Mixin 放入 `common`。
- 平台专属 Mixin 分别放入 `fabric` 和 `neoforge`。
- 更新各平台 Mixin 配置及包路径，保持 `@Unique` 成员命名规范。

## 9. 使用 Fabric 源码参考

- 参考 `libs/观鸟1.21.1-Fabric模组源码` 中的初始化、注册和事件代码。
- 只提取平台实现，不直接覆盖公共代码。
- 检查并替换 `birdcamera` 专属 ID、包名和逻辑。

## 10. 验证顺序

1. 编译 `common`。
2. 编译并运行 Fabric 客户端和服务端。
3. 编译并运行 NeoForge 客户端和服务端。
4. 检查注册、资源、渲染、网络和配置加载。
5. 测试目标模组存在与不存在时的兼容性。

## 11. 提交策略

- 保留原分支作为回滚点。
- 按“模块结构、公共代码、Fabric 平台、NeoForge 平台、验证修复”分阶段提交。
- 每阶段完成后运行对应构建检查，再进入下一阶段。
