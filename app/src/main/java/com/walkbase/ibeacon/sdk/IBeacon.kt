package com.walkbase.ibeacon.sdk

import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import androidx.annotation.IntDef
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter

// https://www.bluetooth.com/specifications/assigned-numbers/
private const val APPLE_INC_MANUFACTURER_ID: Int = 0x004C

// https://github.com/AltBeacon/android-beacon-library/issues/13
private const val IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"
private const val IBEACON_TYPE_CODE: Int = 0x0215

class IBeacon(
    var majorValue: String,
    var minorValue: String,
    private val context: Context,
    private val uuid: String,
    private val beaconLayout: String = IBEACON_LAYOUT,
    private val beaconTypeCode: Int = IBEACON_TYPE_CODE,
    private val dataFields: List<Long> = listOf(0L),
    private val manufacturerId: Int = APPLE_INC_MANUFACTURER_ID,
    // Signal strength at 1 meter measured in dBm. -59 is a typical starting point for BLE devices.
    private val txPower: Int = -59,
) {
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
    private val beaconParser = BeaconParser().setBeaconLayout(beaconLayout)
    private val beaconTransmitter = BeaconTransmitter(context, beaconParser)

    fun startBeaconTransmission(
        @AdvertiseMode advertiseMode: Int? = null,
        @AdvertiseTxPowerLevel advertiseTxPowerLevel: Int? = null
    ) {
        val beacon = Beacon.Builder()
            .setId1(uuid)
            .setId2(majorValue)
            .setId3(minorValue)
            .setBeaconTypeCode(beaconTypeCode)
            .setManufacturer(manufacturerId)
            .setTxPower(txPower)
            .setDataFields(dataFields)
            .build()

        // Use library defaults if omitted.
        if (advertiseMode != null) {
            beaconTransmitter.advertiseMode = advertiseMode
        }
        if (advertiseTxPowerLevel != null) {
            beaconTransmitter.advertiseTxPowerLevel = advertiseTxPowerLevel
        }

        beaconTransmitter.startAdvertising(beacon)
    }

    fun pauseBeaconTransmission() {
        // NB: There is no built-in pause functionality in Android Beacon Library.
        beaconTransmitter.stopAdvertising()
    }

    fun resumeBeaconTransmission() {
        beaconTransmitter.startAdvertising()
    }

    fun stopBeaconTransmission() {
        beaconTransmitter.stopAdvertising()
    }

    fun changeMode(@AdvertiseMode advertiseMode: Int) {
        beaconTransmitter.advertiseMode = advertiseMode
    }

    fun changeTxPowerLevel(@AdvertiseTxPowerLevel advertiseTxPowerLevel: Int) {
        beaconTransmitter.advertiseTxPowerLevel = advertiseTxPowerLevel
    }
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