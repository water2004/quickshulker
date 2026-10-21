# Minecraft 1.21.10 backport

This branch ports Quick Shulker 4.0.1 from the existing 1.21.1 implementation.
It uses Java 21, Mojang mappings and Fabric Loom remapping. It does not modify
other Minecraft branches. Binary releases are published only after validation.

The component-backed storage wrapper enforces the offered item stack limit
before delegating insertions. Empty Fabric component slots can otherwise accept
two unstackable tools in one slot. The joint ChainVein regression exercises this.

Validation:

- `./gradlew clean test runGameTest build`
- `./gradlew -p legacy-gametest runGameTest -PquickShulkerJar=<absolute built jar> -PlegacyBehaviorProfile=current`
- `bash compat-test/run-matrix.sh <absolute built jar>`

The matrix launches a real dedicated v4 server and genuine vanilla, Fabric-only,
original v3 and v4 clients. Server assertions check authoritative shulker/bundle
contents. It does not replace the original v3 release with a compatibility stub.

Local Windows server suites passed 31 required GameTests and 42 required legacy
contract GameTests (including Fabric's generated test). Unit tests and joint
Printer/ChainVein checks are recorded separately. GitHub Actions results are
reported independently from local checks. This branch's workflow does not
publish releases on branch pushes.
