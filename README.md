# RunAndRead

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)

Ultimate Text-to-Speech Player for Android - Listen to your books while running, exercising, or on the go!

<img src="app/src/main/ic_launcher-playstore.png" width="100" height="100" alt="RunAndRead Logo">

## Overview

RunAndRead is an Android application that converts text to speech, allowing you to listen to your books while running, exercising, or on the go. It supports various e-book formats and provides a clean, intuitive interface for managing your library and controlling playback.
Starting from Android v1.5 (6) and iOS v1.6 (18), Run & Read supports MP3 audiobooks generated using the RANDR pipeline in this repository. [See instructions here](https://github.com/sergenes/runandread-audiobook/blob/main/RANDR.md).

**Download and try the apps for free!**

## Installation

### From App Store
🍏 **App Store**: [Ran & Read for Apple Devices](https://apps.apple.com/us/app/run-read-listen-on-the-go/id6741396289)
### From Google Play
🤖 **Google Play**: [Ran & Read for Android](https://play.google.com/store/apps/details?id=com.answersolutions.runandread)


📱 **Scan QR Codes to Download:**

<div align="center">
<img src="assets/apple_runandread_qr_code.png" width="150px"> &nbsp;&nbsp;&nbsp; <img src="assets/google_runandread_qr_code.png" width="150px">
</div>

### From Source

1. Clone the repository:
   ```
   git clone https://github.com/answersolutions/runandread-android.git
   ```

2. Open the project in Android Studio

3. Build and run the app on your device or emulator

---

## Features

- **Text-to-Speech Playback**: Convert any text or e-book to speech
- **Multiple Voice Support**: Choose from various TTS voices
- **Bookmarks**: Save and jump to specific positions in your books
- **Speed Control**: Adjust playback speed to your preference
- **Library Management**: Organize your books in a clean, intuitive interface
- **E-book Format Support**: Read EPUB, PDF, and plain text files
- **Background Playback**: Continue listening even when the app is in the background
- **Media Controls**: Control playback from your lock screen or notification
- **Highlighting**: Follow along with highlighted text as it's being read

## Architecture

RunAndRead follows the MVVM (Model-View-ViewModel) architecture pattern and is built with modern Android development tools and libraries.

### High-Level Architecture

```mermaid
graph TB
    subgraph "📱 UI Layer"
        UI[Jetpack Compose UI]
        VM[ViewModels]
    end

    subgraph "🎵 Player Layer"
        BP[BookPlayer Interface]
        ABP[AudioBookPlayer]
        SBP[SpeechBookPlayer]
    end

    subgraph "🗣️ TTS Layer"
        TTS[Text-to-Speech Engine]
    end

    subgraph "💾 Data Layer"
        REPO[Repositories]
        DS[Data Sources]
    end

    subgraph "⚙️ Service Layer"
        PS[Background Services]
    end

    UI --> VM
    VM --> REPO
    VM --> BP
    BP --> ABP
    BP --> SBP
    SBP --> TTS
    REPO --> DS
```

### Key Architectural Features

- **🏗️ Clean Architecture**: Separation of concerns with distinct layers
- **🔄 MVVM Pattern**: Reactive UI with ViewModels managing state
- **💉 Dependency Injection**: Hilt for clean dependency management
- **🎯 Single Responsibility**: Each component has a focused purpose
- **🧪 Testable Design**: Interfaces and dependency injection enable easy testing
- **📱 Modern Android**: Built with Jetpack Compose and latest Android APIs

For detailed architecture documentation with comprehensive diagrams, see [ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Technologies Used

- **Kotlin**: Modern, concise programming language for Android
- **Jetpack Compose**: Declarative UI toolkit for building native Android UI
- **Coroutines**: For asynchronous programming
- **Hilt**: For dependency injection
- **Media3 (ExoPlayer)**: For audio playback
- **Android TTS**: For text-to-speech conversion
- **Jetpack Navigation**: For in-app navigation
- **DataStore**: For preferences storage

## Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, or improving documentation, your help is appreciated.

### Development Setup

1. **Prerequisites**
   - Android Studio Arctic Fox or later
   - JDK 11 or later
   - Android SDK with API level 24+

2. **Clone and Setup**
   ```bash
   git clone https://github.com/answersolutions/runandread-android.git
   cd runandread-android
   ```

3. **Open in Android Studio**
   - Open the project in Android Studio
   - Let Gradle sync complete
   - Run the app on an emulator or device

### How to Contribute

1. **Fork the repository**
2. **Create your feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
   - Follow the existing code style
   - Add tests for new functionality
   - Update documentation as needed
4. **Commit your changes**
   ```bash
   git commit -m 'Add some amazing feature'
   ```
5. **Push to your branch**
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Open a Pull Request**

### Code Style Guidelines

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Write unit tests for new features

### Areas for Contribution

- 🐛 **Bug Fixes**: Check our [Issues](https://github.com/answersolutions/runandread-android/issues)
- ✨ **New Features**: E-book format support, UI improvements, accessibility features
- 📚 **Documentation**: Code comments, user guides, architecture documentation
- 🧪 **Testing**: Unit tests, integration tests, UI tests
- 🌍 **Localization**: Translations for different languages
- ♿ **Accessibility**: Improving app accessibility for all users

## 📞 Contact

- **[Sergey N](https://www.linkedin.com/in/sergey-neskoromny/)**

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to all the open-source libraries that made this project possible
- Special thanks to our beta testers for their valuable feedback
