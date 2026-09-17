# M0 — Android Foundation

M0 creates the buildable Android application shell used by all later SafeFleet mobile phases.

## Implemented

- Kotlin Android application with Jetpack Compose UI.
- Android SDK 35 / minSdk 26 / JDK 17.
- Hilt application and dependency-injection graph.
- Retrofit/OkHttp backend client with a configurable API base URL.
- Debug-only cleartext support for the Android emulator local backend address.
- Room database with a durable `mobile_outbox` foundation.
- WorkManager worker boundary for later offline synchronization.
- DataStore application preferences.
- Android Keystore-backed encrypted device credential vault.
- CameraX dependencies and camera/location permission declarations.
- Navigation between the driver foundation screen and diagnostics.
- Backend liveness probe from the application shell.
- JVM unit test for API URL normalization.
- GitHub Actions build/test/lint pipeline.

## API base URL

The default debug URL targets a backend running on the development machine from the Android emulator:

```text
http://10.0.2.2:3000/api/v1/
```

Override it through a Gradle property:

```bash
gradle assembleDebug -PSAFEFLEET_API_BASE_URL=https://api.example.com/api/v1/
```

A physical Android device must use an address reachable from that device; `10.0.2.2` is emulator-specific.

## Build

Android Studio can import the project directly. CI pins Gradle 8.10.2 through the Gradle setup action.

Command-line build when Gradle 8.10.2 is available:

```bash
gradle testDebugUnitTest lintDebug assembleDebug
```

## Security boundary

M0 establishes `CredentialVault`. Device secrets are encrypted with an AES key generated inside Android Keystore; ciphertext and IV are stored in private application preferences. M1 will use this vault for the backend-issued SafeFleet device credential.

Release builds disable cleartext HTTP through the manifest placeholder. Production deployments should use HTTPS only.

## Deliberately not implemented in M0

- device pairing/enrollment;
- driver/vehicle/trip lifecycle;
- camera frame analysis;
- EAR/MAR/PERCLOS computation;
- drowsiness state machine;
- driver alarm service;
- real telemetry/drowsiness outbox synchronization;
- BLE cabin sensors.

Those are implemented in M1–M5 rather than being hidden behind placeholder business logic.

## Next phase: M1

M1 should implement secure enrollment and operational context:

1. persistent installation UUID;
2. pairing payload/credential import;
3. `CredentialVault` usage;
4. device-authenticated backend context;
5. active driver/vehicle/trip state;
6. mobile-authenticated trip lifecycle;
7. permission/connectivity/device-health state.

A small backend Phase 4B contract should be added before completing M1 so trip lifecycle does not depend on management-user JWT endpoints.
