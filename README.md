# WorldEdit->Paste

WorldEdit 的附属扩展：**粘贴时按你当前朝向自动旋转建筑**，不用再手动 `//rotate 90`。

WorldEdit 的 `//paste` 永远按剪贴板里记录的原始方向贴出来，想换个朝向只能先 `//rotate` 再贴，
而 `//rotate` 会累积、又会改变粘贴锚点位置，连续贴几栋楼很容易转晕。这个 mod 只做一件事：

```
面朝哪里，建筑就朝哪里。
```

## 兼容性

| 项 | 要求 |
|---|---|
| Minecraft | **26.2**（`[26.2,26.3)`，其他版本需重新编译） |
| 加载器 | NeoForge `26.2.0.53-beta` 及以上 |
| 前置 mod | [WorldEdit](https://modrinth.com/plugin/worldedit) `7.4+`（NeoForge 版） |
| 端 | 服务端侧（单人游戏自带集成服务端，客户端不用额外装） |

> mod 与 MC 版本是一一绑定的，26.2 的 jar 放到 26.3 或 1.20.1 上会直接加载失败。
> `mods.toml` 里写的 `versionRange` 也会拒绝其他版本。

## 安装

1. 装好 WorldEdit 的 NeoForge 版（**必须**，本 mod 依赖它的 API）
2. 把 `dist/worldedit-paste-0.3.1.jar` 放进 `.minecraft/mods/`
3. 启动游戏

进世界时聊天框会显示一张用法卡，蓝色文字可点击直接填入聊天框。

## 用法

```
//schem load 文件名      # 或 //copy 复制选区，先让剪贴板里有东西
/wp                      # 站到位置、面朝想要的方向，粘贴
/wp 90                   # 手动指定旋转角度
/wepaste                 # /wp 的长别名
```

| 命令 | 说明 |
|---|---|
| `/wp` | 按玩家 yaw 自动算旋转角度后粘贴 |
| `/wepaste` | 与 `/wp` 完全相同 |
| `/wp <角度>` | 跳过朝向计算，强制用指定角度（-360 ~ 360，调试用） |

成功时会提示实际用了多少度：

```
[WE->Paste] 已粘贴，旋转 180°（面朝南）
```

## 朝向与角度的对应关系

Minecraft 的 yaw 是逆时针的（0=南、90=西、180=北、270=东），而 WorldEdit 的 `rotateY` 正方向与之相反，
两者还差半圈。实测正确的换算公式是：

```
旋转角度 = 180° - yaw      (mod 360)
```

| 面朝 | yaw | 旋转 |
|---|---|---|
| 南（+Z） | 0 | 180° |
| 西（−X） | 90 | 90° |
| 北（−Z） | 180 | 0° |
| 东（+X） | 270 | 270° |

这也是为什么初版直接把 yaw 当角度会出问题：90° 和 270° 恰好满足 `yaw = 180 - yaw`，看起来是对的，
只有 0° 和 180° 会暴露偏差。

## 已知行为

- **会改写剪贴板自身的旋转状态**：`/wp` 之后再 `//paste` 也会带着这个方向。想回到原始方向，重新 `//schem load` 一次。
- **剪贴板为空时**只提示，不会报错。
- **支持 undo**：粘贴走了 WorldEdit 的编辑会话，`//undo` 能撤回。

## 从源码构建

需要 **JDK 25**（MC 26.2 的 mod 开发要求）和 Gradle 8.14+ / 9.x。

```bash
# 1. 把 WorldEdit 的 jar 放进 libs/（见 libs/README.md）
#    libs/worldedit-mod-7.4.5.jar

# 2. 构建
JAVA_HOME=/path/to/jdk-25 gradle build

# 产物
build/libs/worldedit-paste-<版本>.jar
```

不要用系统里那份 WorldEdit 的绝对路径写死依赖，`build.gradle` 默认读 `libs/`，
也可以用 `-PworldeditJar="D:/.../worldedit-mod-7.4.5.jar"` 临时覆盖。

### 工程结构

```
src/main/java/com/longjie/wepaste/
├── WePaste.java            # 主类，挂事件监听
├── PasteFacedCommand.java  # /wp 命令，全部逻辑
└── JoinHint.java           # 进游戏时的用法提示
src/main/resources/META-INF/neoforge.mods.toml
```

`neoforge.mods.toml` 里的 `${mod_id}` / `${version}` 由 Gradle 在 `processResources` 阶段注入，
改版本号只需改 `build.gradle` 一处。

## 致谢与许可

依赖 [WorldEdit](https://github.com/EngineHub/WorldEdit)（GPL-3.0）的 API，但本 mod 不包含也不修改它的代码，
通过公开 API 调用，因而独立按 **MIT** 发布，见 [LICENSE](LICENSE)。
