# 个人需求文档：Android 客户端界面定制

> 状态：客户端实现、离线验证和本机服务目录端到端验证已完成；实际远端服务的消息提交验收待执行
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

模型菜单不再来自客户端预设。客户端在连接当前 host profile 后读取 `GET /config/providers`，将每个 provider 的 `models` 映射扁平化为可选项：`providerID` / `providerId` 存在时优先使用，否则使用父 provider ID；`model.id` 为空时才使用 models map key。显示名称优先使用服务端 `name`，并始终显示精确 `providerId/modelId`。

因此 GLM、Gemini、DeepSeek、GPT、Kimi 等都不再由 Android 代码按名称隐藏或添加。服务端返回的有效模型都会显示；服务端未返回或缺少有效请求标识的项不会被猜测或伪造。

菜单首项固定为“服务端默认（不指定模型）”。选择它会清除全局和当前会话的显式引用，使 `PromptRequest.model` 为 `null`，由 OpenCode Server 决定实际模型。`default` 响应字段允许多个 provider 默认值，客户端不将其中任一项硬编码为唯一全局默认。

历史引用仍按 `providerId/modelId` 保存。当前服务器不提供已保存引用时，客户端不替换或删除该引用；本次 Prompt 省略模型并在顶栏显示“服务端默认”，切回提供该引用的服务器后可继续使用。模型变体/推理强度仍不由移动端另建字段，除非服务端将其作为独立 model ID 返回。

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
| Model Selector | 展示当前 OpenCode Server 返回的完整有效模型目录和“服务端默认”项 |
| 模型选择持久化 | 以 `providerId/modelId` 保存显式选择；默认项不保存引用 |
| Tests | 覆盖目录扁平化、ID 别名/回退、失效引用和默认选择 |

### 4.2 模型下标迁移要求

`SettingsManager` 与会话偏好使用模型**引用**而非下标：`selected_model_ref` 和 `session_model_refs` 均保存 `providerId/modelId`。schema 1 的旧下标只映射到其原始引用，绝不按当前菜单位置解释；最终能否随 Prompt 发送由当前服务器目录决定。

### 4.3 验证清单

- [x] Settings 页面不显示“语音识别”区块。
- [x] Settings 页面不显示“NFC Quick Prompt”区块。
- [x] Chat 页面不显示 VoiceRail（麦克风、波形、转写状态）。
- [x] 下拉列表显示服务端返回的全部有效模型，而不是个人白名单。
- [x] 菜单首项“服务端默认”会发送不含 `model` 的 Prompt。
- [x] 预置列表移除后，历史会话不会因菜单重排被静默切换到其他模型。
- [ ] 在实际 Android Server 上，确认目录与 `/config/providers` 一致，并选择服务端实际返回的模型发送非敏感消息。
- [x] 相关离线单元测试通过。
- [x] 模型菜单 Compose/instrumented 测试已在 `Pixel_6` 模拟器执行：验证“服务端默认”及 32 个动态模型的滚动与选择。

## 5. 实施结果与服务端验收

1. **已完成客户端改动**：文件内常量默认隐藏语音、NFC 和 VoiceRail；模型菜单按服务端目录动态生成，引用式选择和默认回退均有离线测试。
2. **已确认接口行为**：本机临时 OpenCode Server 的 `/config/providers` 返回 provider/models 目录与 provider 默认映射；`Pixel_6` 模拟器经 `adb reverse` 的真实 HTTP 测试确认，客户端目录的去重引用集合与该响应完全一致。这不是手机远端服务器的替代证明。
3. **待完成服务端验收**：在 Android App 实际连接的 OpenCode Server 上确认下拉目录与接口响应一致。
4. **待完成模拟器端到端验证**：仅在模拟器和显式指定的测试服务器上，选择“服务端默认”和实际目录模型各发送一条非敏感消息。

## 6. 已确认事项与剩余问题

1. **模型目录合同已确认**：以当前 OpenCode Server 的 `/config/providers` 为唯一运行时权威。
2. **变体与模式 UI 不属于本次范围**：移动端不新增变体/模式字段；服务器将其作为独立 model ID 返回时会自然显示。
3. 是否需要保留一个“恢复默认”的开关，以临时打开语音或实验功能？
4. 若未来引入模型 variant/mode，是否需要将模型选择持久化从下标迁移为 `providerId/modelId/variant` 结构？

## 7. 推荐下一步

在运行 OpenCode Server 的主机上启动实际服务，保存 `/config/providers` 的非敏感目录摘要，并在模拟器中确认该目录完整出现；分别验证“服务端默认”和任意实际返回模型的消息记录。

---

## 附录 A：OpenCode 模型配置机制（已核对）

### A.1 客户端与服务端的职责

Android Client 的模型菜单直接由 `GET /config/providers` 动态生成。客户端将每个有效 provider/model 项的精确标识原样用于 Prompt；不使用固定预置或名称猜测。

因此：

- `/config/providers` 是“当前 Android 要连接的服务端”的模型目录运行时权威。
- `providers[].models` 中每个具备有效 `providerId/modelId` 的项都会显示；接口未返回或标识不完整的项不会显示。
- “服务端默认”不指定模型，`default` 映射仅说明各 provider 的默认项，不能被客户端推断为唯一全局默认。
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

### A.3 运行时模型合同

| 用途 | Provider ID | Model ID | 备注 |
|------|-------------|----------|------|
| 显式模型选择 | `ProviderModel.providerID/providerId`，否则父 provider ID | `ProviderModel.id`，否则 models map key | 仅使用当前接口项的精确标识。 |
| 服务端默认 | 不发送 | 不发送 | `PromptRequest.model == null`，由服务器决定实际模型。 |

### A.4 变体与模式边界

现有 Android `PromptRequest` 只发送 provider/model，不新增 variant 或 mode 字段。服务端若将某种运行配置以独立 model ID 发布，客户端会将其作为普通目录项显示；否则配置仍完全由服务端处理。

---

## 附录 B：参考文件与权威来源

**本仓库**

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
