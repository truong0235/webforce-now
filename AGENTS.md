# Webforce-now

Single-module Android app. Java (not Kotlin). Min SDK 21, target/compile SDK 36.

## Build

```bash
./gradlew assembleDebug
./gradlew test                     # unit tests
./gradlew connectedAndroidTest     # instrumented tests on device/emulator
```

## Quirks

- **Java only.** No Kotlin plugin, no Kotlin deps. Write new code in Java (`app/src/main/java`).
- **AGP 9.2.1 / Gradle 9.4.1.** `compileSdk.version` uses new block syntax (not plain `compileSdk = 36`). See `app/build.gradle.kts:7-11`.
- **R8 keep rules** go in `app/src/main/keepRules/` (not `proguard-rules.pro`).
- **Gradle configuration cache** enabled. Run `--no-configuration-cache` if cache causes issues.
- **Java 11** source/target compatibility.
- **CI** (`ci.yml`) is a template generator — manually triggered, pushes scaffolded Android project to `test` branch. Not a real build/test pipeline.
- **No existing business logic.** Only boilerplate example tests (`ExampleUnitTest.java`, `ExampleInstrumentedTest.java`).
