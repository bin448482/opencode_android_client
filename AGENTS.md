# AGENTS.md - opencode_android_client

## Top-level modules

- `app/` contains the Android client source, resources, and its offline/instrumented tests; see `app/AGENTS.md` for module-local model, test, and device-safety facts.
- `docs/` contains product, architecture, design and test documents, personal requirements, and temporary reviewable implementation plans; see `docs/AGENTS.md` for its local document lifecycle.

## Build Environment

终端默认可能找不到 Java，导致 `./gradlew` 失败。使用 Android Studio 自带的 JDK：

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# For integration tests (adb)
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"
```

**持久化**：在 `~/.zshrc` 中加入上述 `JAVA_HOME` 和 `PATH` 两行，然后 `source ~/.zshrc`。

## Run / Module not found

若 Run 报 "Module not found"：File → Sync Project with Gradle Files；若仍失败，File → Invalidate Caches / Restart。Run 配置使用 module `opencode_client.app`（对应 settings.gradle.kts 的 rootProject.name + `:app`）。

## Test Commands

- Unit tests: `./gradlew testDebugUnitTest`
- Coverage report: `./gradlew koverHtmlReport` → `app/build/reports/kover/html/index.html`
- Integration tests: `./gradlew connectedDebugAndroidTest` (requires .env with OPENCODE_* credentials)

## Device Safety

- Do not run `connectedDebugAndroidTest`, install, or launch debug builds on a physical Android phone unless explicitly asked. Physical devices may contain the user's active app settings and credentials; installing test builds can overwrite them.
- For UI/instrumented tests, use an emulator only. If both emulator and physical devices are connected, target the emulator explicitly with `ANDROID_SERIAL=<emulator-id>` or an equivalent Gradle/adb device selection.

## 通用规范

### 目录治理

- 每个已有的代码、文档或 Skill 定义目录都必须包含 `AGENTS.MD` 和 `CLAUDE.MD`。
- 每个 `CLAUDE.MD` 必须且只能包含 `@AGENTS.MD`。
- 子目录的 `AGENTS.MD` 是该目录的活文档：不得重复根目录通用规则，必须准确说明本目录的职责与不负责的边界、主要功能或业务逻辑、子目录和关键文件作用、输入输出与上下游依赖、适用的数据合同、运行入口、验证方式及维护注意事项。
- 根目录 `AGENTS.MD` 只维护全局规则、顶层模块概览和跨目录边界；子目录 `AGENTS.MD` 只维护本地事实。父目录应说明子模块的组合关系，但不得替代子目录的本地说明。
- 新增、删除、重命名或移动文件、子目录时，必须同步更新直接父目录的 `AGENTS.MD`；若改变模块归属、调用关系或顶层结构，还必须同步更新受影响祖先目录的说明。
- 任何影响目录职责、业务规则、数据合同、调用链、文件结构、运行入口或验证方式的修改，必须在同一次变更中同步更新受影响目录的 `AGENTS.MD`；实现与 `AGENTS.MD` 描述不一致，视为变更未完成。
- 当一个目录同时承载多个可独立演进的职责，或其 `AGENTS.MD` 已无法在一个层级内清晰说明时，必须先进行目录拆分与职责抽象，再补充说明；不得仅靠持续扩写 `AGENTS.MD` 掩盖职责混杂。
- 出现以下任一信号时必须评估拆分：`AGENTS.MD` 超过约 1000 行；存在三个及以上独立业务子域；同目录文件具有不同输入输出合同；或一次需求修改通常只影响其中少数文件而难以定位。
- 拆分后的每个子目录必须具有单一清晰职责、独立的本地 `AGENTS.MD` 与 `CLAUDE.MD`；目录治理的验收标准是：从任意目录的 `AGENTS.MD` 可以判断该目录做什么、依赖什么、改动后应验证什么，并定位到对应实现。
- `DATA/` 是本地不可变证据和可重建投影的根目录，顶层 `AGENTS.MD` 与 `CLAUDE.MD` 必须受版本控制；`RAW/`、`NORMALIZED/`、`RUNS/`、`BACKUPS/`、`DERIVED/` 与 `V2/` 下按日期、运行号或合同类型生成的证据子树不是独立代码或文档目录，不逐层创建治理文件。它们的职责、写入规则和查询边界统一由 `DATA/AGENTS.MD` 说明。

### 数据合同标准

- 数据合同是项目内的数据接口标准：它定义模块间交换、持久化、重建和查询所需的合同名称、版本、字段、枚举和不变量；不代表、不判断也不授予任何外部数据源的授权、许可或访问权。
- 每个项目必须指定唯一的合同权威位置及版本策略。数据生产模块在输出或落盘前必须按该合同验证；数据消费模块在使用数据前必须确认所需合同版本、必填字段和状态语义均满足。
- 合同不兼容、字段不足、状态未知或证据缺失时，模块必须显式拒绝或报告不可用；不得用展示文本、猜测值、默认值或其他模块的内部结构替代。
- 领域对象只表达业务不变量，并通过受控工厂或合同反序列化构建、以合同序列化输出；领域对象不得直接执行网络、数据库、文件、调度或自保存操作。仓储应只提供合同定义的写入和读取边界，不得以通用更新或删除破坏事实历史。
- 原始证据、已验证的规范化合同和可重建查询投影必须分层保存；查询投影不是唯一事实副本，必须能够从已验证的权威合同重建。
- 对由多个固定组成部分构成的输入，合同必须定义完整性、可选性和缺失状态。缺失、未捕获、来源不可用和业务上不存在必须使用可区分的事实状态及原因记录，不能伪造数据；完整性应作为可查询的质量事实，而不是隐式丢弃整批数据的理由。
- 自动化测试必须验证合同、领域不变量、序列化/反序列化和仓储边界，并保持确定性和离线；网络采集应由显式、受控的应用入口承担，不能隐藏在测试、领域对象或数据消费者中。

### 协作与实施

#### 编码前先思考

**不要臆测。不要掩盖困惑。明确说明权衡。**

- 明确说明你的假设；不确定时应提问。
- 存在多种解释时，应列出它们，不得静默选择其中一种。
- 有更简单的方案时，应指出；必要时应提出异议。
- 有不清楚之处时，停止执行，说明困惑点并提问。

#### 代码与 Skill 的决策边界

- 只有目标、输入、输出、业务规则、失败语义和验证方式均明确且稳定的任务，才允许用项目代码实现。
- 需求存在语义判断、名称理解、来源选择、开放式研究、规则尚未冻结或其他不明确因素时，必须由 Skill 完成；不得用 LLM 配置、模型客户端或提示词代码替代 Skill。
- 代码只能接收经 Skill 处理后、满足既定合同的确定性输入，并负责验证、持久化和查询；代码不得读取 LLM 凭据、调用模型或隐式执行 Skill。

#### 保持简单

**只写解决问题所需的最少代码，不做推测性实现。**

- 不得实现请求范围之外的功能。
- 单次使用的逻辑不得建立抽象。
- 不得加入未被请求的“灵活性”或“可配置性”。
- 不得为不可能发生的场景增加错误处理。
- 若写了 200 行代码而 50 行即可完成，应重写为更简单的版本。

应自问：“资深工程师会认为这过度复杂吗？”若答案为是，应简化。

#### 尊重现有结构

- 复用现有辅助函数和分层，不得在新的入口中重建已有逻辑。
- 遵循现有代码风格，即使个人偏好不同。
- 发现无关的死代码时，只说明，不得删除。

#### 精准修改

**只修改必要内容，只清理由自己造成的遗留内容。**

修改已有代码时：

- 不得“顺手改进”相邻代码、注释或格式。
- 不得重构未损坏的内容。

若你的改动产生孤儿内容：

- 删除由你的改动造成且不再使用的导入、变量和函数。
- 未经请求，不得删除既有死代码。

检验标准：每一处修改都必须能直接追溯到用户请求。

#### 执行规划与并行

- 编辑前，将非简单请求转换为 DAG；每个工作项必须包含输入、输出、依赖、涉及的文件或模块、风险和验证方式。
- 可并行时，先并行进行只读探索：代码搜索、文件读取、测试发现、配置发现和调用链检查。
- 不得并行修改同一文件、共享合同、共享状态、不稳定接口或线上业务数据。
- 实施前冻结核心合同：API 形状、Schema、状态转换、工作流语义和公共入口。
- 任务较大时分批执行：并行探索 -> 串行确定合同 -> 并行实现相互隔离的部分 -> 串行集成 -> 串行验证。
- 每个有意义的批次后，报告已完成工作、新发现的冲突或依赖，以及下一批工作。
- 出现冲突时，自动从并行降级为串行，不得强行并行。
- 所需输入未明确或工作流状态不清楚时，应在 `blocked` 或正确的等待点停止，不得猜测。
- 环境共享、测试不稳定或已知会残留进程时，优先串行执行测试。

#### 目标驱动执行

**定义成功标准，循环执行直至验证通过。**

将任务转换为可验证目标：

- “增加校验” -> “先为无效输入编写测试，再使测试通过”
- “修复缺陷” -> “先编写可复现缺陷的测试，再使测试通过”
- “重构 X” -> “确保重构前后测试均通过”

多步骤任务应给出简要计划：

```text
1. [步骤] -> 验证：[检查]
2. [步骤] -> 验证：[检查]
3. [步骤] -> 验证：[检查]
```

强成功标准能够支持独立推进；弱成功标准（例如“让它能工作”）会持续需要澄清。

#### 实施收尾

- 完成并验证实施文档中的工作后，在交付或提交前复核该文档。将其中可长期保留、与代码相关的规则、所有权边界、命令和验证预期，写入相关父目录及子目录的 `AGENTS.MD` 索引，使其与已实现的代码保持一致。
- 完成复核和索引更新后，删除已完成的实施文档，并从对应文档目录的 `AGENTS.MD` 中移除对应条目。设计、需求和策略文档应与操作性源码说明和代码摘要分开存放，后者归入相应的 `AGENTS.MD`。
- 不得删除仍未完成、受阻、等待审批，或仍是规范设计/需求来源的实施文档。不可变的运行产物、审计记录和报告不属于实施文档。
