# NJCourts Mobile App

Android mobile prototype for the NJ Courts project. This app provides a professional interface for officers to select tickets, validate evidence using on-device ML, and manage local records.

---

## Tech Stack

- **Language:** Java
- **Architecture:** MVVM (ViewModel, LiveData, RoomDB)
- **Networking:** Retrofit / OkHttp
- **ML Integration:** Google ML Kit (Face Detection, Pose Detection, Selfie Segmentation, Image Labeling)
- **Camera:** CameraX API
- **Persistence:** Room Database (SQLite)
- **Minimum SDK:** Android 14 (API 34)

---

## Midterm Milestone Features

The following core modules are implemented and up to date for the Midterm Milestone:

- **Advanced ML Validation:** Multi-model validation pipeline to prevent person-detection in evidence photos.
- **Strictness Mode Toggle:** Dynamic validation sensitivity (Balanced vs. Strict) with real-time UI feedback.
- **Evidence Dashboard:** A persistent local grid view for reviewing and deleting captured evidence photos.
- **Local Persistence:** Full RoomDB implementation for tickets and photo metadata (BLOB storage).
- **Professional Camera UI:** Custom shutter, stealth-mode overlays, and instant compression (<250KB per image).
- **Network Awareness:** Integrated connectivity checks and mobile data warning modals.
- **Ticket Selection:** Robust ticket selection system refactored with the **Builder Design Pattern**.

---

## Setup Instructions

### 1. Clone the repository
```bash
git clone https://github.com/guynamedaj/NJCourts.git
```

### 2. Open the project in Android Studio
- Launch Android Studio
- Click "Open"
- Select the cloned `NJCourts` folder

### 3. Sync Gradle
Allow Android Studio to sync Gradle dependencies and download ML Kit models.

### 4. Run the project
Run the app using:
- **Android Emulator** (API 34 / Android 14)
- **Physical Device** running Android 14+

---

## Project Structure
```
app/
├── java/
│   └── edu.njit.njcourts
│       ├── ui        (Activities & ViewModels)
│       ├── data      (RoomDB Entities, DAOs, ApiService)
│       ├── models    (POJO Data Models & Builders)
│       ├── adapters  (RecyclerView Adapters)
│       └── utils     (ML, Network, & Image Utils)
├── res/
│   ├── layout        (XML layouts)
│   ├── drawable      (Custom shapes & icons)
│   └── values        (Colors & Themes)
```

---

## Development Workflow
Create a feature branch when working on new functionality.
```bash
git switch -c feature/feature1
```
After completing work, open a Pull Request for review.

---

## Important Notes
Do **NOT** commit the following files (machine-specific):
- `local.properties`
- `build/`
- `.idea/`

---

## Contributors
NJCourts Development Team
