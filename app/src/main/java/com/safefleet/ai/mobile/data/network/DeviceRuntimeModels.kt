package com.safefleet.ai.mobile.data.network

data class DeviceContextResponse(
    val device: DeviceContextDevice,
    val bindingSource: String,
    val assignment: DeviceContextAssignment?,
    val driver: DeviceContextDriver?,
    val vehicle: DeviceContextVehicle?,
    val fleet: DeviceContextFleet?,
    val activeTrip: DeviceContextTrip?,
    val canStartTrip: Boolean,
    val requiredActions: List<String>,
)

data class DeviceContextDevice(
    val id: String,
    val deviceUid: String,
    val platform: String,
    val status: String,
    val appVersion: String?,
    val modelVersion: String?,
    val lastSeenAt: String?,
)

data class DeviceContextAssignment(
    val id: String,
    val startedAt: String,
    val status: String,
)

data class DeviceContextDriver(
    val id: String,
    val employeeCode: String,
    val fullName: String,
    val status: String,
)

data class DeviceContextVehicle(
    val id: String,
    val plateNumber: String,
    val make: String?,
    val model: String?,
    val fleetId: String?,
    val status: String,
)

data class DeviceContextFleet(
    val id: String,
    val code: String,
    val name: String,
    val status: String,
)

data class DeviceContextTrip(
    val id: String,
    val clientTripId: String?,
    val startedAt: String?,
    val status: String,
)

data class StartDeviceTripRequest(
    val clientTripId: String,
    val startedAt: String? = null,
)

data class CompleteDeviceTripRequest(
    val endedAt: String? = null,
)

data class DeviceTripResponse(
    val id: String,
    val clientTripId: String?,
    val driverId: String,
    val vehicleId: String,
    val deviceId: String?,
    val startedAt: String?,
    val endedAt: String?,
    val status: String,
)
