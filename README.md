# SafeFleet AI Mobile

Android edge-safety application for SafeFleet AI. This repository owns the driver-side runtime: camera capture, on-device driver-state inference, immediate local alarms, GPS/device telemetry, offline-first synchronization, and optional cabin-sensor integration.

The mobile application is intentionally **edge-first**. Immediate driver warning must continue to work without a network connection. The backend receives derived measurements and operational safety events; it is not the primary wake-up mechanism and does not require continuous raw camera upload.

## Repository role

```text
SafeFleet Mobile
  = perception + immediate driver safety

SafeFleet Backend
  = fleet safety intelligence + storage + alerts + realtime

SafeFleet Web
  = fleet/supervisor operations dashboard
```

Backend repository: `taufikiqbalr/safefleet-ai_backend`.

## Phased implementation

### M0 — Android foundation ✅

Implemented:

- Kotlin + Jetpack Compose;
- single `app` Gradle module for MVP simplicity;
- CameraX dependencies and camera permission boundary;
- Room database foundation;
- WorkManager sync foundation;
- Retrofit/OkHttp networking foundation;
- DataStore configuration foundation;
- Android Keystore-backed secret storage boundary;
- Hilt dependency injection;
- Coroutines/Flow;
- environment configuration for backend URL;
- app navigation and operational shell;
- unit-test, lint, and Android build CI.

### M1 — Device enrollment and trip context ✅

Implemented:

- persistent application/device UUID;
- secure device credential storage;
- pairing/enrollment flow;
- backend device context;
- driver/vehicle/trip context;
- mobile-authenticated trip start/complete flow;
- permissions/connectivity/device-health state.

### M2 — Driver monitoring / computer vision ✅

Implemented:

- CameraX front-camera image-analysis pipeline;
- MediaPipe Face Landmarker on-device inference;
- EAR and MAR from facial landmarks;
- time-weighted PERCLOS observation window;
- blink count/rate;
- eye-closure duration;
- yawning state/duration and repeated-yawn window;
- approximate geometric head pose (pitch/yaw/roll);
- versioned/calibrated threshold profile editor;
- temporal NORMAL / CAUTION / DROWSY state engine;
- face-loss state;
- model/profile/inference metadata;
- monitoring diagnostics UI.

SafeFleet does **not** ship a universal EAR/MAR/PERCLOS safety threshold. Until an explicit calibrated profile is saved, temporal classification remains `UNCALIBRATED`. Thresholds need validation for the actual camera, driver population, lighting, landmark implementation, and test protocol.

### M3 — Driver safety session

- active monitoring screen integrated with trip foreground execution;
- foreground trip/safety service;
- local audio + vibration alarm;
- camera/GPS/network/device state indicators;
- face-loss/camera-unavailable operational handling;
- drowsiness event generation;
- local alarm works when backend/network is unavailable.

### M4 — Backend integration and offline synchronization

- device-authenticated API client;
- telemetry batches;
- drowsiness-event batches;
- durable Room outbox;
- retry through WorkManager;
- stable client-generated event UUIDs;
- ACCEPTED/DUPLICATE/REJECTED handling;
- GPS, battery, network, app/model version metadata;
- offline replay and out-of-order upload tolerance.

Mobile MVP completion boundary: **M0 through M4**.

### M5 — Cabin/environment sensor integration

- generic sensor abstraction;
- BLE-first adapter when selected hardware supports it;
- optional ESP32/sensor connectivity;
- CO/VOC/etc. readings as type/value/unit data;
- sensor health/connectivity state;
- backend `/device/sensor-readings/batch` integration.

Toxic-gas thresholds are not invented in the app. They must come from the selected hardware specification and approved safety policy.

### M6 — Validation and release hardening

- daylight/low-light test matrix;
- glasses/face-position/temporary face-loss cases;
- camera/GPS/network recovery;
- offline-to-online replay;
- duplicate retry verification;
- process recreation/device reboot recovery;
- false-alarm and latency instrumentation;
- signed release build and demo checklist.

## Architecture direction

```text
Front Camera
     |
     v
CameraX ImageAnalysis
     |
     v
MediaPipe Face Landmarker
     |
     v
EAR / MAR / head pose
     |
     v
Temporal analysis
PERCLOS / blink / closure / yawn
     |
     v
Versioned ThresholdProfile
     |
     v
Driver State
UNCALIBRATED / NORMAL / CAUTION / DROWSY
     |
     +------> M3 local alarm
     |
     +------> M4 Room outbox -> backend
```

## Development environment

Current mobile target:

- Android Studio with JDK 17;
- Android SDK 35;
- minSdk 26;
- Gradle 8.10.2 pinned in CI;
- backend URL supplied through Gradle properties / BuildConfig rather than hard-coded production endpoints;
- MediaPipe Face Landmarker float16 v1 task downloaded into app assets by Gradle before build.

Local backend remains Docker-based; the Android application itself is built with Gradle and runs on an emulator or physical Android device.
