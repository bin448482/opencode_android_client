# 文件浏览路径复制实施计划

## 关联需求

[`file-browser-copy-requirements.md`](file-browser-copy-requirements.md)

## 实施范围

- 在文件浏览列表中，为目录和文件条目增加长按复制完整相对路径。
- 为非根目录的文件页标题增加长按复制当前目录路径。
- 复制成功后显示短暂提示。
- 保持目录和文件的短按既有行为不变。

不修改服务端 API、`FilesViewModel` 的文件加载逻辑、文件状态逻辑或文件预览逻辑。

## 当前实现事实

- `ui/files/FileBrowserPane.kt` 中的 `FileRow` 使用 `Modifier.clickable` 处理短按，显示名称来自 `FileNode.name`，完整路径来自 `FileNode.path`。
- `ui/files/FilesScreen.kt` 中的 `TopAppBar` 将 `FilesUiState.currentPath` 作为非根目录标题。
- 工程既有复制实现使用 Compose 的 `LocalClipboardManager` 和 `AnnotatedString`，例如 `ui/settings/SettingsScreen.kt`。
- 文件浏览的 UI 状态与导航逻辑由 `ui/files/FilesViewModel.kt` 管理；复制仅是界面副作用，不应写入该状态层。

## 实施 DAG

1. 扩展列表条目交互
   - 输入：`FileNode.path`、既有 `onFileSelected` 回调。
   - 输出：`FileRow` 支持 `onLongClick`，短按继续调用原 `onClick`。
   - 涉及文件：`ui/files/FileBrowserPane.kt`。
   - 实现：将仅支持短按的 `clickable` 替换为同时支持短按和长按的 Compose 点击修饰符；长按回调将完整 `FileNode.path` 交给上层复制。
   - 风险：长按手势不得触发短按导航或预览。
   - 验证：为目录和文件分别断言长按后只复制路径，不调用短按回调。

2. 在文件页接入剪贴板和复制反馈
   - 输入：列表条目完整路径、当前目录路径。
   - 输出：路径写入系统剪贴板，用户可见复制成功提示。
   - 依赖：步骤 1。
   - 涉及文件：`ui/files/FilesScreen.kt`。
   - 实现：通过 `LocalClipboardManager` 写入 `AnnotatedString(path)`；使用当前页面已有 Material 组件显示短暂反馈。仅当 `currentPath` 非空时，让标题支持长按复制。
   - 风险：根目录标题显示的是本地化“文件”标签，不能作为路径写入剪贴板。
   - 验证：分别长按文件、目录和非根目录标题，读取剪贴板并比对完整路径；根目录标题不应提供复制动作。

3. 补充测试与回归验证
   - 输入：步骤 1 和步骤 2 的 UI 行为。
   - 输出：覆盖复制值、反馈和短按不回归的模拟器 UI 测试。
   - 依赖：步骤 1、步骤 2。
   - 涉及文件：新增或扩展 `app/src/androidTest/java/com/yage/opencode_client/` 下的文件浏览测试。
   - 实现：使用可控的 `FileNode` 数据渲染文件浏览界面；对每个条目执行 Compose 长按并读取 Android 剪贴板服务，验证复制内容。保留短按目录和文件的断言。
   - 风险：系统 Toast 不稳定且不适合断言；复制反馈应使用可通过 Compose 语义定位的页面内组件。
   - 验证：只在模拟器上运行目标仪器测试；离线单元测试验证无回归。

## 验收命令

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.yage.opencode_client.FileBrowserCopyInstrumentedTest
```

第二条命令仅可在 Android 模拟器上执行，且应显式指定模拟器序列号，避免对物理设备安装或运行测试包。

## 完成条件

1. 文件和目录条目长按后，剪贴板内容与对应 `FileNode.path` 完全一致。
2. 非根目录标题长按后，剪贴板内容与 `currentPath` 完全一致。
3. 每次复制后出现可见确认反馈。
4. 文件短按仍打开预览，目录短按仍进入目录。
5. 全量离线单元测试和目标模拟器仪器测试通过。
