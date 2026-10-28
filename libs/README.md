# libs/

编译用的 **WorldEdit** jar 放在这个目录，不要提交到仓库（已在 `.gitignore` 里排除）。

## 需要的版本

| 项 | 值 |
|---|---|
| 文件 | `worldedit-mod-7.4.5.jar` |
| 对应 MC | 26.2 |
| 对应加载器 | NeoForge（JDK 25） |

下载地址：<https://modrinth.com/plugin/worldedit/versions?g=26.2&l=neoforge>

## 放好之后

```
libs/worldedit-mod-7.4.5.jar
```

然后直接 `gradle build` 即可。文件名或版本不一样时，用 Gradle 属性覆盖路径：

```
gradle build -PworldeditJar="D:/path/to/worldedit-mod-7.4.x.jar"
```

## 为什么用本地 jar 而不是 Maven 坐标

WorldEdit 的 Maven 仓库要单独配仓库地址和认证，而它发布到 Modrinth 的 jar 本身就带完整 API。
mod 只需要 `compileOnly` 引用它的类，运行时游戏里那份 WorldEdit 会提供实现，所以本地引用足够。
