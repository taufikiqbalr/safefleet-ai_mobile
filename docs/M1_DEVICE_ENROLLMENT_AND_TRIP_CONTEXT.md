# M1 — Device Enrollment and Trip Context

M1 turns the Android foundation into an authenticated SafeFleet driver installation. It deliberately does not start camera inference yet; M2 owns computer vision and drowsiness metrics.

## Implemented

- persistent installation UUID generated once and stored in DataStore;
- secure device credential import using the backend credential-rotation JSON;
- credential validation against `GET /api/v1/device/context` before persistence;
- Android Keystore-backed credential storage from M0;
- authenticated driver, vehicle, fleet, assignment, and active-trip context;
- device-authenticated trip start;
- device-authenticated trip completion;
- persistent client trip UUID for retry-safe start requests;
- persistent local trip-end timestamp for retry-safe completion;
- camera/location permission state;
- connectivity and battery status snapshot;
- paired/unpaired driver UI and diagnostics;
- unit tests for pairing-payload validation.

## Provisioning flow

The MVP uses a controlled two-step provisioning flow instead of public self-registration:

```text
SafeFleet Mobile
    |
    | shows installation UUID
    v
Supervisor/Admin
    |
    | POST /devices using installation UUID as deviceUid
    | POST /devices/:id/credentials/rotate
    v
credential JSON
    |
    v
SafeFleet Mobile
    |
    | GET /device/context to validate
    v
Android Keystore-backed CredentialVault
```

The app accepts the complete credential-rotation response; it extracts only `deviceId` and `deviceKey`. Invalid credentials are not saved.

For the hackathon this JSON can be pasted manually. A QR transport can be added later without changing the stored credential or backend authentication contract.

## Device runtime API

M1 uses the backend Phase 4B device-authenticated endpoints:

```text
GET  /api/v1/device/context
POST /api/v1/device/trips/start
POST /api/v1/device/trips/:id/complete
```

Every call uses:

```text
X-SafeFleet-Device-Id
X-SafeFleet-Device-Key
```

The mobile request does not provide organization, driver, vehicle, fleet, or assignment identity. Those values are resolved server-side from the authenticated device.

## Retry-safe trip lifecycle

Before a trip start request, Android stores a generated `clientTripId` in DataStore. If the request fails or the response is lost, a retry reuses the same UUID. The backend therefore returns the already-created trip instead of producing a duplicate.

Before a trip completion request, Android stores the trip ID and local `endedAt` timestamp. A retry preserves the original end time rather than replacing it with the later reconnect time.

WorkManager automation of these retries is intentionally deferred to M4, where telemetry and drowsiness events also use the durable Room outbox.

## Permissions and device health

M1 surfaces:

- camera permission;
- coarse/fine location permission;
- network availability;
- battery percentage when Android exposes it;
- backend reachability.

These are operational readiness indicators only. They are not safety-risk classifications.

## M1 completion boundary

M1 is complete when a registered Android installation can securely pair, fetch its backend-assigned driver/vehicle context, start a trip, survive a retry without duplicate trip creation, and complete the trip using only the device credential.

The next phase, M2, adds CameraX image analysis, facial landmarks, EAR/MAR/PERCLOS, blink/yawn/head-pose metrics, and a versioned temporal drowsiness state machine.
