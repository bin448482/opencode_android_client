# docs

本目录保存面向本个人 Android fork 的需求、设计和待评审实施计划；不承载 Android 源码、运行产物、凭据或服务端配置。

## 文件索引

- `personal-ui-requirements.md`：个人 UI 精简和模型选择的需求来源；模型目录以当前 OpenCode Server 的运行时响应为准，并定义默认选择和验收边界。
- `PRD.md`：产品需求与范围说明。
- `RFC.md`：客户端架构、状态与协议设计；模型选择章节必须与当前引用式持久化合同一致。
- `design.md`：界面与交互设计记录。
- `working.md`：已实施工作的时间线与历史决策；实现演进时同步修正仍被引用的技术事实。
- `android_build_storage.md`：本机 Android 构建环境、APK 产物位置、构建缓存迁移结果与后续复用步骤；记录环境事实，不承载构建产物。
- `opencode-server-start.md`：通过 Tailscale 启动受 Basic Auth 保护的 OpenCode Server 并连接 Android Client 的本机操作手册；不保存实际凭据。
- `windows-tailscale-opencode-mobile-connection.md`：Windows 上通过 Tailscale MagicDNS 启动受 Basic Auth 保护的 OpenCode Server，并在 Android Client 创建 Direct Profile 的操作手册；不保存任何实际凭据。
- `test.md`、`skill_operate_emulator.md`、`skill_ui_test_tasks.md` 与 `ui_test_prompts/`：测试策略、模拟器操作约束和 UI 验收提示，不是生产源码。


## 维护边界

- 新增、删除或重命名本目录文档时，必须同步更新本索引。
- 实施计划只描述一次性执行路径，不替代源码目录的职责、运行命令和长期维护说明。
- 与代码事实冲突时，先修订需求、RFC 或计划并完成评审；不得把计划中的假设当作已实现事实。
- 验证命令、设备安全限制和模型请求合同的长期说明归入对应源码目录的 `AGENTS.md`；本目录只维护文档索引和各文档的范围。
