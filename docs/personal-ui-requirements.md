# 个人需求文档：Android 客户端界面定制

> 状态：客户端实现与离线验证已完成；服务端端到端验收待执行
> 创建日期：2026-07-22
> 适用分支：`personal-ui`
> 范围：OpenCode Android Client（个人 fork）

---

## 1. 需求概述

本需求为个人使用场景下的界面精简与模型列表调整。所有改动均为**隐藏/屏蔽**，不删除原有代码，便于后续恢复或合并上游更新。

---

## 2. 需求明细

### 2.1 设置页隐藏语音识别入口

- **目标**：在 `SettingsScreen` 中隐藏“语音识别”配置区块（`SpeechRecognitionSection`）。
- **约束**：
  - 不删除 `SpeechRecognitionSection` 组件本身。
  - 不删除相关的 ViewModel 状态、存储逻辑或字符串资源。
  - 通过条件渲染控制：在 `SettingsScreen.kt` 中调用 `SpeechRecognitionSection(...)` 处增加可见性开关。
- **建议实现**：在 `AppState` 或 `SettingsManager` 中增加一个 feature flag（如 `showSpeechSettings: Boolean = false`），由 SettingsScreen 读取并决定是否渲染。

**涉及文件**：

- `app/src/main/java/com/yage/opencode_client/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/yage/opencode_client/ui/MainViewModel.kt`（如需新增 AppState 字段）

---

### 2.2 设置页隐藏实验功能入口

- **目标**：在 `SettingsScreen` 中隐藏“NFC Quick Prompt”实验功能区块（`NfcExperimentalSection`）。
- **约束**：
  - 不删除 `NfcExperimentalSection` 组件本身。
  - 不删除 NFC 相关的 Activity、ViewModel 逻辑或存储。
  - 通过条件渲染控制：在 `SettingsScreen.kt` 中调用 `NfcExperimentalSection(...)` 处增加可见性开关。
- **建议实现**：与 2.1 共用同一个 feature flag 机制（如 `showExperimentalSettings: Boolean = false`）。

**涉及文件**：

- `app/src/main/java/com/yage/opencode_client/ui/settings/SettingsScreen.kt`

---

### 2.3 聊天窗口隐藏语音识别 UI

- **目标**：在聊天输入区域隐藏整个语音输入条（`VoiceRail`），包括麦克风按钮、波形图、转写状态提示等。
- **约束**：
  - 不删除 `ChatInputBar.kt` 中的 `VoiceRail` 及其子组件。
  - 不删除 `MainViewModelSpeechActions.kt` 等语音业务逻辑。
  - 仅通过条件渲染屏蔽 `VoiceRail(...)` 的显示。
- **建议实现**：
  - 在 `ChatInputBar` 的参数中增加 `showVoiceInput: Boolean = false`。
  - 在 `VoiceRail(...)` 调用处判断：若 `showVoiceInput == false`，则整个 `VoiceRail` 不渲染。
  - 调用方（`ChatScreen.kt` 或 `MainViewModel`）根据同一个 feature flag 传入该值。

**涉及文件**：

- `app/src/main/java/com/yage/opencode_client/ui/chat/ChatInputBar.kt`
- `app/src/main/java/com/yage/opencode_client/ui/chat/ChatScreen.kt`（调用 `ChatInputBar` 的位置）

---

### 2.4 聊天窗口模型下拉列表调整

当前模型预置列表位于 `ModelPresets.kt`：

```kotlin
object ModelPresets {
    val list: List<AppState.ModelOption> = listOf(
        AppState.ModelOption("GLM-5.2", "zai-coding-plan", "glm-5.2"),
        AppState.ModelOption("GPT-5.6 Sol", "openai", "gpt-5.6-sol"),
        AppState.ModelOption("Gemini 3.5 Flash", "google", "gemini-3.5-flash"),
        AppState.ModelOption("DeepSeek Local", "ds4", "deepseek-v4-flash"),
        AppState.ModelOption("DeepSeek V4 Pro", "deepseek", "deepseek-v4-pro"),
        AppState.ModelOption("Ollama GLM 5.2", "ollama-cloud", "glm-5.2"),
        AppState.ModelOption("GPT-5.6 Sol Pro", "openai", "gpt-5.6-sol-pro"),
        AppState.ModelOption("GPT-5.6 Sol Fast", "openai", "gpt-5.6-sol-fast"),
    )
}
```

