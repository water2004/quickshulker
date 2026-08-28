# QuickShulker legacy behavior GameTests

This standalone test mod characterizes the public and observable behavior of
QuickShulker 3.0.4 and checks the same contract against the current build. It
deliberately compiles only against APIs that existed in 3.0.4.

Run the published baseline:

```powershell
..\gradlew.bat -p . runGameTest `
  -PquickShulkerJar=D:\litematica-printer\libs\quickshulker-3.0.4-26.2.jar `
  -PlegacyBehaviorProfile=baseline-3.0.4
```

Run a current build:

```powershell
..\gradlew.bat jar
..\gradlew.bat -p . runGameTest `
  -PquickShulkerJar=D:\quickshulker\build\libs\quickshulker-4.0.0-alpha.1-26.2.jar `
  -PlegacyBehaviorProfile=current
```

Normal contract tests have identical expectations in both profiles. Tests in
`LegacyKnownBugGameTests` describe historical defects explicitly: the baseline
profile proves that 3.0.4 really exhibited the defect, while the current profile
either verifies the fix or records that the compatibility quirk remains.
