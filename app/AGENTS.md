# app

本目录是 OpenCode Android Client 的应用模块，负责 Compose 界面、客户端状态、受控本地偏好和对已配置 OpenCode Server 的 REST/SSE 调用；不保存模型凭据，也不配置或运行远端 OpenCode Server。

## 主要边界

- `src/main/`：生产 Kotlin、Compose UI、资源和 Android 配置。
- `src/test/`：确定性离线 JVM 测试；不得触发网络或真实模型推理。
- `src/androidTest/`：Compose 与集成测试，只能针对模拟器执行。

## 模型选择合同

- `ui/ModelPresets.kt` 保留个人预置定义，并只将四个可见候选提供给 UI：`openai/gpt-5.6-sol`、`ds4/deepseek-v4-flash`、`deepseek/deepseek-v4-pro`、`kimi-for-coding/k3`。
- `util/ModelSelectionMigration.kt` 固化旧八项下标到 schema 2 引用的迁移表；不要从当前可见列表的顺序推导旧值。
- `AppState.availableModels` 取该静态白名单与服务端 `GET /config/providers` 的交集；响应缺失或加载失败时列表为空。
- 选择和会话偏好使用 `providerId/modelId` 规范引用。`SettingsManager` 在首次启动时将旧整数下标迁移至 schema 2，不能再按重排后的列表位置解释旧值。
- 若当前服务器不提供已保存的引用，提示请求不得伪造另一模型；请求不带显式模型，由服务器默认值处理。

## 运行与验证

- 离线单元测试：`./gradlew testDebugUnitTest`。
- 覆盖率：`./gradlew koverHtmlReport`。
- 仪器测试：仅在模拟器上显式执行 `./gradlew connectedDebugAndroidTest`；不得安装或启动物理设备上的调试包。
- 服务器端模型验收依次检查 `opencode models kimi-for-coding --refresh` 与实际服务器的 `GET /config/providers`；不要用 Android App 代替服务端合同验证。
- `AIUsageClientTest` 的 `MockWebServer` 必须绑定回环地址并使用 `127.0.0.1` 请求，以便私有 HTTP 策略测试不继承运行环境的主机别名。

修改模型合同、持久化键、UI 可见性或测试入口时，必须在同一次变更中更新本文件及受影响子目录说明。