#### 2.4.1 移除指定模型

从下拉列表中移除以下模型（不删除代码，可通过注释或条件过滤实现）：

| 显示名称 | Provider ID | Model ID | 移除原因 |
|---------|------------|----------|---------|
| Gemini 3.5 Flash | `google` | `gemini-3.5-flash` | 按需求移除 |
| Ollama GLM 5.2 | `ollama-cloud` | `glm-5.2` | 按需求移除 |
| GLM-5.2 | `zai-coding-plan` | `glm-5.2` | 按需求移除 |

#### 2.4.2 添加 Kimi K3

新增模型的合同已完成目录级验证：

| 显示名称 | Provider ID | Model ID | 验证依据 |
|---------|-------------|----------|----------|
| Kimi K3 | `kimi-for-coding` | `k3` | OpenCode 使用的 Models.dev 当前将 `k3` 登记在 `kimi-for-coding` provider 下，底层模型为 `moonshotai/kimi-k3`。 |

因此 Android 预置项应为：

```kotlin
AppState.ModelOption("Kimi K3", "kimi-for-coding", "k3")
```

**不得使用** `ollama-cloud/kimi-k3`、`moonshot/kimi-k3`、`kimi/k3` 或其他占位值。客户端会把 `providerID` 和 `modelID` 原样提交给 OpenCode Server；未注册组合必须被视为不可用，而不是先写入 UI 后再尝试。

**服务端配置前提**

本机已存在 `kimi-for-coding` 认证记录，但 OpenCode 的全局配置尚未设置默认模型。应在运行 `opencode serve` 的主机上，将以下字段合并到全局 `opencode.json` 中，保留已有的 `mcp` 等配置，不得覆盖整个文件：

```json
{
  "$schema": "https://opencode.ai/config.json",
  "model": "kimi-for-coding/k3"
}
```

Windows 的全局配置位置为 `%USERPROFILE%\.config\opencode\opencode.json`。认证与模型配置是两件事：认证凭据由 `opencode providers login`（或 TUI 的 `/connect`）管理，`model` 指定默认的 `provider/model` 组合。

**运行时验收**

在实际供 Android App 连接的服务器上：

1. 执行 `opencode models kimi-for-coding --refresh`，确认列表包含 `k3`。
2. 启动 `opencode serve --port 4096` 后，调用 `GET /config/providers`，确认返回中存在 `providerID = kimi-for-coding` 与 `modelID = k3`。
3. 仅在以上两项都通过后，将 Kimi K3 放入 Android 的 `ModelPresets`，并用 App 创建会话、发送一条非敏感测试消息验证端到端请求。

#### 2.4.3 GPT-5.6 Sol 配置结论

当前 OpenCode Models.dev 注册的基础模型是：

| 显示名称 | Provider ID | Model ID | 状态 |
|---------|-------------|----------|------|
| GPT-5.6 Sol | `openai` | `gpt-5.6-sol` | 有效基础模型 |

`gpt-5.6-sol-pro` 与 `gpt-5.6-sol-fast` **不是独立模型 ID**，不得继续作为 `ModelPresets` 中的 `modelId`。`gpt-5.6-sol` 的 `medium`、`high` 等属于 reasoning variant；`pro`、`fast` 属于该基础模型的模式配置，而不是可通过改显示名称获得的模型。

现有 Android 请求合同仅发送 `providerID` 与 `modelID`，未发送 OpenCode variant 或 mode。因此本次个人 UI 定制中：

- GPT 下拉列表最多保留一个 `GPT-5.6 Sol / openai / gpt-5.6-sol` 条目。
- 不将 `Medium`、`High`、`Fast`、`Pro` 作为独立 GPT 模型展示。
- 不需要为本次变更修改 `ModelOption.shortName` 的 GPT 专用映射；其通用 `GPT` 回退显示已足够。
- 若未来确实需要切换 reasoning variant 或 experimental mode，必须先冻结移动端 REST 请求合同、扩展 `ModelOption` 与 `PromptRequest`、补充序列化和端到端测试；这不是本需求中的“改显示名称”。

