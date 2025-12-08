# RunAndRead Architecture Overview

This document provides a high-level overview of the RunAndRead Android application architecture.

## Project Structure

```mermaid
graph TD
    subgraph "📁 app/src/main/java/com/answersolutions/runandread"
        subgraph "🎨 UI Layer"
            UI_LIB[ui/library/]
            UI_PLAYER[ui/player/]
            UI_SETTINGS[ui/settings/]
            UI_ABOUT[ui/about/]
            UI_NAV[ui/navigation/]
            UI_COMP[ui/components/]
        end

        subgraph "💾 Data Layer"
            DATA_MODEL[data/model/]
            DATA_REPO[data/repository/]
            DATA_DS[data/datasource/]
        end

        subgraph "🎵 Player Layer"
            AUDIO[audio/]
            VOICE[voice/]
            PLAYER[BookPlayer.kt]
        end

        subgraph "⚙️ Core"
            DI[di/]
            SERVICES[services/]
            APP[app/]
            MAIN[MainActivity.kt]
        end

        subgraph "🛠️ Extensions"
            EXT[extensions/]
        end
    end

    UI_LIB --> DATA_REPO
    UI_PLAYER --> PLAYER
    UI_SETTINGS --> DATA_REPO
    PLAYER --> AUDIO
    PLAYER --> VOICE
    DATA_REPO --> DATA_DS
    DATA_REPO --> DATA_MODEL
    DI --> DATA_REPO
    DI --> PLAYER
    SERVICES --> PLAYER
```

### Package Organization

- **`ui/`**: All UI-related components organized by feature
  - `library/`: Book library screen and components
  - `player/`: Book player screen and controls
  - `settings/`: App and book settings screens
  - `about/`: About screen and app information
  - `navigation/`: Navigation logic and ViewModels
  - `components/`: Reusable UI components

- **`data/`**: Data layer components
  - `model/`: Data models and entities
  - `repository/`: Repository implementations
  - `datasource/`: Data source interfaces and implementations

- **`audio/`**: Audio playback functionality
- **`voice/`**: Text-to-speech functionality
- **`di/`**: Dependency injection modules
- **`services/`**: Background services
- **`app/`**: Application class and global configuration

## Application Architecture

The following diagram shows the overall architecture of the RunAndRead application:

```mermaid
graph TB
    subgraph "UI Layer"
        UI[Compose UI Components]
        VM[ViewModels]
        UI --> VM
    end

    subgraph "Player Layer"
        BP[BookPlayer Interface]
        ABP[AudioBookPlayer]
        SBP[SpeechBookPlayer]
        BP --> ABP
        BP --> SBP
    end

    subgraph "TTS Layer"
        SSP[SimpleSpeechProvider]
        SCB[SpeakingCallBack]
        SSP --> SCB
    end

    subgraph "Data Layer"
        REPO[Repositories]
        DS[Data Sources]
        MODEL[Models]
        REPO --> DS
        REPO --> MODEL
    end

    subgraph "Service Layer"
        PS[PlayerService]
    end

    subgraph "DI Layer"
        HILT[Hilt/AppModule]
    end

    VM --> REPO
    VM --> BP
    SBP --> SSP
    PS --> BP
    HILT -.-> UI
    HILT -.-> VM
    HILT -.-> REPO
    HILT -.-> BP
```

## Application Layers

### 1. UI Layer

The UI layer is built using Jetpack Compose and follows the MVVM (Model-View-ViewModel) pattern:

- **Views**: Compose UI components that render the user interface
  - `LibraryScreenView`: Displays the library of books
  - `PlayerScreenView`: Shows the player interface for reading/listening to books
  - `BookSettingsView`: Provides settings for book playback
  - `AboutScreenView`: Shows information about the app

- **ViewModels**: Manage UI state and business logic
  - `LibraryScreenViewModel`: Manages the library of books
  - `PlayerViewModel`: Controls the playback of books
  - `BookSettingsViewModel`: Handles book settings
  - `VoiceSelectorViewModel`: Manages voice selection for TTS
  - `NavigationViewModel`: Controls navigation between screens

