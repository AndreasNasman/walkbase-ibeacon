package com.walkbase.ibeacon.sdk

import android.Manifest
import android.bluetooth.le.AdvertiseSettings
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import com.walkbase.ibeacon.R
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter

// https://www.bluetooth.com/specifications/assigned-numbers/
private const val APPLE_INC_MANUFACTURER_ID: Int = 0x004C

// https://github.com/AltBeacon/android-beacon-library/issues/13
private const val IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
private const val IBEACON_TYPE_CODE: Int = 0x0215

class IBeacon(
    private val context: ComponentActivity,
    private val onAllPermissionsRejected: () -> Unit,
    private val dataFields: List<Long> = listOf(0L),
    // Signal strength at 1 meter measured in dBm. -59 is a typical starting point for BLE devices.
    private val txPower: Int = -59,
    // TODO: Calculate the UUID per device.
    private val uuid: String = "856E3AB6-5EA8-45EB-9813-676BB29C4316",
) {
    var majorValue: String = "0"
        set(value) {
            field = (value.toIntOrNull()?.takeIf { it > 0 } ?: 0).toString()
        }
    var minorValue: String = "0"
        set(value) {
            field = (value.toIntOrNull()?.takeIf { it > 0 } ?: 0).toString()
        }

    val modes: Map<String, Int> = mapOf(
        "Low power" to AdvertiseSettings.ADVERTISE_MODE_LOW_POWER,
        "Balanced" to AdvertiseSettings.ADVERTISE_MODE_BALANCED,
        "Low latency" to AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
    )
    val txPowerLevels: Map<String, Int> = mapOf(
        "Ultra low" to AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW,
        "Low" to AdvertiseSettings.ADVERTISE_TX_POWER_LOW,
        "Medium" to AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
        "High" to AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
    )

    private val beaconParser = BeaconParser().setBeaconLayout(IBEACON_LAYOUT)
    private val beaconTransmitter = BeaconTransmitter(context, beaconParser)

    fun startBeaconTransmission() {
        actionFunction = {
            beaconTransmitter.startAdvertising(
                Beacon.Builder()
                    .setId1(uuid)
                    .setId2(majorValue)
                    .setId3(minorValue)
                    .setBeaconTypeCode(IBEACON_TYPE_CODE)
                    .setManufacturer(APPLE_INC_MANUFACTURER_ID)
                    .setTxPower(txPower)
                    .setDataFields(dataFields)
                    .build()
            )
            notifyTransmitting()
        }
        doAction()
    }

    fun pauseBeaconTransmission() {
        // NB: There is no built-in pause functionality in Android Beacon Library.
        beaconTransmitter.stopAdvertising()
    }

    fun resumeBeaconTransmission() {
        actionFunction = {
            beaconTransmitter.startAdvertising()
            notifyTransmitting()
        }
        doAction()
    }

    fun stopBeaconTransmission() {
        beaconTransmitter.stopAdvertising()
    }

    fun changeMode(@AdvertiseMode advertiseMode: Int) {
        beaconTransmitter.advertiseMode = advertiseMode
        if (beaconTransmitter.isStarted) {
            notifyTransmitting()
        }
    }

    fun changeTxPowerLevel(@AdvertiseTxPowerLevel advertiseTxPowerLevel: Int) {
        beaconTransmitter.advertiseTxPowerLevel = advertiseTxPowerLevel
        if (beaconTransmitter.isStarted) {
            notifyTransmitting()
        }
    }

    private fun notifyTransmitting() {
        Toast.makeText(
            context,
            context.getString(
                R.string.transmitting,
                modes.filterValues { it == beaconTransmitter.advertiseMode }.keys.first(),
                txPowerLevels.filterValues { it == beaconTransmitter.advertiseTxPowerLevel }.keys.first()
            ),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN
        private const val BLUETOOTH_ADVERTISE = Manifest.permission.BLUETOOTH_ADVERTISE
        // Add if needed.
        // private const val BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT
        // TODO: Handle this permission separately since you cannot request it directly.
        // https://developer.android.com/reference/android/Manifest.permission#ACCESS_BACKGROUND_LOCATION
        // private const val ACCESS_BACKGROUND_LOCATION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }

    private fun doAction() {
        val location = isPermissionGranted(ACCESS_FINE_LOCATION)
        val bluetooth = listOf(
            isPermissionGranted(BLUETOOTH_SCAN),
            isPermissionGranted(BLUETOOTH_ADVERTISE)
        ).all { it }

        when {
            location && bluetooth -> {
                Toast.makeText(context, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
                // TODO: Handle the error appropriately.
                actionFunction?.invoke() ?: error("No action function.")
            }

            // TODO: Handle more rationale cases.
            location && !bluetooth -> {
                if (context.shouldShowRequestPermissionRationale(BLUETOOTH_SCAN) ||
                    context.shouldShowRequestPermissionRationale(BLUETOOTH_ADVERTISE)
                ) {
                    Toast.makeText(
                        context,
                        "TODO: Ask for permission with rationale.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    permissionRequest.launch(arrayOf(BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE))
                }
            }

            else -> permissionRequest.launch(
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    BLUETOOTH_SCAN,
                    BLUETOOTH_ADVERTISE
                )
            )
        }
    }

    private fun doPermissionAction() {
        val location = isPermissionGranted(ACCESS_FINE_LOCATION)
        val bluetooth = listOf(
            isPermissionGranted(BLUETOOTH_SCAN),
            isPermissionGranted(BLUETOOTH_ADVERTISE)
        ).all { it }
        when {
            location && bluetooth -> {
                Toast.makeText(context, R.string.permissions_granted, Toast.LENGTH_SHORT).show()
                // TODO: Handle the error appropriately.
                actionFunction?.invoke() ?: error("No action function.")
            }

            else -> {
                // TODO: Handle the error appropriately.
                Toast.makeText(context, R.string.permissions_rejected, Toast.LENGTH_LONG).show()
                onAllPermissionsRejected()
            }
        }
    }

    private var actionFunction: (() -> Unit)? = null

    private val permissionRequest =
        context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { doPermissionAction() }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    AdvertiseSettings.ADVERTISE_MODE_LOW_POWER,
    AdvertiseSettings.ADVERTISE_MODE_BALANCED,
    AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
)
annotation class AdvertiseMode

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW,
    AdvertiseSettings.ADVERTISE_TX_POWER_LOW,
    AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
    AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
)
annotation class AdvertiseTxPowerLevel