OpenAI 目前未在本机认证记录中出现。因此即使 Android 保留上述基础模型，也只能在运行服务端完成 OpenAI 认证、`/config/providers` 返回该模型后才可显示或验收。

## 3. 建议的 feature flag 设计

为便于统一管理“隐藏但不删除”的界面元素，建议使用文件内私有常量：

```kotlin
private const val SHOW_SPEECH_SETTINGS = false
private const val SHOW_EXPERIMENTAL_SETTINGS = false
private const val SHOW_VOICE_INPUT = false
```

| 方式 | 优点 | 缺点 |
|------|------|------|
| 文件内常量 | 改动最小，不引入持久化或状态管理 | 需改代码才能切换 |
| AppState / SettingsManager 字段 | 可运行时切换 | 本需求不需要，会扩大状态管理范围 |

**推荐**：使用文件内常量。只有确有“恢复默认”需求时，才单独设计可配置开关。

## 4. 影响范围与验证清单

### 4.1 影响范围

| 模块 | 影响内容 |
|------|---------|
| Settings UI | 隐藏语音识别、NFC 实验功能两个区块 |
| Chat Input UI | 隐藏 VoiceRail（麦克风、波形、转写状态） |
| Model Selector | 移除指定预置项、添加已验证的 Kimi K3；GPT 仅保留有效基础模型 |
| 模型选择持久化 | 预置列表重排后迁移或重置全局与会话级模型下标 |
| Tests | 更新相关 UI 测试、`ModelTests` 与模型下标迁移测试 |

### 4.2 模型下标迁移要求

`SettingsManager` 与会话偏好当前保存的是模型**下标**，不是 `providerId/modelId`。删除或重排 `ModelPresets` 后，旧下标可能指向错误模型。

实施时必须在同一次变更中完成以下之一：

- 将已有下标映射到新的等价 `providerId/modelId`；或
- 清除旧的全局和会话级模型下标，并设置一个经过服务端验证的默认模型。

不得只依赖现有的越界裁剪逻辑；它只能处理超出范围，不能识别“同一数值已指向另一模型”。

### 4.3 验证清单

- [x] Settings 页面不显示“语音识别”区块。
- [x] Settings 页面不显示“NFC Quick Prompt”区块。
- [x] Chat 页面不显示 VoiceRail（麦克风、波形、转写状态）。
- [x] 下拉列表中不显示 Gemini、Ollama GLM、GLM-5.2。
- [ ] 在服务端 `/config/providers` 已确认的前提下，下拉列表显示 Kimi K3（`kimi-for-coding/k3`）。
- [x] 若 OpenAI 未在服务端返回，不显示 GPT-5.6 Sol；若已返回，则只显示一个 `openai/gpt-5.6-sol` 条目。
- [x] 预置列表变更后，历史会话不会因旧下标被静默切换到其他模型。
- [ ] 选择 Kimi K3 后能正常发送消息并收到回复。
- [x] 相关离线单元测试通过。
- [ ] UI/instrumented 测试仅在模拟器运行，尚未执行。

## 5. 实施结果与服务端验收

1. **已完成客户端改动**：文件内常量默认隐藏语音、NFC 和 VoiceRail；静态白名单、引用式迁移和服务端交集均已由离线单元测试覆盖。
2. **已确认 CLI 模型目录**：`opencode models kimi-for-coding --refresh` 返回 `kimi-for-coding/k3`。
3. **待完成服务端验收**：在 Android App 实际连接的 OpenCode Server 上验证 `/config/providers`，仅当它返回对应 `providerId/modelId` 时模型才会显示。
4. **待完成模拟器端到端验证**：仅在模拟器和显式指定的测试服务器上，分别选择每个可见模型并收发一条非敏感测试消息。

## 6. 已确认事项与剩余问题

1. **Kimi K3 合同已确认**：`kimi-for-coding/k3`。剩余的是运行该 Android App 所连接服务器的实际可用性验收。
2. **GPT 三档 UI 不属于本次范围**：现有客户端不支持变体/模式选择；基础模型 `openai/gpt-5.6-sol` 是否可显示取决于服务端的 OpenAI 认证与返回。
3. 是否需要保留一个“恢复默认”的开关，以临时打开语音或实验功能？
4. 若未来引入模型 variant/mode，是否需要将模型选择持久化从下标迁移为 `providerId/modelId/variant` 结构？

