#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
jar="$(realpath "${1:?Usage: run-matrix.sh built-quickshulker.jar}")"
mc="$(sed -n 's/^minecraft_version=//p' gradle.properties | tr -d '\r')"
case "$mc" in
  26.1) old=3.0.0-26.1 ;;
  26.2) old=3.0.2-26.2 ;;
  *) echo "Unsupported Minecraft version: $mc" >&2; exit 1 ;;
esac
results="$PWD/compat-test/build/matrix-results"
mkdir -p "$results" compat-test/build/reference
old_jar="$PWD/compat-test/build/reference/quickshulker-$old.jar"
curl --fail --location --retry 3 \
  "https://github.com/MoRanpcy/quickshulker/releases/download/$old/quickshulker-$old.jar" -o "$old_jar"

# Keep one real dedicated server alive across clients. Nothing is mocked in the
# handshake or inventory packet paths. Bind only loopback, with offline test IDs.
./gradlew -p compat-test runServer -PquickShulkerJar="$jar" \
  -PmatrixName=ci-server -PmatrixPort=25574 --console=plain >"$results/server.log" 2>&1 &
server_pid=$!
cleanup() {
  touch "$results/ci-server-stop"
  for ((i=0; i<30; i++)); do
    if ! kill -0 "$server_pid" 2>/dev/null; then break; fi
    sleep 1
  done
  kill "$server_pid" 2>/dev/null || true
  wait "$server_pid" || true
}
trap cleanup EXIT
for ((i=0; i<180; i++)); do
  if grep -q 'Done (' "$results/server.log"; then break; fi
  if ! kill -0 "$server_pid" 2>/dev/null; then cat "$results/server.log"; exit 1; fi
  sleep 1
done
grep -q 'Done (' "$results/server.log"

run_client() {
  name="$1"; task="$2"; shift 2
  timeout 240s xvfb-run -a ./gradlew -p compat-test "$task" \
    -PmatrixName="ci-$name" -PmatrixPort=25574 --console=plain "$@" \
    >"$results/$name.log" 2>&1 || { cat "$results/$name.log"; return 1; }
  grep '^PASS ' "$results/ci-$name.txt"
  grep '^PASS authoritative ' "$results/ci-server-server.txt"
  cp "$results/ci-server-server.txt" "$results/ci-$name-server.txt"
}
run_client vanilla runVanillaClient
run_client fabric-without-qs runClient -PexpectedClient=none
run_client original-v3 runClient -PexpectedClient=old -PquickShulkerJar="$old_jar"
run_client v4 runClient -PexpectedClient=new -PexpectedDirect=true -PquickShulkerJar="$jar"
