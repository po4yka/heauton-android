# Heauton Android

Android application for the Heauton project.

## Tech Stack

### Core Technologies

#### UI Framework
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - Modern declarative UI toolkit for Android
  - Fully declarative approach to building UI
  - Reactive UI updates
  - Integration with Android Views when needed
  - Material Design 3 support

#### Design System
- **[Material 3 Expressive](https://m3.material.io/blog/building-with-m3-expressive)** - Latest version of Material Design
  - Expressive component variants with enhanced visual expressiveness
  - Dynamic Color - adaptive color scheme
  - Enhanced typography and iconography
  - Dark theme support out of the box

#### Navigation
- **[Jetpack Compose Navigation 3](https://developer.android.com/guide/navigation/navigation-3)** - Type-safe navigation
  - Type-safe arguments between screens
  - Deep links support
  - Back stack integration
  - Nested navigation graphs support

#### Dependency Injection
- **[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)** - DI framework built on top of Dagger
  - Reduced boilerplate code
  - Compile-time dependency validation
  - Integration with Android components
  - Jetpack libraries support

#### Local Data Storage
- **[Room](https://developer.android.com/training/data-storage/room)** - SQLite abstraction layer for robust database access
  - Compile-time verification of SQL queries
  - Seamless database migration support
  - Integration with Kotlin Coroutines and Flow
  - Type converters for complex data types
  - Support for full-text search and multi-map queries

- **[Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)** - Modern data storage solution
  - Type-safe preferences storage with Preferences DataStore
  - Protocol Buffers support with Proto DataStore
  - Asynchronous API with Kotlin Coroutines and Flow
  - Safe for concurrent access
  - Migration support from SharedPreferences
  - Handles data corruption gracefully

#### Widgets
- **[Jetpack Glance](https://developer.android.com/jetpack/androidx/releases/glance)** - Compose-based framework for App Widgets
  - Declarative API similar to Jetpack Compose
  - Build home screen widgets with composable functions
  - Support for Material 3 design in widgets
  - Simplified widget update mechanism
  - Integration with WorkManager for periodic updates
  - Support for interactive widget elements

### Architecture

The project follows **Clean Architecture** principles and **MVVM** pattern:

- **Presentation Layer** (Jetpack Compose + ViewModel)
  - UI components in Compose
  - ViewModels for state management
  - State holders and UI events

- **Domain Layer** (Business Logic)
  - Use Cases
  - Domain Models
  - Repository Interfaces

- **Data Layer** (Data Sources)
  - Repository Implementations
  - Remote Data Sources (API)
  - Local Data Sources (Database, Preferences)

### Additional Libraries

*Will be added as the project evolves:*

- **Networking**: Retrofit + OkHttp + Kotlin Serialization
- **Async**: Kotlin Coroutines + Flow
- **Image Loading**: Coil for Compose
- **Testing**: JUnit, Mockk, Compose Testing

## Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Kotlin**: 1.9+
- **Java**: 17
- **Gradle**: 8.0+

## Project Structure

```
heauton-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/heauton/
│   │   │   │   ├── di/              # Hilt modules
│   │   │   │   ├── domain/          # Business logic
│   │   │   │   ├── data/            # Data layer
│   │   │   │   ├── presentation/    # UI (Compose)
│   │   │   │   │   ├── navigation/  # Navigation setup
│   │   │   │   │   ├── screens/     # Screen composables
│   │   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   │   └── theme/       # Material 3 theme
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle.kts
└── build.gradle.kts
```

## Building the Project

```bash
# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

## Development

### Code Style
- Following [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Formatting via ktlint
- Maximum line length: 120 characters

### Git Flow
- `main` - production-ready code
- `develop` - active development
- `feature/*` - new features
- `bugfix/*` - bug fixes

## License

See the [LICENSE](LICENSE) file for details.