## 7. 推荐下一步

在运行 OpenCode Server 的主机上启动实际服务，检查 `/config/providers` 是否返回 `kimi-for-coding/k3` 与已认证的 OpenAI 模型；随后仅用模拟器完成端到端消息验收。客户端不会在该接口缺失模型时显示或发送该模型。

---

## 附录 A：OpenCode 模型配置机制（已核对）

### A.1 客户端与服务端的职责

Android Client 的模型菜单来自 `ModelPresets.kt`，不会依据服务端列表动态生成。客户端会请求 `GET /config/providers` 用于服务端 provider/model 元数据，但发送消息时仍由预置项决定 `providerID` 和 `modelID`。

因此：

- `/config/providers` 是“当前 Android 要连接的服务端”是否允许某个预置项的运行时权威。
- `ModelPresets.kt` 是 Android UI 的静态白名单。
- 两者必须取交集；服务端未返回的预置项不得显示为可用。
- `GET /provider` 可用于诊断 provider 总览，但不能替代 `/config/providers` 对当前服务端配置的确认。

### A.2 OpenCode 配置层级

OpenCode 支持 JSON/JSONC 配置，并按远端组织配置、全局配置、环境变量指定配置、项目配置等层级合并。个人服务器的模型默认值通常写入全局 `~/.config/opencode/opencode.json`：

```json
{
  "$schema": "https://opencode.ai/config.json",
  "model": "kimi-for-coding/k3"
}
```

`model` 的格式固定为 `provider_id/model_id`。认证凭据不应写入项目仓库或 Android 客户端；使用 OpenCode 的 provider 登录流程保存到本机认证存储。

### A.3 已验证的模型合同

| 用途 | Provider ID | Model ID | 备注 |
|------|-------------|----------|------|
| Kimi K3 | `kimi-for-coding` | `k3` | 当前 Models.dev 的 Kimi For Coding provider 已登记该模型。 |
| GPT-5.6 Sol 基础模型 | `openai` | `gpt-5.6-sol` | reasoning variant 和 experimental mode 不是独立的 model ID。 |

### A.4 变体与模式边界

OpenCode 将 reasoning variant 作为基础模型的运行配置。例如 `gpt-5.6-sol` 可使用 `medium` 或 `high` reasoning effort；这与另一个 `modelID` 不同。现有 Android `PromptRequest` 没有 variant/mode 字段，所以不能把这些配置伪装成 `gpt-5.6-sol-medium`、`gpt-5.6-sol-pro` 或 `gpt-5.6-sol-fast`。

新增该能力时，必须先定义移动端请求字段、服务端兼容版本、持久化格式、回退语义和测试，再修改 UI。

---

## 附录 B：参考文件与权威来源

**本仓库**

- `app/src/main/java/com/yage/opencode_client/ui/ModelPresets.kt`
- `app/src/main/java/com/yage/opencode_client/ui/MainViewModel.kt`
- `app/src/main/java/com/yage/opencode_client/ui/MainViewModelSessionActions.kt`
- `app/src/main/java/com/yage/opencode_client/data/api/OpenCodeApi.kt`
- `app/src/main/java/com/yage/opencode_client/util/SettingsManager.kt`
- `app/src/main/java/com/yage/opencode_client/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/yage/opencode_client/ui/settings/SettingsSections.kt`
- `app/src/main/java/com/yage/opencode_client/ui/chat/ChatInputBar.kt`
- `app/src/main/java/com/yage/opencode_client/ui/chat/ChatTopBar.kt`

**外部权威来源**

- `https://opencode.ai/docs/config`
- `https://opencode.ai/docs/models`
- `https://github.com/anomalyco/models.dev/blob/dev/providers/kimi-for-coding/provider.toml`
- `https://github.com/anomalyco/models.dev/blob/dev/providers/kimi-for-coding/models/k3.toml`
- `https://github.com/anomalyco/models.dev/blob/dev/providers/openai/models/gpt-5.6-sol.toml`
