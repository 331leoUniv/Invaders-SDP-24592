# Records & Achievements 开发与验证

需求正文以 `teams/Chinese_can_fly.md` 为准。本提交接入团队当前 Campaign 主线，在整局结束时创建唯一记录；计时排除关卡倒计时和结束动画，重复结束不替换原记录。

```powershell
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\build.ps1
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\test-records.ps1
powershell -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\run-game.ps1
```

脚本查找 JAVA_HOME、PATH 或工作区便携式 JDK。输出和个人高分文件不进入代码提交。
