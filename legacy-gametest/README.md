# QuickShulker legacy behavior GameTests

This standalone test mod characterizes the public and observable behavior of
QuickShulker 3.0.4 and checks the same contract against the current build. It
deliberately compiles only against APIs that existed in 3.0.4.

The 1.21.1 port runs the `current` contract profile. Historical 26.x jars cannot
be loaded into 1.21.1; the `baseline-3.0.4` profile is retained for upstream use
with a matching Minecraft target.

Run a current build from the repository root:

```sh
./gradlew remapJar
./gradlew -p legacy-gametest runGameTest \
  -PquickShulkerJar=../build/libs/quickshulker-4.0.1+1.21.1.jar \
  -PlegacyBehaviorProfile=current
```

Use JDK 21. On Windows, use `gradlew.bat` and put the command on one line.
`quickShulkerJar` is resolved relative to the `legacy-gametest` project directory.

Normal contract tests have identical expectations in both profiles. Tests in
`LegacyKnownBugGameTests` describe historical defects explicitly: the baseline
profile proves that 3.0.4 really exhibited the defect, while the current profile
either verifies the fix or records that the compatibility quirk remains.
