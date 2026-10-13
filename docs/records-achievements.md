# Records & Achievements: local implementation

本地实现覆盖团队 3.1–3.7，并接入团队主线 `1eac70c` 的 Campaign、Settings 和音效代码。正式验收条件仍以 [`teams/Chinese_can_fly.md`](../teams/Chinese_can_fly.md) 为准。

## 使用方式

- 标题页用上下方向键选择 **Records & Achievements**，空格打开。
- 页面用左右方向键切换 **Run Records** 和 **Achievements**；Esc 或空格返回。
- 游戏中解锁成就时，HUD 顶部会显示约三秒的提示；结算页会列出本局解锁的全部成就。
- 档案保存在用户目录 `.space-invaders/records-achievements.properties`。记录和成就解锁时间共同保存，个人最佳由完整记录重新计算。

## 本地数据保护

档案使用带版本号的 UTF-8 Properties 文件。写入在单独线程执行：先写临时文件并重新读取校验，再保留上一个有效备份并替换主档案。读取损坏档案时会保留损坏副本并尝试恢复备份；主档案和备份都不可读时，程序报告问题并禁止自动写入，避免把原文件覆盖为空档案。

玩家在中途退出时不产生 completed-run 记录；已经解锁的成就会立即排队保存。完成一局后异步保存，不阻塞结算页；菜单退出、窗口关闭和 Alt+F4 都会等待后台保存队列，最多十秒，超时或失败会报告错误。强制终止进程或断电无法保证尚未写入的进度。

从备份恢复时，页面和日志会提示恢复结果及新进度可能丢失。备份更新也通过临时文件校验后替换；备份无法更新时停止本次写入并保留原来的有效主档案。

## 当前游戏接线与协作接口

团队主线的普通敌人和特殊敌人通过 `EnemyDefeatedEvent` 与公共 `GameplayEventPublisher` 接入 3.5；同一敌人的事件 ID 保持稳定；`RecordsService.enemyDefeated(runId, eventId)` 接受稳定事件 ID，并忽略重复事件。新敌人、Boss、激光等新增击败路径仍需调用这个接口，直到对方系统提供共同的 defeat event。

Records 页面已从本地标题菜单打开并能返回。当前菜单入口、HUD 提示均为本地接线；正式团队集成仍需与 Main Menu、Gameplay HUD 团队协调。Octopus 提出的“首次通过 Campaign stage”成就不属于我们登记的五个成就，暂不改变本项目成就集合。

## 验证

在游戏仓库根目录运行：

```powershell
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\build.ps1
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\test-records.ps1
```

记录测试覆盖字段验证、计时与结束去重、五项成就边界、排行榜排序与十条限制、重启恢复、备份恢复提示、无法恢复时保留原始文件、备份写入失败时保护有效主档案，以及关闭后的事件边界。

2026-10-13 复查：104 项自动检查通过；构建成功（50 个 Java 源文件）。新增测试实际运行 Records 页面的返回循环，验证 Esc/空格返回菜单；另起独立 Java 进程，通过生产代码的 shutdown hook 验证 80 条排队记录在退出后可恢复。结算时补查成就阈值，避免最后一帧跨过 60 秒时漏解锁；结束动画期间不再接收新的碰撞击败。

测试使用实际绘图代码生成满十条排行榜、五项已解锁成就和五项结算成就的图片，位于忽略目录 `out/records-tests/render/`；本轮已检查条件、日期、姓名输入和成绩不会重叠。图片是隔离测试数据的离屏渲染，不代表手工完成整局游戏。全关卡手工回归和其他组分支的正式集成仍未验证。

## 团队主线兼容边界

保留 Campaign 的过关/失败判定、难度设置、自定义按键、音效、资产模块和原 Settings 菜单。Records 使用新的菜单代码 8。当前团队主线尚无 Boss、激光击败或游戏暂停实现，这些未来路径需发布相同击败事件或停止/恢复活跃计时。七个 PR 按 3.1 至 3.7 顺序依赖，后续 PR 在前置 PR 合并前包含依赖提交；正式合并前应更新与重新检查。
