<div align="center">

# 📍 Offline Map Timestamp Camera

**An advanced, privacy-first Android camera application engineered for real-time geolocation stamping, custom timestamp overlays, and offline OpenStreetMap integration.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.12-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Latest-7D5260?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![CameraX](https://img.shields.io/badge/CameraX-1.4.1-FF6F00?style=for-the-badge)](https://developer.android.com/training/camerax)
[![OSMDroid](https://img.shields.io/badge/OSMDroid-6.1.18-2C3E50?style=for-the-badge&logo=openstreetmap&logoColor=white)](https://github.com/osmdroid/osmdroid)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange?style=for-the-badge)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-green?style=for-the-badge)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

<br/>

[Features](#-key-features) •
[Architecture](#-architecture--tech-stack) •
[Project Structure](#-project-structure) •
[Getting Started](#-getting-started) •
[Permissions](#-permissions) •
[Roadmap](#-roadmap) •
[Contributing](#-contributing) •
[License](#-license)

</div>

---

## 📖 Overview

**Offline Map Timestamp Camera** is a modern Android application built using **Jetpack Compose**, **CameraX**, and **OSMDroid**. It empowers users to capture high-resolution photos stamped with accurate GPS coordinates, dynamic addresses, customizable dates/times, and embedded mini-maps—even in low-connectivity or completely offline environments.

Whether for fieldwork, construction verification, investigative journalism, travel documentation, or legal evidence collection, this app provides granular control over metadata overlays and visual stamps without compromising personal privacy.

---

## ✨ Key Features

### 📸 Pro Camera Engine
- **Powered by CameraX**: High-performance camera lifecycle management supporting modern Android hardware.
- **Flash & Torch Controls**: Toggle between `Auto`, `On`, `Off`, and continuous `Torch` lighting modes.
- **Camera Switcher**: Seamlessly switch between rear and ultra-wide/front cameras.
- **Real-Time WYSIWYG Overlay**: Live camera viewfinder rendering exactly how the stamped photo will be saved.

### 🗺️ Offline Geolocation & OpenStreetMap
- **OSMDroid Integration**: Interactive vector and cached offline map previews directly embedded into photo corners.
- **Flexible Location Source**: Use live device GPS or manually pin/search any location on the map.
- **Geocoding & Reverse Geocoding**: Automatic address resolution (City, State, Country, Postal Code) with offline fallback.
- **Format Flexibility**: Latitude and Longitude displayed in Decimal Degrees (DD) or Degrees-Minutes-Seconds (DMS).
- **Plus Codes (Open Location Codes)**: Automatic Plus Code calculation for precision location indexing.

### 🕒 Customizable Date & Time Stamping
- **Live vs. Manual DateTime**: Stamp current device time or pick custom historical/future timestamps.
- **12h / 24h Clock Formats**: Full support for both formats with seconds precision.
- **Timezone Awareness**: Choose local timezone or custom global UTC offsets.

### 🎨 Overlay Editor & Studio Presets
- **Customizable Stamp Widgets**: Toggle Date, Time, Coordinates, Address, Plus Code, and Mini-Map individually.
- **Typography & Styling**: Customize font sizes, colors, text alignment, and background opacity for high contrast against any scene.
- **Preset Management**: Save custom overlay designs into Room database presets and switch between them in one tap.

### 🖼️ In-App Gallery & EXIF Inspector
- **Dedicated Gallery**: Browse captured images stored in app-scoped media storage.
- **Full EXIF Inspection**: Read standard EXIF metadata tags (Aperture, ISO, Shutter Speed, Focal Length, GPS Coordinates).
- **Share & Manage**: Share directly with other apps or clean up captures with instant deletion.

### ⚙️ User Preferences & Customization
- **Metric & Imperial Systems**: Toggle between Meters/Kilometers and Feet/Miles.
- **Image Compression Quality**: Tune JPEG compression from 70% to 100% to optimize between storage space and raw quality.
- **Watermark Toggle**: Optional branding watermark toggle.
- **Modern Themes**: Full support for Material You dynamic color, Dark Mode, and Light Mode.

---

## 📱 Screenshots

| Home & Workflow | Location Picker | Overlay Editor |
| :---: | :---: | :---: |
| *(Add Home UI Screenshot)* | *(Add Map & Pin Screenshot)* | *(Add Overlay Editor Screenshot)* |

| Camera Viewfinder | Captured Photo Preview | In-App Gallery |
| :---: | :---: | :---: |
| *(Add Camera Preview)* | *(Add Stamped Output Photo)* | *(Add Gallery & EXIF Inspector)* |

> *Tip: Place high-resolution screenshots in an `art/` or `docs/screenshots/` directory and reference them here.*

---

## 🏗️ Architecture & Tech Stack

This project follows **Modern Android Architecture** recommendations, adhering to Clean Architecture principles, Single-Activity pattern, and Unidirectional Data Flow (UDF).

```
                      ┌──────────────────────┐
                      │      UI Layer        │
                      │  (Jetpack Compose)   │
                      └──────────▲───────────┘
                                 │ Observes UIState / Sends Events
                      ┌──────────┴───────────┐
                      │     ViewModel        │
                      │  (Kotlin StateFlow)  │
                      └──────────▲───────────┘
                                 │ Calls Repository Methods
                      ┌──────────┴───────────┐
                      │   Repository Layer   │
                      └────▲────────────▲────┘
                           │            │
            ┌──────────────┴───┐    ┌───┴────────────────┐
            │   Local Storage  │    │   Hardware / SDK   │
            │  - Room DB       │    │  - CameraX         │
            │  - DataStore     │    │  - OSMDroid / GPS  │
            └──────────────────┘    └────────────────────┘
```

### 🧰 Technology Matrix

| Component | Library / Framework | Version | Purpose |
|:---|:---|:---|:---|
| **Language** | [Kotlin](https://kotlinlang.org/) | `2.0.21` | Modern, concise, type-safe programming language |
| **Build System** | [Gradle Kotlin DSL](https://gradle.org/) | `8.7.3` | Modern, declarative build scripts and version catalogs |
| **UI Toolkit** | [Jetpack Compose BOM](https://developer.android.com/jetpack/compose) | `2024.12.01` | Declarative, modern UI development |
| **Design System** | [Material Design 3](https://m3.material.io/) | Latest | Modern Material You components, styling, and dynamic themes |
| **Navigation** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) | `2.8.5` | Type-safe in-app navigation routes |
| **Camera** | [AndroidX CameraX](https://developer.android.com/training/camerax) | `1.4.1` | Robust, device-agnostic camera preview and image capture |
| **Maps** | [OSMDroid](https://github.com/osmdroid/osmdroid) | `6.1.18` | OpenStreetMap rendering with tile caching and offline support |
| **Database** | [Room](https://developer.android.com/training/data-storage/room) | `2.6.1` | SQLite abstraction for overlay presets, metadata, and history |
| **Preferences** | [DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) | `1.1.2` | Key-value reactive preference storage |
| **Async / Reactive** | [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html) | `1.9.0` | Asynchronous operations, streams, and state management |
| **Image Loading** | [Coil Compose](https://coil-kt.github.io/coil/) | `2.7.0` | Fast, lightweight image loading for previews and gallery items |
| **EXIF Engine** | [AndroidX ExifInterface](https://developer.android.com/jetpack/androidx/releases/exifinterface) | `1.3.7` | Extraction and injection of metadata tags in JPEG images |
| **JSON Serialization**| [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) | `1.7.3` | Fast JSON serialization for preset layouts and configurations |

---

## 📁 Project Structure

```
gps-map-camera/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/offline/maptimestampcamera/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/             # Room Database, DAOs, & Entities
│   │   │   │   │   ├── preferences/       # DataStore Settings & Keys
│   │   │   │   │   └── repository/        # Repositories (Location, Presets, Photos)
│   │   │   │   ├── di/                    # Dependency Injection / Service Locators
│   │   │   │   ├── map/                   # OSMDroid helpers, tile caching, layers
│   │   │   │   ├── model/                 # Domain models, Coordinates, StampConfig
│   │   │   │   ├── ui/                    # Compose Screens & Components
│   │   │   │   │   ├── camera/            # Viewfinder, shutter, controls
│   │   │   │   │   ├── datetime/          # Date & time picker dialogs/screens
│   │   │   │   │   ├── gallery/           # Photo browser, detail view, EXIF sheet
│   │   │   │   │   ├── home/              # Main dashboard & workflow steps
│   │   │   │   │   ├── location/          # OSMDroid map picker & address search
│   │   │   │   │   ├── navigation/        # Compose NavHost & destination routes
│   │   │   │   │   ├── overlay/           # Visual stamp layout & preset editor
│   │   │   │   │   ├── settings/          # Units, compression, watermark, theme
│   │   │   │   │   ├── splash/            # Animated splash & initialization
│   │   │   │   │   └── theme/             # Color schemes, Typography, Shapes
│   │   │   │   ├── utils/                 # Bitmap, EXIF, PlusCode, Formatters
│   │   │   │   ├── MainActivity.kt        # Single activity host
│   │   │   │   └── MapTimestampCameraApp.kt # Application class
│   │   │   ├── res/                       # Drawables, mipmaps, strings, XML configs
│   │   │   └── AndroidManifest.xml        # Permissions, providers, activity declarations
│   │   └── test/                          # Unit and instrumentation tests
│   └── build.gradle.kts                   # App module Gradle configuration
├── gradle/
│   ├── wrapper/                           # Gradle wrapper executables and properties
│   └── libs.versions.toml                 # Version Catalog (dependencies & plugins)
├── .gitignore                             # Comprehensive Android gitignore
├── build.gradle.kts                       # Root project configuration
├── settings.gradle.kts                    # Gradle settings & plugin repositories
├── LICENSE                                # MIT License
└── README.md                              # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites

Ensure your development environment meets the following minimum requirements:
- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK**: Java Development Kit 17 (or newer)
- **Android SDK**:
  - `compileSdk`: 35
  - `targetSdk`: 35
  - `minSdk`: 24 (Android 7.0 Nougat)

### Installation & Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/gps-map-camera.git
   cd gps-map-camera
   ```

2. **Open in Android Studio**:
   - Launch Android Studio.
   - Click **File > Open...** and select the cloned `gps-map-camera` directory.
   - Allow Gradle to sync dependencies and generate KSP sources.

3. **Build the Application**:
   Run the debug build via Gradle Wrapper:

   **Linux / macOS**:
   ```bash
   ./gradlew assembleDebug
   ```

   **Windows (PowerShell / Command Prompt)**:
   ```powershell
   .\gradlew.bat assembleDebug
   ```

4. **Run on Device / Emulator**:
   - Connect an Android device with Developer Mode & USB Debugging enabled (or launch an Android Virtual Device).
   - Press **Shift + F10** (or click the green **Run** button in Android Studio).

---

## 🔒 Permissions

This app requests only the permissions necessary to provide its core functionality:

| Permission | Protection Level | Purpose |
|:---|:---|:---|
| `CAMERA` | Dangerous (Runtime) | Required to display the live viewfinder preview and capture high-resolution photos. |
| `ACCESS_FINE_LOCATION` | Dangerous (Runtime) | Enables precise GPS coordinate acquisition to stamp live location data on photos. |
| `ACCESS_COARSE_LOCATION` | Dangerous (Runtime) | Provides approximate location fallback when GPS lock is unavailable. |
| `INTERNET` | Normal | Required by OSMDroid to download OpenStreetMap tiles and perform reverse geocoding queries. |
| `ACCESS_NETWORK_STATE` | Normal | Checks network availability before attempting tile downloads. |
| `READ_MEDIA_IMAGES` | Dangerous (Runtime, API 33+) | Allows reading captured photos to render the in-app gallery and read EXIF tags. |
| `READ_EXTERNAL_STORAGE` | Dangerous (Legacy, API ≤ 32) | Backwards-compatible storage access for gallery inspection on older Android versions. |

---

## 🗺️ Roadmap

- [x] Initial CameraX capture engine with real-time overlay
- [x] OSMDroid OpenStreetMap integration with offline cache support
- [x] Custom Date, Time, and Timezone picker
- [x] Drag-and-drop / customizable Overlay Studio
- [x] Room-backed preset save & load
- [x] Comprehensive EXIF metadata preservation & inspector
- [ ] Offline vector mbtiles map package importer
- [ ] Video recording mode with live dynamic timestamp overlay
- [ ] Custom watermark logo import (PNG / SVG)
- [ ] Multi-language localization (i18n)

---

## 🤝 Contributing

Contributions are welcomed and appreciated! To contribute:

1. **Fork** the repository.
2. Create your feature branch:
   ```bash
   git checkout -b feature/awesome-feature
   ```
3. Commit your changes following standard semantic messages:
   ```bash
   git commit -m "feat: add offline mbtiles layer support"
   ```
4. Push to your branch:
   ```bash
   git push origin feature/awesome-feature
   ```
5. Open a **Pull Request** targeting the `main` branch.

Please ensure code formatting conforms to Kotlin style guidelines and all Gradle checks pass before submitting.

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for complete details.

```
Copyright (c) 2026 Offline Map Timestamp Camera Contributors
```
