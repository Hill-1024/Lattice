# Lattice 安全扫描与死代码记录索引

2026-09-13，扫描 ID：`72c01cd0-86be-4569-8841-af17fd12e43a`。Codex Security 已验证并索引完成，记录两个中危生产风险；扫描当时尚未修复。当前修复状态见[归并报告](security-audit-2026-09-13-pr.zh-CN.md)。

- [自动生成的安全报告](security-audit-2026-09-13-rescan/report.md)：证据、攻击条件、修复方案和回归验证建议。
- [死代码与未接入功能清单](security-audit-2026-09-13-rescan/dead-code-review.md)：确定的私有死代码、恒假分支、测试专用代码、51 个孤立 JNI 导出及休眠 NBT UAF。
- 原始规范文件：[扫描清单](security-audit-2026-09-13-rescan/scan-manifest.json)、[漏洞](security-audit-2026-09-13-rescan/findings.json)、[覆盖范围](security-audit-2026-09-13-rescan/coverage.json)、[SARIF](security-audit-2026-09-13-rescan/exports/results.sarif)。

完整审阅 282/478 个本地源码、补丁和构建文件；196 个仅局部阅读或搜索。未运行服务器、测试、攻击复现、sanitizer 或联网 CVE 比对。源码与已有未提交修复均未改动。扫描完成不等于全部源码已逐行审计。

详细产物目录按当前 `.gitignore` 规则被忽略；该索引可正常纳入版本管理。详细文件已在本地保留，内容与完成扫描后的原件逐字节一致。若要连同仓库提交，应显式纳入该产物目录。本次没有暂存、提交、推送或向外部维护者发送消息。

工具返回的计量（`codex_rollout`，8 个任务，coverage=complete）：totalTokens=28,574,670；inputTokens=28,483,433；cachedInputTokens=27,271,936；outputTokens=91,237；reasoningOutputTokens=8,438。这是工具汇总的含重复上下文/缓存的 token 统计，不是费用或独立新生成 token 数。计量的 complete 不代表源码审阅覆盖完整。

## PR 交付补充（2026-09-13）

本次提交已显式纳入上述详细产物目录，保留扫描原件，保证仓库内的报告链接可用。扫描阶段未运行测试的说明仅描述当时的扫描；提交前新执行的验证及当前修复状态见[中文 PR 修复报告](security-audit-2026-09-13-pr.zh-CN.md)。
