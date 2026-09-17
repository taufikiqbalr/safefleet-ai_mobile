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

### M0 — Android foundation

Target: reproducible Android project that builds in CI and provides the application shell for later safety features.

Planned/implemented scope:

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
- app navigation and placeholder operational screens;
- unit-test and Android build CI.

### M1 — Device enrollment and trip context

- persistent application/device UUID;
- secure device credential storage;
- pairing/enrollment flow;
- backend device context;
- driver/vehicle/trip context;
- mobile-authenticated trip start/complete flow;
- permissions/connectivity/device-health state.

### M2 — Driver monitoring / computer vision

- CameraX image-analysis pipeline;
- face/landmark processing;
- EAR and MAR;
- PERCLOS observation window;
- blink count/rate;
- eye-closure duration;
- yawning state/duration;
- head pose (pitch/yaw/roll);
- versioned/calibrated threshold profile;
- temporal drowsiness state machine;
- model/threshold/inference metadata.

No universal EAR/MAR/PERCLOS threshold will be treated as scientifically valid by default. Threshold profiles are versioned and calibratable.

### M3 — Driver safety session

- active monitoring screen;
- foreground trip/safety session;
- local audio + vibration alarm;
- camera/GPS/network/device state indicators;
- face-loss/camera-unavailable handling;
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
CameraX / sensors / location
           |
           v
      edge processing
           |
           +------> immediate local alarm
           |
           v
      normalized events
           |
           v
        Room outbox
           |
     network available
           |
           v
       WorkManager
           |
           v
   SafeFleet Backend API
```

## Development environment

M0 targets:

- Android Studio with JDK 17;
- Android SDK 35;
- minSdk 26;
- Gradle 8.10.2 pinned in CI; Android Studio can import the Gradle project directly;
- backend URL supplied through Gradle properties / BuildConfig rather than hard-coded production endpoints.

Local backend remains Docker-based; the Android application itself is built with Gradle and runs on an emulator or physical Android device.
