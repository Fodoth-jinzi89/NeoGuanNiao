# Neo Guan Niao - 观鸟模组

[![Mod Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/fodoth/neoguanniao)
[![Minecraft Version](https://img.shields.io/badge/minecraft-1.21.1-green.svg)](https://minecraft.net)
[![NeoForge Version](https://img.shields.io/badge/neoforge-21.1.233+-orange.svg)](https://neoforged.net)
[![License](https://img.shields.io/badge/license-GPL--3.0--only-red.svg)](LICENSE)

**Neo Guan Niao（新·观鸟）** 是一个为 Minecraft 添加真实鸟类生态的模组。

不同于传统宠物模组，Neo Guan Niao 致力于创造一个充满生命感的鸟类世界：
鸟类拥有独特的行为、习性、个体差异和成长过程，玩家可以通过观察、投喂、驯服和照顾，与这些天空中的居民建立联系。

---

# 📖 简介

Neo Guan Niao 是原 [Guaniao](https://github.com/EdDYON/there-is-a-bird) 模组的非官方 NeoForge 重构版本。

经过重新设计后，模组不仅完成了 Minecraft 1.21.1 NeoForge 适配，还加入了大量新的生态系统：

- 全新的鸟类行为架构
- 个体化鸟类数据
- 鸟蛋与繁殖系统
- 鸟巢与孵化系统
- 鸟类指南系统
- 鸟浴互动系统
- 鸟食袋成长调节系统
- 鸟羽经济系统
- 鸟类研究者村民交易
- 观鸟成就系统
- 更丰富的鸟类收集与观察玩法

目标是让 Minecraft 中的鸟类不再只是装饰实体，而是真正生活在世界中的生物。

---

# ✨ 核心特色

## 🐦 丰富的鸟类生态

目前包含：

| 鸟类         | 特点                             | 栖息环境   |
|--------------|----------------------------------|------------|
| **虎皮鹦鹉** | 色彩丰富，可舞蹈，会成为玩家伙伴 | 热带、草原 |
| **麻雀**     | 群居鸟类，会被面包屑吸引         | 草原、森林 |
| **夜鹭**     | 夜行性水鸟，会捕鱼               | 河流、沼泽 |
| **斑鸠**     | 温和的伴侣型鸟类，可以繁殖       | 森林、草原 |
| **鸽子**     | 城市适应型鸟类，多种羽色         | 村庄、城市 |

未来将持续加入更多鸟类。

---

# 🪶 鸟类个体系统

每一只鸟都是独特的。

鸟类拥有：

- 独立性别
- 个体体型
- 羽色皮肤
- 模型类型
- 成长状态
- 遗传属性

繁殖产生的新鸟可能继承父母特征，也可能发生突变，形成独一无二的个体。

---

# 🥚 鸟蛋与繁殖系统

部分鸟类可以形成伴侣关系并繁殖。

鸟蛋会记录：

- 鸟类种类
- 羽色
- 性别
- 体型
- 孵化时间
- 遗传信息

部分稀有皮肤只能通过繁殖获取，玩家可以扩充自己的鸟类收藏。

---

# 🪺 鸟巢系统

鸟巢不仅是装饰方块，而是鸟类生态的一部分。

支持：

- 鸟类寻找合适巢穴
- 自动产蛋
- 鸟蛋管理
- 孵化过程

让鸟类拥有更加自然的繁殖行为。

---

# 🛁 鸟浴系统

建造鸟浴盆，吸引附近鸟类。

鸟类会：

- 饮水
- 清洁羽毛
- 使用鸟浴区域作为活动地点

通过观察鸟浴盆，你可以发现附近隐藏的鸟类。

---

# 🌾 鸟食与成长调节

新增鸟食袋系统。

玩家可以使用不同类型的鸟食：

- 种子类鸟食
- 鱼类鸟食
- 成长调节鸟食

鸟食袋可以调整鸟类成长状态：

- 暂停成长
- 恢复成长
- 加速成长

让玩家能够更加自由地培养自己的鸟类伙伴。

---

# 📖 可编辑鸟类指南

模组内置鸟类观察指南。

特点：

- 记录世界上的鸟类
- 查看鸟类资料
- 支持 E 键自定义界面
- 作为玩家的观鸟手册

探索世界，收集属于自己的鸟类图鉴。

---

# 🎮 主要玩法

## 驯服鸟类

驯服后的鸟类可以：

- 跟随玩家
- 成为伙伴
- 参与互动

这是观察鸟类的重要方式。

---

# 🎨 视觉特色

## GeckoLib 动画

所有鸟类使用 GeckoLib 动画系统：

- 飞行动画
- 行走动画
- 进食动画
- 互动动画

---

# 🧠 鸟类行为系统

Neo Guan Niao 使用模块化 Controller + Ticker 架构。

每种行为独立管理，使鸟类 AI 更容易扩展。

支持：

- 飞行
- 觅食
- 逃跑
- 社交
- 繁殖
- 驯服
- 环境互动等

---

# 📋 游戏需求

- Minecraft: **1.21.1**
- NeoForge: **21.1.233+**
- Java: **21**
- GeckoLib: **4.7.2+**

---

# 🔧 安装

1. 安装 NeoForge 1.21.1
2. 安装 GeckoLib
3. 将 `NeoGuanNiao-x.x.jar` 放入 `.minecraft/mods`
4. 启动游戏

---

# 🛠️ 开发信息

## 作者

NeoForge 重构：

- Fodoth_金子89

原作者：

- EdDYON
- 映素
- 哥斯拉


---

## 技术栈

- Java 21
- NeoForge
- GeckoLib 4
- Gradle


---

# 🤝 贡献

欢迎提交 Issue 和 Pull Request。

报告问题时请提供：

1. Minecraft 版本
2. NeoForge 版本
3. 模组版本
4. 错误描述
5. 日志文件

---

# 📄 许可证

本项目采用双重许可：

## 原模组内容

原 Guaniao 模组中的代码、资源遵循原作者许可。

## Neo Guan Niao 新增内容

新增代码、资源采用：

**GNU General Public License v3.0 only**

GPL-3.0-only 授予：

- 自由使用
- 修改
- 分发
- 商业使用

但衍生作品必须继续遵循 GPL-3.0-only。

详细信息请参阅：

`LICENSE`

---

# 🙏 致谢

感谢：

- 原 Guaniao 模组作者
- NeoForge 开发团队
- GeckoLib 开发团队

---

# 📞 联系方式

GitHub:

[https://github.com/Fodoth-jinzi89](https://github.com/Fodoth-jinzi89)

Issue Tracker:

[https://github.com/Fodoth-jinzi89/NeoGuanNiao/issues](https://github.com/Fodoth-jinzi89/NeoGuanNiao/issues)


---

**让 Minecraft 世界重新拥有鸟鸣。**

🪶 Discover. Observe. Protect.