### 2. Player Layer

The player layer is responsible for playing books, either as audio or using text-to-speech:

```mermaid
classDiagram
    class BookPlayer {
        <<interface>>
        +play()
        +pause()
        +stop()
        +seekTo(position)
        +setSpeed(speed)
        +getCurrentPosition()
        +getDuration()
    }

    class AudioBookPlayer {
        -exoPlayer: ExoPlayer
        +play()
        +pause()
        +stop()
        +seekTo(position)
        +setSpeed(speed)
        +getCurrentPosition()
        +getDuration()
    }

    class SpeechBookPlayer {
        -speechProvider: SimpleSpeechProvider
        -currentText: String
        +play()
        +pause()
        +stop()
        +seekTo(position)
        +setSpeed(speed)
        +getCurrentPosition()
        +getDuration()
    }

    class SimpleSpeechProvider {
        -textToSpeech: TextToSpeech
        +speak(text: String)
        +stop()
        +pause()
        +setSpeed(speed)
    }

    class SpeakingCallBack {
        <<interface>>
        +onSpeechStart()
        +onSpeechEnd()
        +onError()
    }

    BookPlayer <|-- AudioBookPlayer
    BookPlayer <|-- SpeechBookPlayer
    SpeechBookPlayer --> SimpleSpeechProvider
    SimpleSpeechProvider --> SpeakingCallBack
```

**Components:**
- `BookPlayer`: Interface defining common player functionality
- `AudioBookPlayer`: Implementation for playing audio books using ExoPlayer
- `SpeechBookPlayer`: Implementation for playing books using text-to-speech

### 3. Text-to-Speech (TTS) Layer

The TTS layer handles the conversion of text to speech:

- `SimpleSpeechProvider`: Provides TTS functionality
- `SpeakingCallBack`: Interface for communication between TTS and UI

### 4. Data Layer

The data layer manages the app's data:

```mermaid
classDiagram
    class Book {
        <<abstract>>
        +id: String
        +title: String
        +author: String
        +coverUrl: String
    }

    class AudioBook {
        +audioUrl: String
        +duration: Long
    }

    class RunAndReadBook {
        +content: String
        +bookmarks: List~Bookmark~
    }

    class Bookmark {
        +id: String
        +position: Int
        +title: String
        +timestamp: Long
    }

    class EBookFile {
        +filePath: String
        +format: String
        +size: Long
    }

    class LibraryRepository {
        -diskDataSource: LibraryDiskDataSource
        -assetDataSource: LibraryAssetDataSource
        +getBooks(): List~Book~
        +addBook(book: Book)
        +deleteBook(id: String)
    }

    class EBookRepository {
        -dataSource: EBookDataSource
        +loadEBook(path: String): EBookFile
        +parseContent(file: EBookFile): String
    }

    class VoiceRepository {
        -dataSource: VoiceDataSource
        +getAvailableVoices(): List~Voice~
        +setSelectedVoice(voice: Voice)
    }

    class LibraryDataSource {
        <<interface>>
        +getBooks(): List~Book~
        +saveBook(book: Book)
        +deleteBook(id: String)
    }

    class LibraryDiskDataSource {
        +getBooks(): List~Book~
        +saveBook(book: Book)
        +deleteBook(id: String)
    }

    class LibraryAssetDataSource {
        +getBooks(): List~Book~
        +saveBook(book: Book)
        +deleteBook(id: String)
    }

    class PrefsStore {
        <<interface>>
        +getString(key: String): String
        +putString(key: String, value: String)
    }

    class PrefsStoreImpl {
        +getString(key: String): String
        +putString(key: String, value: String)
    }

    Book <|-- AudioBook
    Book <|-- RunAndReadBook
    RunAndReadBook --> Bookmark

    LibraryRepository --> LibraryDataSource
    LibraryDataSource <|-- LibraryDiskDataSource
    LibraryDataSource <|-- LibraryAssetDataSource

    EBookRepository --> EBookDataSource
    VoiceRepository --> VoiceDataSource

    PrefsStore <|-- PrefsStoreImpl
```

