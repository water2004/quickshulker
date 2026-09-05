# Real connection compatibility tests

This fixture connects separate clients to a dedicated server and checks both
client results and the authoritative server inventory after extraction.

- `runVanillaClient` launches Mojang's `Main` directly, without Fabric Loader,
  mixins, Quick Shulker or any mod entrypoints. A test-only driver schedules input
  on the client main thread. Merely disabling a Fabric receiver is not vanilla.
- `runClient` without `-PquickShulkerJar` tests Fabric API without Quick Shulker.
- The original-v3 case downloads the matching upstream release, not a stub.
- The v4 case checks direct transfer and the enhanced 64-slot bundle interface.

Every client must join, extract four stone from a carried shulker and four diamonds
from a carried bundle, close the menu, and leave exactly those items in the server
inventory with empty containers. Original-v3 and unmodded clients use the vanilla
paged bundle layout. Failed assertions and missing result files fail Gradle.

On Linux (JDK 25, Xvfb, curl):

```sh
./gradlew build
bash compat-test/run-matrix.sh build/libs/quickshulker-4.0.1-26.1.jar
```

The fixture binds loopback only. Results and logs are in `build/matrix-results`
inside this directory. For manual Windows runs, use `gradlew.bat -p compat-test`
with separate `runServer` and client invocations, the same `-PmatrixPort`, and
distinct `-PmatrixName` values. To stop the server gracefully, create the
`<server-matrixName>-stop` file in `build/matrix-results`.

Regression baseline (4.0.0, 26.2): the vanilla client was rejected with a demand
for Fabric; Fabric without Quick Shulker was rejected because
`quickshulker:bundle_item` was missing from `minecraft:menu`.
