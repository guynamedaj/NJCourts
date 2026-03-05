# NJCourts Mobile App

Android mobile prototype for the NJ Courts project. This app is being developed as part of a team project and provides the mobile interface for selecting tickets and capturing evidence.

---

## Tech Stack

- Language: Java
- Platform: Android
- Minimum SDK: Android 14 (API 34)
- IDE: Android Studio

---

## Current Features

- Ticket Selection UI
- Expandable ticket details
- Ticket summary view
- Navigation to Camera screen (placeholder)

The camera functionality and additional modules will be implemented in upcoming sprints.

---

## Setup Instructions

### 1. Clone the repository

git clone https://github.com/guynamedaj/NJCourts.git

### 2. Open the project in Android Studio

- Launch Android Studio
- Click "Open"
- Select the cloned NJCourts folder

### 3. Sync Gradle

Allow Android Studio to sync Gradle dependencies when prompted.

### 4. Run the project

Run the app using:

- Android Emulator (API 34 / Android 14)
or
- A physical Android device running Android 14+

---

## Project Structure

app/
 ├── java/
 │    └── edu.njit.njcourts
 │         ├── ui
 │         ├── models
 │         ├── adapters
 │         └── utils
 ├── res/
 │    ├── layout
 │    ├── drawable
 │    └── values

---

## Development Workflow

Create a feature branch when working on new functionality.

Example:

git checkout -b feature/camera-module

After completing your work, open a Pull Request to merge into the main branch.

---

## Important Notes

Do NOT commit the following files because they are machine-specific:

- local.properties
- build/
- .idea/

---

## Contributors

NJCourts Development Team
