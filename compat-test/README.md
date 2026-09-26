# Real connection compatibility tests

This fixture connects separate clients to a dedicated server and checks both
client results and the authoritative server inventory after extraction.

- `runVanillaClient` launches Mojang's `Main` directly, without Fabric Loader,
  mixins, Quick Shulker or any mod entrypoints. A test-only driver schedules input
  on the client main thread. Merely disabling a Fabric receiver is not vanilla.
- `runClient` without `-PquickShulkerJar` tests Fabric API without Quick Shulker.
- `legacy-wire` uses a separate test implementation of the frozen original-v3
  open/inventory channels, with no v4 classes available. It checks real negotiation
  and server-side paged menus. This is wire compatibility evidence, not a claim
  that a historical 26.x binary runs on Minecraft 1.21.1.
- A matching binary that implements the original-v3 API can additionally be
  supplied through `QUICKSHULKER_LEGACY_JAR`; 26.x matrix branches retain their
  release downloads when no override is supplied.
- The v4 case checks direct transfer and the enhanced 64-slot bundle interface.

Every client must join, extract four stone from a carried shulker and four diamonds
from a carried bundle, close the menu, and leave exactly those items in the server
inventory with empty containers. Legacy-wire and unmodded clients use the vanilla
paged bundle layout. Failed assertions and missing result files fail Gradle.

On Linux (JDK 21, Xvfb, Mesa and curl), from the repository root:

```sh
./gradlew build
bash compat-test/run-matrix.sh build/libs/quickshulker-4.0.1+1.21.1.jar
```

The fixture binds loopback only. Results and logs are in `build/matrix-results`
inside this directory. For manual Windows runs, use `gradlew.bat -p compat-test`
with separate `runServer` and client invocations, the same `-PmatrixPort`, and
distinct `-PmatrixName` values. To stop the server gracefully, create the
`<server-matrixName>-stop` file in `build/matrix-results`.

For example, start the server from the repository root in one PowerShell window:

```powershell
.\gradlew.bat -p compat-test runServer -PmatrixName=manual-server -PmatrixPort=25574 -PquickShulkerJar=../build/libs/quickshulker-4.0.1+1.21.1.jar
```

After the server prints `Done`, run the clients sequentially in another window:

```powershell
.\gradlew.bat -p compat-test runVanillaClient -PmatrixName=manual-vanilla -PmatrixPort=25574
.\gradlew.bat -p compat-test runClient -PmatrixName=manual-fabric -PmatrixPort=25574 -PexpectedClient=none
.\gradlew.bat -p compat-test runClient -PmatrixName=manual-legacy -PmatrixPort=25574 -PexpectedClient=legacy-wire
.\gradlew.bat -p compat-test runClient -PmatrixName=manual-v4 -PmatrixPort=25574 -PexpectedClient=new -PexpectedDirect=true -PquickShulkerJar=../build/libs/quickshulker-4.0.1+1.21.1.jar
New-Item -ItemType File -Force compat-test/build/matrix-results/manual-server-stop
```

Each client produces its own result file. After each run, also check that
`manual-server-server.txt` begins with `PASS authoritative`; the server clears
this file whenever a client joins. The Linux matrix copies it for each client.
JAR paths in Gradle properties are relative to the `compat-test` project;
paths passed to `run-matrix.sh` or `QUICKSHULKER_LEGACY_JAR` are relative to the
directory where the script is invoked.

Regression baseline (4.0.0, 26.2): the vanilla client was rejected with a demand
for Fabric; Fabric without Quick Shulker was rejected because
`quickshulker:bundle_item` was missing from `minecraft:menu`.
