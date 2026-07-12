# Neo Guan Niao - 观鸟模组

[![Mod Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/fodoth/neoguanniao)
[![Minecraft Version](https://img.shields.io/badge/minecraft-1.21.1-green.svg)](https://minecraft.net)
[![NeoForge Version](https://img.shields.io/badge/neoforge-21.1.233-orange.svg)](https://neoforged.net)
[![License](https://img.shields.io/badge/license-GPL--3.0--only-red.svg)](LICENSE)

**Neo Guan Niao** 是一个为 Minecraft 1.21.1 添加各种鸟类生物的模组，让玩家在游戏中体验观鸟的乐趣。

---

## 📖 简介

Neo Guan Niao（新·观鸟）是原 [Guaniao](https://github.com/EdDYON/there-is-a-bird) 模组的**非官方** NeoForge 迁移版本。模组为 Minecraft 世界引入了多种真实的鸟类，每种鸟类都有其独特的行为、习性、外观和交互方式。

## ✨ 改进

- 修复了面包屑方块缺失本地化（汉化）的问题。
- 修复了鸟类图鉴编辑模式无法正常使用的问题，并略微修改了交互方式，详情请参照游戏内按键绑定。

## ⚠️ 调整

- 暂时移除了尼康相机及拍照系统，推荐搭配 [**Exposure**](https://modrinth.com/mod/exposure) 模组使用，以获得更完善的摄影体验。
- 更新模组图标。

## 🚀 更新计划

- 🤝 持续完善与更多模组的兼容性。
- 🪶 实装鸟笼系统，支持捕捉、饲养与管理鸟类。
- ⚙️ 提供可配置的鸟类行为与生成概率，方便整合包和服务器作者进行自定义配置。

---

## ✨ 特色功能

### 🐦 鸟类物种

| 鸟类         | 特性                       | 栖息环境       |
|--------------|----------------------------|----------------|
| **虎皮鹦鹉** | 活泼好动，喜欢音乐，可驯服 | 草原、村庄     |
| **麻雀**     | 群居，吃面包屑，可驯服     | 村庄、农田     |
| **夜鹭**     | 夜行性，捕鱼，可投喂       | 水域、湿地     |
| **斑鸠**     | 温和，感知天气，可配对     | 乡村、开阔地带 |
| **鸽子**     | 城市适应，可配对           | 城市、村庄     |

### 🎮 核心玩法

- **驯服系统**：使用特定食物驯服鸟类（种子/鱼）
- **鸟类大脑**：每只鸟都有基于饥饿、恐惧、疲劳、舒适度的智能行为系统
- **群体行为**：鸟类会形成群体，有集群跟随行为
- **配对系统**：部分鸟类（斑鸠、鸽子）可以配对产仔
- **鸟浴互动**：鸟类会使用鸟浴设施饮水或觅食
- **面包屑系统**：投喂面包屑吸引麻雀

### 🎨 视觉特色

- **GeckoLib 模型**：流畅的 3D 动画
- **个体缩放**：同种鸟类有个体大小差异
- **皮肤变体**：虎皮鹦鹉有多种颜色变体，鸽子有灰/白变体
- **持鱼渲染**：夜鹭捕鱼时会显示在嘴中

## 📋 需求

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.233+
- **GeckoLib**: 4.4+

## 🔧 安装

1. 安装 [NeoForge](https://neoforged.net/) 21.1.233+
2. 安装 [GeckoLib](https://www.curseforge.com/minecraft/mc-mods/geckolib) 4.4+
3. 将 `NeoGuanNiao-x.x.jar` 放入 `.minecraft/mods` 文件夹
4. 启动游戏

## 🎯 使用方法

### 驯服鸟类

| 鸟类     | 驯服物品                   | 驯服方式             |
|----------|----------------------------|----------------------|
| 虎皮鹦鹉 | 种子（小麦、甜菜、南瓜等） | 手持种子右键点击     |
| 麻雀     | 种子（小麦、甜菜、南瓜等） | 手持种子右键点击     |
| 夜鹭     | 鱼（鳕鱼、鲑鱼等）         | 手持鱼右键点击或投掷 |

### 鸟浴交互

1. 手持水桶右键点击鸟浴 → 注水
2. 向鸟浴中放入食物（鱼、肉、面包）
3. 鸟类会自动使用鸟浴盆进食

### 面包屑

- 手持面包屑右键点击地面 → 撒面包屑
- 麻雀会被吸引过来啄食
- 可帮助驯服麻雀

## 🛠️ 开发者信息

### 原作者
EdDYON、映素、哥斯拉

### 非官方 NeoForge 版作者
金子89

### 技术栈

- **语言**: Java 21
- **构建工具**: Gradle
- **模组框架**: NeoForge
- **动画引擎**: GeckoLib 4.7.2+
- **API 版本**: 21.1.117

## 📁 项目结构

```text
net.fodoth.skina.neoguanniao
├── client/                     # 客户端渲染
│   ├── entity/                 # 实体模型与渲染器
│   └── gui/                    # GUI 界面
│
├── content/                    # 游戏内容
│   ├── bath/                   # 鸟浴系统
│   ├── bird/                   # 鸟类实体
│   │   ├── brain/              # AI 行为系统
│   │   ├── flight/             # 飞行系统
│   │   ├── scale/              # 缩放系统
│   │   └── species/            # 物种实现
│   │       ├── budgerigar/     # 虎皮鹦鹉
│   │       ├── columbid/       # 鸽形目
│   │       ├── nightheron/     # 夜鹭
│   │       └── sparrow/        # 麻雀
│   │
│   ├── feed/                   # 面包屑系统
│   └── guide/                  # 观鸟指南
│
└── registry/                   # 注册系统
```


### 核心系统

#### 鸟类大脑系统 (`BirdBrain`)

基于多因素决策的智能行为系统：

- **饥饿** (`hunger`): 影响觅食行为
- **恐惧** (`fear`): 影响逃离行为
- **疲劳** (`fatigue`): 影响休息行为
- **舒适** (`comfort`): 环境影响
- **警觉** (`alertness`): 警戒状态

#### 飞行系统 (`BirdFlightController`)

- Boids 群体飞行算法
- 动态目标寻找
- 着陆接近和减速
- 飞行受阻恢复

#### 缩放系统 (`BirdModelScale`)

- 个体随机缩放
- 遗传继承（繁殖）
- 物种基础缩放配置

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 报告问题

请提交 Issue 并提供以下信息：

1. Minecraft 版本
2. NeoForge 版本
3. 模组版本
4. 问题描述和重现步骤
5. 错误日志（如 latest.log 或 crash-reports.txt ）

## 📄 许可证

本项目包含来自原模组的资源与代码，以及本项目新增的内容，因此采用**双重许可**：

* **原模组已有的资源、代码**：遵循原作者的许可证（目前为 **All Rights Reserved**）。
* **本项目新增的代码、资源及其他原创内容**：采用 **GNU General Public License v3.0 only (GPL-3.0-only)**。

### GPL-3.0-only 授予的权利

* ✅ 允许自由使用、复制和分发
* ✅ 允许修改和二次开发
* ✅ 允许商业使用
* 🔄 衍生作品必须继续采用 GPL-3.0-only 发布
* 📝 分发时必须提供对应的源代码

请注意，**GPL-3.0-only 不适用于原模组中仍受 All Rights Reserved 保护的内容**。如需使用这些内容，请遵循原作者的许可要求。

更多信息请参阅项目根目录下的 **[LICENSE](LICENSE)** 文件。

## 🙏 致谢

- 原版 [Guaniao](https://www.mcmod.cn/class/28155.html) 模组
- [GeckoLib](https://github.com/bernie-g/geckolib) 动画引擎
- [NeoForge](https://neoforged.net/) 模组框架

## 📞 联系方式

- GitHub: [@Fodoth_jinzi89](https://github.com/Fodoth-jinzi89)
- 问题反馈: [Issue Tracker](https://github.com/fodoth/neoguanniao/issues)

---

*让 Minecraft 世界充满生机，享受观鸟的乐趣！* 🦜
