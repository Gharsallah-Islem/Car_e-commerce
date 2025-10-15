# Mobile Application (Kotlin/Android)

## Overview
Native Android application for the Car E-Commerce platform built with Kotlin.

## Prerequisites
- Android Studio Arctic Fox or later
- JDK 11+
- Android SDK (API 24+)

## Setup

1. Open project in Android Studio
2. Sync Gradle
3. Run on emulator or device

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/              # Kotlin source files
│   │   ├── res/               # Resources
│   │   └── AndroidManifest.xml
│   ├── test/                  # Unit tests
│   └── androidTest/           # Instrumented tests
└── build.gradle.kts
```

## Architecture
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture**
- **Dependency Injection** (Hilt)

## Features
- Product browsing
- Shopping cart
- Order tracking
- User authentication
- Real-time notifications
- AR view for parts
- Barcode scanning

## Development

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

### Build
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Dependencies
- Retrofit - Network calls
- Room - Local database
- Hilt - Dependency injection
- Coroutines - Async operations
- Jetpack Compose - UI

## Documentation
See [docs/mobile](../docs/mobile) for detailed documentation.

## License
MIT
