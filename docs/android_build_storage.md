# Android APK 构建与磁盘占用

本文记录本机为构建 `opencode_android_client` 配置 Android 环境、生成 APK，以及将大体积构建缓存迁移到 `/mnt/tool` 的结果。

## 已生成的 APK

- 产物：`app/build/outputs/apk/debug/app-debug.apk`
- 完整路径：`/mnt/tool/2-projects/opencode_android_client/app/build/outputs/apk/debug/app-debug.apk`
- 构建任务：`assembleDebug`
- APK SHA-256（本次构建）：`1c3df2aa80e75cab4e7c9d8952122e6a09e0a26edd09c66121ce21694a796776`

该项目的 Release 页面目前仅有源码压缩包；若需要可安装 APK，应在本地生成 debug APK，或另行配置 release 签名与发布流程。

## 已安装与迁移的内容

构建使用 JDK 21。它由系统包管理器安装，保留在根分区，以便下次构建：

```text
/usr/lib/jvm/java-21-openjdk-amd64
```

以下内容已迁移到容量较大的 `/mnt/tool`：

```text
/mnt/tool/android-build-cache/
├── .gradle/          # Gradle 用户缓存；~/.gradle 已软链接到这里
├── android-sdk/      # Android SDK 的存储副本
└── gradle-9.3.1/     # Gradle 9.3.1 解压目录
```

已删除仅用于下载的压缩包：

```text
/tmp/gradle-9.3.1-bin.zip
/tmp/commandlinetools-linux-15859902_latest.zip
```

并已执行 `apt-get clean` 清理 apt 安装包缓存。

## 重要限制：`/mnt/tool` 不能直接运行 Android SDK

`/mnt/tool` 当前为 NTFS/FUSE 挂载。它会丢失从 ZIP 解压出的可执行权限；例如 SDK 的 `sdkmanager` 和 Build Tools 二进制文件无法在此目录直接执行。

因此：

- Gradle 缓存可长期放在 `/mnt/tool`，并由 `~/.gradle` 软链接自动复用。
- Android SDK 可在 `/mnt/tool` 作为存储副本保留，但构建时必须放到可执行的 Linux 文件系统（例如 `/tmp` 或 ext4 分区）。
- 如要彻底消除根分区的临时 SDK 占用，应将 `/mnt/tool` 改为 ext4 等支持 Unix 可执行权限的挂载，而不是 NTFS/FUSE。

## 下次构建步骤

### 1. 准备临时可执行 SDK

将 SDK 副本复制到 `/tmp`。由于源挂载不保留执行位，需要在复制后恢复权限：

```bash
cp -a /mnt/tool/android-build-cache/android-sdk /tmp/
find /tmp/android-sdk -type f -exec chmod a+rx {} +
```

若 `/tmp/android-sdk` 已存在且不再使用，请先确认路径无误后再删除或替换它。

### 2. 执行 debug 构建

在项目根目录运行：

```bash
export ANDROID_HOME=/tmp/android-sdk
export ANDROID_SDK_ROOT=/tmp/android-sdk
bash /mnt/tool/android-build-cache/gradle-9.3.1/bin/gradle assembleDebug
```

不要直接执行 `./gradlew`：项目目录所在挂载点也可能不允许直接执行脚本。使用 `bash gradlew` 或上面的 Gradle 路径即可。

### 3. 构建后可选清理

若需要再次释放根分区空间，可删除这次临时复制的 SDK：

```bash
rm -rf /tmp/android-sdk
```

这不会影响 `/mnt/tool/android-build-cache/android-sdk` 中的存储副本，也不会影响 Gradle 缓存。

## 本次占用核对

迁移后大致占用如下：

| 位置 | 用途 | 大小 |
| --- | --- | ---: |
| `/mnt/tool/android-build-cache/.gradle` | Gradle 缓存 | 1.4 GB |
| `/mnt/tool/android-build-cache/android-sdk` | Android SDK | 610 MB |
| `/mnt/tool/android-build-cache/gradle-9.3.1` | Gradle 安装 | 146 MB |
| `/usr/lib/jvm/java-21-openjdk-amd64` | JDK 21 | 约 286 MB |

本次迁移和清理后，根分区的已用空间从约 56 GB 降至约 54 GB。
