---
id: android
name: android
description: >
  Provides expert knowledge for building modern native Android apps using Kotlin, Jetpack Compose, Material Design 3, MVI architecture, Hilt dependency injection, and Media3/ExoPlayer.

user-invokable: true
license: MIT
metadata:
  author: lgzarturo
  category: mobile

compatibility:
  tools: [claude, codex, gemini, agy, opencode]
  stacks:
    languages: [kotlin, java]
    frameworks: [android]

risk:
  level: medium
  can_execute_shell: true
  can_modify_files: true
  requires_network: false

inputs: []

outputs: []

quality:
  reviewed_by: codeconductor-core
  version: 0.1.0
paths:
  - "**/*.kt"
  - "**/AndroidManifest.xml"
---

# Android + Kotlin (Jetpack Compose & Media3)

This playbook defines the architecture, UI design system, performance guidelines, and testing strategy for native Android applications.

`android-ui-design` owns specialized Jetpack Compose phone UI/UX design,
implementation, review, and audit guidance. This skill remains the general guide
for architecture, Hilt dependency injection, Media3, performance, and testing.

---

## 1. Lead Architect & Modularization

### Architecture (MVI / MVVM with Clean Architecture)
- Prefer **MVI (Model-View-Intent)** for state management. Ensure a strict unidirectional data flow:
  - **UiState**: Single source of truth for the UI state.
  - **UiIntent**: Actions initiated by the user or system.
  - **UiEffect**: One-time events (navigation, toasts, snackbars).
- Separate the project into clean logical modules:
  - `app`: Application entry point, launcher activity, global dependency injection configuration.
  - `core`: Shared resources, network layer, media playback logic (ExoPlayer/Media3), storage.
  - `feature`: Individual business features (isolated and independent of other features).

### Dependency Injection (Hilt)
- Annotate the Application class with `@HiltAndroidApp`.
- Annotate Activities and Fragments with `@AndroidEntryPoint`.
- Use constructor injection for ViewModels with `@HiltViewModel`.

---

## 2. Jetpack Compose UI/UX

### Recomposition & State Management
- Consume `StateFlow` from ViewModels safely using `collectAsStateWithLifecycle()` to respect the activity lifecycle.
- Keep data models immutable. Mark configuration classes with `@Immutable` or `@Stable` to prevent redundant recompositions.
- Use `contentType` and `key` in `LazyColumn` and `LazyRow` to optimize item recycling and rendering performance.
- Avoid fixed hardcoded margins. Apply `WindowInsets` to support modern edge-to-edge screens.

---

## 3. Performance & Multimedia (Media3 / ExoPlayer)

### Playback & Services
- Run `ExoPlayer` / `Media3` inside a robust `Foreground Service` (using `MediaSessionService`) to ensure playback continues when the app is in the background.
- Clean up resources: release player instances in `onDestroy` or when the service is stopped.
- Control wake locks strictly to avoid draining the user's battery when playback is paused.

### Kotlin Coroutines & Threading
- Always inject `CoroutineDispatcher` instances instead of hardcoding them:
  - `Dispatchers.IO`: Database access, network operations, file reading.
  - `Dispatchers.Default`: CPU-intensive operations (parsing JSON, filtering large lists).
  - `Dispatchers.Main`: UI modifications and light interactions.

### R8 & Resource Optimization
- Enable shrinking and obfuscation in production builds:
  - `isMinifyEnabled = true`
  - `isShrinkResources = true`
- Convert all static images to the modern `WebP` format to minimize the application binary size.

---

## 4. Quality Assurance & Automated Testing

### Unit Testing
- Use JUnit 5 and MockK for mocking dependencies.
- Test ViewModels and Reducers by asserting StateFlow updates. Use `Turbine` to easily test Kotlin Flows.
- Mock all multimedia/ExoPlayer components to avoid loading actual audio/video resources in unit tests.

### UI Testing
- Create UI tests using Compose test rules: `createComposeRule()` or `createAndroidComposeRule<MainActivity>()`.
- Assert correct rendering, visibility states, and user interactions without executing real network calls.

---

## 5. Key Gradle & ADB Commands

Use these commands for building, checking code style, running tests, and profiling performance:

| Command | Action |
| ------- | ------ |
| `./gradlew init` | Initialize a new Gradle project structure. |
| `./gradlew app:dependencies` | List all dependencies of the application module. |
| `./gradlew assembleDebug` | Validate compilation and generate debug APK. |
| `./gradlew ktlintCheck` | Run KtLint check to enforce Kotlin style guidelines. |
| `./gradlew lintDebug` | Run Android Lint tool to inspect code quality and UI issues. |
| `./gradlew testDebugUnitTest` | Run unit tests for debug build variant. |
| `./gradlew connectedAndroidTest` | Run instrumented UI tests on connected emulator or device. |
| `./gradlew assembleRelease` | Generate release build variant with R8 optimizations. |
| `adb shell dumpsys batterystats` | Profile system battery consumption metrics. |
| `adb shell am dumpheap <package> heap.hprof` | Dump application heap memory file for profiling. |
