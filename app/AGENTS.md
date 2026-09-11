# app

本目录是 OpenCode Android Client 的应用模块，负责 Compose 界面、客户端状态、受控本地偏好和对已配置 OpenCode Server 的 REST/SSE 调用；不保存模型凭据，也不配置或运行远端 OpenCode Server。

## 主要边界

- `src/main/`：生产 Kotlin、Compose UI、资源和 Android 配置。
- `src/test/`：确定性离线 JVM 测试；不得触发网络或真实模型推理。
- `src/androidTest/`：Compose 与集成测试，只能针对模拟器执行。

## 模型选择合同

- `AppState.availableModels` 直接扁平化当前服务器 `GET /config/providers` 返回的有效 models 映射；OpenCode Server 可通过 provider 的 `whitelist` / `blacklist` 先裁剪目录，客户端不得按模型名称或个人预置再次隐藏、添加或猜测。请求 ID 优先取 model 的 `providerID/providerId` 与 `id`，为空才分别回退到父 provider ID 与 models map key。
- 聊天菜单首项“服务端默认”表示 Prompt 不含 `model`；`default` 响应映射可含多个 provider 默认值，不能被移动端当作唯一全局默认。每个显式项显示服务端名称和规范 `providerId/modelId`。
- 选择和会话偏好使用 `providerId/modelId` 规范引用。`SettingsManager` 在首次启动时将旧整数下标迁移至 schema 2 的原始引用，不能再按当前目录顺序解释旧值。
- 若当前服务器不提供已保存的引用，提示请求不得伪造或改写另一模型；保存值保留，本次请求不带显式模型并显示“服务端默认”。

## 运行与验证

- 离线单元测试：`./gradlew testDebugUnitTest`。
- 覆盖率：`./gradlew koverHtmlReport`。
- 仪器测试：仅在模拟器上显式执行 `./gradlew connectedDebugAndroidTest`；不得安装或启动物理设备上的调试包。
- `ChatTopBarInstrumentedTest` 在模拟器上验证“服务端默认”操作和长模型目录滚动；筛选单个类时使用 `-Pandroid.testInstrumentationRunnerArguments.class=<fully-qualified-class>`，`connectedDebugAndroidTest` 不支持 JVM 的 `--tests` 参数。
- `OpenCodeIntegrationTest` 从根目录 `.env` 的 `OPENCODE_SERVER_URL` 读取服务端；无 URL 或健康检查失败时跳过。其目录断言比较去重后的引用集合，验证客户端不因展示排序而遗漏或重复服务端模型；目录、健康和 agent 检查不需要会话凭据。
- 服务器端模型验收以实际服务器的 `GET /config/providers` 为准；不要用 Android App 或本机模型目录代替远端服务合同验证。
- `AIUsageClientTest` 的 `MockWebServer` 必须绑定回环地址并使用 `127.0.0.1` 请求，以便私有 HTTP 策略测试不继承运行环境的主机别名。

修改模型合同、持久化键、UI 可见性或测试入口时，必须在同一次变更中更新本文件及受影响子目录说明。
