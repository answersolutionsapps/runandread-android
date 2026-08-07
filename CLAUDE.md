# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

RunAndRead is a native Android app (Kotlin/Jetpack Compose) that converts text/e-books to speech and plays audiobooks. It supports EPUB, PDF, plain text, and a custom `.randr` archive format (pre-generated audio + timed transcript produced by the companion [runandread-audiobook](https://github.com/sergenes/runandread-audiobook) pipeline).

## Build & Test Commands

```bash
./gradlew assembleDebug              # build debug APK
./gradlew installDebug                # install on connected device/emulator
./gradlew test                        # run all unit tests (app/src/test)
./gradlew testDebugUnitTest --tests "com.answersolutions.runandread.ExampleUnitTest"  # run a single unit test
./gradlew connectedAndroidTest        # run instrumented tests (app/src/androidTest), needs a device/emulator
./gradlew lint                        # Android lint
```

There is no dedicated ktlint/detekt config in this repo — `./gradlew lint` is the only static check wired up. Unit test coverage is currently minimal (mostly template `ExampleUnitTest`/`ExampleInstrumentedTest`); MockK and kotlinx-coroutines-test are already on the test classpath if you add real tests.

Module layout: single `:app` module (see `settings.gradle.kts`). Namespace/applicationId is `com.answersolutions.runandread`, minSdk 24, target/compileSdk 36.

## Architecture

MVVM + Clean Architecture with Hilt for DI. Full diagrams live in `docs/ARCHITECTURE.md` — read it for the complete picture; the essentials are below.

```
UI (Compose Views + ViewModels)
   -> Domain (PlayerUseCase, BookmarkUseCase)
        -> Player (BookPlayer interface: AudioBookPlayer | SpeechBookPlayer)
        -> Data (PlayerStateRepository, LibraryRepository, EBookRepository, VoiceRepository)
PlayerService (foreground service, MediaSessionCompat) also drives the Domain layer directly.
```

Key pattern: **the Domain layer decouples the UI and the background Service from each other and from the concrete player.** Neither `PlayerViewModel` nor `PlayerService` talks to `AudioBookPlayer`/`SpeechBookPlayer` directly — they go through `PlayerUseCase`/`BookmarkUseCase`, which hold a `BookPlayer` reference set at runtime via `setBookPlayer()`. `PlayerStateRepository` (a Hilt singleton `StateFlow`-backed store) is the single source of truth for current book / playback position / play-pause state, observed by both the UI and `PlayerService` (which republishes it to `MediaSessionCompat` for lock-screen controls and the media notification).

**Two player implementations behind one `BookPlayer` interface** (`app/src/main/java/com/answersolutions/runandread/BookPlayer.kt`):
- `audio/AudioBookPlayer.kt` — plays pre-rendered MP3 audiobooks (ExoPlayer/Media3), used for `.randr` books and generic `AudioBook`s.
- `voice/SpeechBookPlayer.kt` — drives on-device TTS via `voice/SimpleSpeechProvider.kt` for plain-text/EPUB/PDF books, paced by `SpeakingCallBack`.
- Which one is used is decided by `RunAndReadBook.playerType()` (`BookPlayerType.AUDIO` vs `.TTS`).

**Book model**: `data/model/RunAndReadBook.kt` is a sealed class (`AudioBook`, `Book`) holding UI state (`BookUIState` StateFlow) and bookmarks. `.randr` files are zip archives containing `book.json` (title/author/language/voice/model/rate + a list of `TextPart(start_time_ms, text)` for highlighting) plus `audio.mp3`; parsing/extraction lives in `data/datasource/EBookDataSource.kt` (`EBookDataSource.validExtensions` gates accepted file types: `.pdf`, `.epub`, `.txt`, `.randr`). Zip extraction validates against Zip Slip by checking each entry's canonical path stays under the output dir — preserve that check if you touch `unzipFile()`.

**Data layer**: repositories (`data/repository/`) wrap data sources (`data/datasource/`) — e.g. `LibraryRepository` composes `LibraryDiskDataSource` (on-device library) and `LibraryAssetDataSource` (bundled sample content). All repositories/use cases are provided as Hilt singletons from `di/AppModule.kt` — that file is the map of what depends on what; check it first when wiring a new component into the graph.

**Service layer**: `services/PlayerService.kt` is a foreground `Service` (not `MediaBrowserService`) using `MediaSessionCompat` for lock-screen/notification controls. It observes `PlayerStateRepository.getPlaybackState()` and pushes updates into the media session; playback intents (`ACTION_PLAY`/`ACTION_PAUSE`/`ACTION_FF`/`ACTION_FR`/`ACTION_SERVICE_STOP`) are routed back through `PlayerUseCase`/`BookmarkUseCase`, never touching the player directly.

**Navigation**: Jetpack Navigation Compose, screens under `ui/<feature>/` (`library`, `player`, `settings`, `about`, `init` for splash), each typically with a `*ScreenView` (Composable), `*ViewModel`, and an `*Event` sealed class for UI events. Shared/reusable composables live in `ui/components/`.

## Conventions

- Logging via Timber (`timber.log.Timber`), not `println`/`Log`.
- Kotlin coroutines throughout; ViewModels use `viewModelScope`, the service uses its own `serviceScope` (`SupervisorJob + Dispatchers.Main`), data sources do I/O on `Dispatchers.IO` via `withContext`.
- Extension functions live under `com.answersolutions.extensions` (`app/src/main/java/com/answersolutions/extensions/`), not the `runandread` package.
