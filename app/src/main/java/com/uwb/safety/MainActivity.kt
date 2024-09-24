package com.uwb.safety

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.estimote.uwb.api.EstimoteUWBFactory
import com.estimote.uwb.api.ranging.EstimoteUWBRangingResult
import com.estimote.uwb.api.scanning.EstimoteUWBScanResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import android.Manifest
import android.bluetooth.BluetoothDevice
import com.estimote.uwb.api.exceptions.ConnectionTimeout
import com.uwb.safety.databinding.ActivityMainBinding
import kotlinx.coroutines.delay

//import com.estimote.proximity_sdk.api.EstimoteCloudCredentials
//import com.estimote.proximity_sdk.api.ProximityObserver
//import com.estimote.proximity_sdk.api.ProximityObserverBuilder
//import com.estimote.proximity_sdk.api.ProximityZoneBuilder

private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val uwbManager = EstimoteUWBFactory.create()
    private var job: Job? = null
    private var distJob: Job? = null
    private var isConnected = false
    private val map: MutableMap<String, Int> = mutableMapOf()
    private var a = 0
    private val beacons = mutableListOf<BluetoothDevice>()
    private val connectedDevices = mutableListOf<String>() // 연결된 비콘의 ID 리스트
    //private var observationHandler:  ProximityObserver.Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uwbManager.init(this)

        // UWB 디바이스 스캔 시작
        uwbManager.uwbDevices.onEach { scanResult: EstimoteUWBScanResult ->
            when (scanResult) {
                is EstimoteUWBScanResult.Devices -> {
                    Log.i("UWB", "Found ${scanResult.devices.size} UWB Beacons")

                    if (scanResult.devices.size >= 3 && beacons.isEmpty()) {
                        beacons.addAll(scanResult.devices.take(3).mapNotNull { it.device })
                        Log.i("UWB", "3개 비콘 탐색 완료: ${beacons.map { it.address }}")
                        connectToNextBeacon()
                    }
                }
                is EstimoteUWBScanResult.Error -> {
                    Log.e("UWB SCAN", "Error: ${scanResult.errorCode}")
                }
                EstimoteUWBScanResult.ScanNotStarted -> {
                    Log.i("UWB", "Error: scan not started")
                }
            }

        }.launchIn(lifecycleScope)

        uwbManager.rangingResult.onEach { rangingResult ->
            when (rangingResult) {
                is EstimoteUWBRangingResult.Position -> {
                    // 각 비콘의 거리 및 방위각을 표시
                    val deviceId = rangingResult.device.address.toString()
                    val distance = rangingResult.position.distance?.value.toString()
                    val azimuth = rangingResult.position.azimuth?.value.toString()
                    val elevation = rangingResult.position.elevation?.value.toString()

                    Log.i("UWB", "Device: $deviceId, Distance: $distance, Azimuth: $azimuth, Elevation: $elevation")

                    // 현재 비콘과의 연결을 끊고 다른 비콘과 연결 시도
                    lifecycleScope.launch {
                        disconnectFromBeacon(deviceId)
                        delay(500) // 약간의 지연 후 다시 연결 시도
                        connectToNextBeacon()
                    }
                }
                is EstimoteUWBRangingResult.Error -> {
                    Log.e("UWB Distance", "Error: ${rangingResult.message}")
                }
                else -> Unit
            }
        }.launchIn(lifecycleScope)

        uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
    }

    private fun connectToNextBeacon() {
        val nextBeacon = beacons.find { !connectedDevices.contains(it.address) }
        if (nextBeacon != null) {
            lifecycleScope.launch {
                try {
                    uwbManager.connect(nextBeacon, this@MainActivity)
                    connectedDevices.add(nextBeacon.address)
                    Log.i("UWB", "Connected to beacon: ${nextBeacon.address}")
                } catch (e: ConnectionTimeout) {
                    Log.e("UWB", "Connection timeout for beacon: ${nextBeacon.address}, Error: ${e.message}")
                    connectToNextBeacon() // 연결 실패 시 다음 비콘으로 시도
                } catch (e: Exception) {
                    Log.e("UWB", "Failed to connect to beacon: ${nextBeacon.address}, Error: ${e.message}")
                    connectToNextBeacon() // 연결 실패 시 다음 비콘으로 시도
                }
            }
        } else {
            Log.i("UWB", "No more beacons to connect to.")
        }
    }

    private suspend fun disconnectFromBeacon(deviceId: String) {
        try {
            uwbManager.disconnectDevice()
            Log.i("UWB", "Disconnected from beacon: $deviceId")
            connectedDevices.remove(deviceId) // 연결된 디바이스 리스트에서 제거
        } catch (e: Exception) {
            Log.e("UWB", "Failed to disconnect from beacon: $deviceId, Error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()  // 작업 취소
    }

    override fun onStart() {
        super.onStart()
        requestPermissions(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.UWB_RANGING,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }
}
