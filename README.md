# RunAndRead

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)

Ultimate Text-to-Speech Player for Android - Listen to your books while running, exercising, or on the go!

<img src="app/src/main/ic_launcher-playstore.png" width="100" height="100" alt="RunAndRead Logo">

## Overview

RunAndRead is an Android application that converts text to speech, allowing you to listen to your books while running, exercising, or on the go. It supports various e-book formats and provides a clean, intuitive interface for managing your library and controlling playback.
Starting from Android v1.5 (6) and iOS v1.6 (18), Run & Read supports MP3 audiobooks generated using the RANDR pipeline in this repository. [See instructions here](https://github.com/sergenes/runandread-audiobook/blob/main/RANDR.md).

## Features

- **Text-to-Speech Playback**: Convert any text or e-book to speech
- **Multiple Voice Support**: Choose from various TTS voices
- **Bookmarks**: Save and jump to specific positions in your books
- **Speed Control**: Adjust playback speed to your preference
- **Library Management**: Organize your books in a clean, intuitive interface
- **E-book Format Support**: Read EPUB, PDF, and plain text files and a custom `.randr` archive
- **Background Playback**: Continue listening even when the app is in the background
- **Media Controls**: Control playback from your lock screen or notification
- **Highlighting**: Follow along with highlighted text as it's being read


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

## Architecture

RunAndRead follows the MVVM (Model-View-ViewModel) architecture pattern and is built with modern Android development tools and libraries.

For a detailed overview of the app's architecture, see [ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Technologies Used

- **Kotlin**: Modern, concise programming language for Android
- **Jetpack Compose**: Declarative UI toolkit for building native Android UI
- **Coroutines**: For asynchronous programming
- **Hilt**: For dependency injection
- **Media3 (ExoPlayer)**: For audio playback
- **Android TTS**: For text-to-speech conversion
- **Jetpack Navigation**: For in-app navigation
- **DataStore**: For preferences storage

## 📦 Dependencies
[RunAndRead-Audiobook](https://github.com/sergenes/runandread-audiobook) is an open-source project aimed at generating high-quality text-to-speech (TTS) generated audiobooks using models like **Zyphra/Zonos**.
Run & Read supports MP3 audiobooks generated using the RANDR pipeline in this repository. [See instructions here](https://github.com/sergenes/runandread-audiobook/blob/main/RANDR.md).


## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Contact

- **[Sergey N](https://www.linkedin.com/in/sergey-neskoromny/)**

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to all the open-source libraries that made this project possible
- Special thanks to our beta testers for their valuable feedback
