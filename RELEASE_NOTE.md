# Fakeplayer 0.4.0

This release adds Minecraft 26.2 support, a new compatibility self-test, improved translations, and several reliability fixes.

## Highlights

### Minecraft 26.2 support

- Fake players now work on Minecraft 26.2.

### Compatibility self-test

- Added the OP-only `/fp test` command.
> The command creates a temporary fake player, checks the main plugin features, reports any failures, and cleans up automatically.

### Translation improvements

- Improved translation loading, reloading, and locale fallback behavior.
- Updated Simplified Chinese, Traditional Chinese, and Hong Kong Chinese translations.
- Fixed a translation compatibility issue that prevented the plugin from starting on Minecraft 26.2.

## Fixes

- Improved fake-player spawning reliability and compatibility with login-related plugins.
- Improved fake-player registration, lifecycle, and cleanup behavior.
- Updated OpenInv inventory support and inventory titles.
- Improved proxy-network compatibility.

## Requirements

| Minecraft version | Java | CommandAPI |
| --- | --- | --- |
| 1.20.x–26.1.x | Java 21 | Any supported release except 10.0.0 |
| 26.2 | Java 25 | 12.0.0 or newer |

Paper or a compatible Paper downstream such as Purpur is required.

## Upgrade notes

1. Stop the server.
2. Replace the previous Fakeplayer jar with the 0.4.0 jar.
3. On Minecraft 26.2, update CommandAPI to 12.0.0 or newer and run the server with Java 25.
4. Start the server and run `/fp test` as an operator to check the active server implementation.

<details>
<summary>中文发布说明</summary>

## Fakeplayer 0.4.0

本次发布新增 Minecraft 26.2 支持、兼容性自检命令，改进了多语言体验，并修复了多项稳定性问题。

### 主要更新

#### Minecraft 26.2 支持

- 假人现已支持 Minecraft 26.2。

#### 兼容性自检

- 新增仅 OP 可用的 `/fp test` 命令。
> 命令会创建一个临时假人、检查主要功能、报告发现的问题，并在完成后自动清理。

#### 翻译改进

- 改进翻译加载、重载及地区回退行为。
- 更新简体中文、繁体中文及香港繁体中文翻译。
- 修复 Minecraft 26.2 上因翻译组件不兼容而导致插件无法启动的问题。

### 问题修复

- 提高假人生成的稳定性及与登录相关插件的兼容性。
- 改进假人的注册、生命周期及清理行为。
- 改进 OpenInv 背包支持及背包标题显示。
- 改进代理网络兼容性。

### 运行要求

| Minecraft 版本 | Java | CommandAPI |
| --- | --- | --- |
| 1.20.x–26.1.x | Java 21 | 除 10.0.0 外的受支持版本 |
| 26.2 | Java 25 | 12.0.0 或更高版本 |

需要使用 Paper 或兼容的 Paper 下游服务端，例如 Purpur。

### 升级步骤

1. 停止服务器。
2. 使用 0.4.0 jar 替换旧版 Fakeplayer。
3. Minecraft 26.2 需要将 CommandAPI 更新至 12.0.0 或更高版本，并使用 Java 25 启动服务器。
4. 启动服务器后，以管理员身份运行 `/fp test` 检查当前服务端实现。

</details>
