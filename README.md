# NJCourts - Parking Citation Evidence Management

A full-stack system for capturing, validating, and managing parking citation photo evidence. Built as a capstone project at NJIT.

## Components

### Android App (`app/`)
- **Photo capture** using CameraX with real-time ML Kit validation (face detection, pose estimation, person segmentation)
- **Gallery import** with configurable validation strictness (Strict / Balanced)
- **Local storage** via Room DB with offline support
- **Sync** to backend API with status tracking (Not Synced / Submitted / Failed)
- **Car color detection** using PyTorch Mobile

### Backend API (`api/`)
- Express + TypeScript REST API
- Neon PostgreSQL database
- AWS S3 for photo storage with presigned URLs
- API key authentication
- Endpoints: ticket CRUD, photo upload, photo deletion

### Web App (`web/`)
- React + Vite + Tailwind CSS
- PCSAM-style case search and management interface
- Case summary, photo evidence viewer with lightbox
- Administrative evidence deletion

## Running

### API
```bash
cd api
cp .env.example .env  # configure DB, S3, and API_KEY
npm install
npm run dev
```

### Web App
```bash
cd web
npm install
npm run dev
```
Web app runs at `http://localhost:5173` by default.

### Android App
Open the project in Android Studio and run on an emulator or device. For emulator, the API is reached at `http://10.0.2.2:3000/api/`. For a physical device, update `ApiConstants.java` with your machine's LAN IP.