**Components:**
- **Models**:
  - `Book`: Base class for all book types
  - `AudioBook`: Represents an audio book
  - `RunAndReadBook`: Represents a book in the app
  - `Bookmark`: Represents a bookmark in a book
  - `EBookFile`: Represents an e-book file

- **Repositories**:
  - `LibraryRepository`: Manages the library of books
  - `EBookRepository`: Handles e-book file operations
  - `VoiceRepository`: Manages TTS voices

- **Data Sources**:
  - `LibraryDataSource`: Interface for library data operations
  - `LibraryDiskDataSource`: Implementation for disk-based library storage
  - `LibraryAssetDataSource`: Implementation for asset-based library storage
  - `EBookDataSource`: Handles e-book file operations
  - `VoiceDataSource`: Manages TTS voices
  - `PrefsStore`: Interface for preferences storage
  - `PrefsStoreImpl`: Implementation of preferences storage

### 5. Service Layer

The service layer provides background services:

- `PlayerService`: Foreground service for media playback

## Dependency Injection

The app uses Hilt for dependency injection:

- `AppModule`: Provides app-wide dependencies

## Navigation

Navigation is handled using Jetpack Navigation Compose.

```mermaid
stateDiagram-v2
    [*] --> SplashScreen
    SplashScreen --> LibraryScreen

    LibraryScreen --> PlayerScreen : Select Book
    LibraryScreen --> AboutScreen : About Menu

    PlayerScreen --> BookSettingsScreen : Settings Button
    PlayerScreen --> LibraryScreen : Back Button

    BookSettingsScreen --> PlayerScreen : Back/Save
    BookSettingsScreen --> VoiceSelector : Change Voice

    VoiceSelector --> BookSettingsScreen : Voice Selected

    AboutScreen --> LibraryScreen : Back Button

    note right of PlayerScreen
        Main playback interface
        - Play/Pause controls
        - Text highlighting
        - Bookmark management
    end note

    note right of LibraryScreen
        Book library management
        - Book list display
        - Add/Remove books
        - Book selection
    end note

    note right of BookSettingsScreen
        Playback configuration
        - Speed adjustment
        - Voice selection
        - Reading preferences
    end note
```

### Screen Navigation:
- **SplashScreen**: Initial loading screen
- **LibraryScreen**: Main screen showing the book library
- **PlayerScreen**: Book playback interface with controls
- **BookSettingsScreen**: Configuration for book playback
- **VoiceSelector**: TTS voice selection dialog
- **AboutScreen**: App information and credits

## Data Flow

The following diagram illustrates how data flows through the application:

```mermaid
sequenceDiagram
    participant User
    participant UI as Compose UI
    participant VM as ViewModel
    participant Repo as Repository
    participant DS as Data Source
    participant Player as BookPlayer

    User->>UI: User Interaction
    UI->>VM: UI Event
    VM->>VM: Update State

    alt Data Operation Needed
        VM->>Repo: Call Repository Method
        Repo->>DS: Query Data Source
        DS-->>Repo: Return Data
        Repo-->>VM: Return Result
    end

    alt Player Operation Needed
        VM->>Player: Control Playback
        Player-->>VM: Playback Status
    end

    VM->>VM: Update UI State
    VM-->>UI: State Change
    UI->>UI: Recompose
    UI-->>User: Updated Interface
```

### Data Flow Steps:
1. User interacts with the UI
2. UI events are sent to ViewModels
3. ViewModels update state and call repositories as needed
4. Repositories interact with data sources
5. Data flows back to ViewModels
6. ViewModels update UI state
7. UI recomposes based on new state

## Threading

The app uses Kotlin Coroutines for asynchronous operations:

- ViewModels use `viewModelScope` for coroutine management
- Repositories use suspend functions for asynchronous operations
- Data sources perform I/O operations on appropriate dispatchers
