# Records & Achievements 开发与验证

本地实现覆盖团队 3.1–3.7，已同步课程主线 `f476f5b`（2026-10-15），兼容团队主线 `2d5ce71` 的 Campaign、多波次、道具、无限模式、Settings，以及最新 BGM 和音频缓存。正式验收条件仍以 [`teams/Chinese_can_fly.md`](../teams/Chinese_can_fly.md) 为准。

## 使用方式

- 标题页用上下方向键选择 **Records & Achievements**，空格打开；Records 使用编号 9，保留无限模式的 I 快捷键和编号 8。
- 页面用左右方向键切换 **Run Records** 和 **Achievements**；Esc 或空格返回。
- 游戏中解锁成就时，HUD 顶部会显示约三秒的提示；结算页会列出本局解锁的全部成就。
- 档案保存在用户目录 `.space-invaders/records-achievements.properties`。记录和成就解锁时间共同保存，个人最佳由完整记录重新计算。

## 本地数据保护

档案使用带版本号的 UTF-8 Properties 文件。写入在单独线程执行：先写临时文件并重新读取校验，再保留上一个有效备份并替换主档案。读取损坏档案时会保留损坏副本并尝试恢复备份；主档案和备份都不可读时，程序报告问题并禁止自动写入，避免把原文件覆盖为空档案。

玩家在中途退出时不产生 completed-run 记录；已经解锁的成就会立即排队保存。完成一局后异步保存，不阻塞结算页；菜单退出、窗口关闭和 Alt+F4 都会等待后台保存队列，最多十秒，超时或失败会报告错误。强制终止进程或断电无法保证尚未写入的进度。

从备份恢复时，页面和日志会提示恢复结果及新进度可能丢失。备份更新也通过临时文件校验后替换；备份无法更新时停止本次写入并保留原来的有效主档案。

## 当前游戏接线与协作接口

Campaign 的普通/特殊敌人已覆盖子弹、Orbital Annihilator 激光和 Chameleon Hull 接触三种击败路径；Infinite Mode 的 Zigzag 敌人已覆盖子弹击败。所有路径通过 `EnemyDefeatedEvent` 与公共 `GameplayEventPublisher` 接入 3.5；同一敌人的事件 ID 保持稳定，服务忽略重复事件。护盾吸收、敌人逃离、无限模式玩家受撞不算击败。当前主线没有 Boss；未来新增有效击败路径仍需调用该接口。

Records 页面可从标题菜单打开并返回，菜单保持 Settings 和 Infinite Mode 快捷键。两个模式都显示 HUD 解锁提示和本局成就结算；Campaign 通关标题、无限模式重玩路径、现有道具绘制和 BGM 切换均保留。Octopus 提出的“首次通过 Campaign stage”成就不属于我们登记的五个成就，暂不改变本项目成就集合。

## 验证

在游戏仓库根目录运行：

```powershell
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\build.ps1
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\test-records.ps1
```

记录测试覆盖字段验证、计时与结束去重、五项成就边界、排行榜排序与十条限制、重启恢复、备份恢复提示、无法恢复时保留原始文件、备份写入失败时保护有效主档案，以及关闭后的事件边界。

2026-10-15 验证：构建成功（60 个 Java 源文件）；104 项 records 检查、35 项当前玩法兼容检查，以及课程原有 wave list 检查通过。新增检查实际调用当前碰撞处理方法，覆盖三类武器的普通/特殊敌人、重复调用、护盾吸收、无限模式击败/逃离/碰撞、倒计时与结束动画排除、中途退出、60 秒触发、自然死亡记录、I 快捷键和两种模式的重玩。

测试继续覆盖独立进程退出后恢复 80 条排队记录、最后一帧成就阈值补查和档案保护。使用实际绘图代码生成的排行榜、成就页、失败结算、无限模式结算、Campaign 通关五成就结算已离屏检查，输出均在忽略目录 `out/`。这些检查不代表完成了全关卡人工实机游玩，也未完成 20 次击败/60 秒阈值的游戏难度评估。

## 团队主线兼容边界

保留 Campaign 过关/失败判定、多波次构造器、难度设置、自定义按键、音频缓存、BGM、道具、资产模块和 Settings。Campaign 全程与自然死亡的 Infinite run 共用记录/排行榜；无限模式 Esc 中途退出保留上游结算界面和已解锁成就，不创建 completed-run 记录。两个模式均排除输入等待和结束动画；当前课程主线没有 gameplay pause，未来加入时须调用 `RunSession.stopActiveSegment`/`startActiveSegment`。

主线同步使用单独的团队内部 PR；七个需求 PR 仍按 3.1–3.7 顺序依赖，后续 PR 在前置合并前包含依赖提交。所有 PR 的目标均为 `clover0409/Invaders-SDP-24592:main`，课程主仓库仅用于读取和同步。
