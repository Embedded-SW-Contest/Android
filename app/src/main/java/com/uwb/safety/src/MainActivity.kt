package com.uwb.safety.src

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.view.View
import com.estimote.uwb.api.exceptions.ConnectionTimeout
import com.uwb.safety.config.BaseActivity
import com.uwb.safety.databinding.ActivityMainBinding
import com.uwb.safety.util.ConfirmDialogInterface
import com.uwb.safety.util.CustomDialog
import kotlinx.coroutines.delay

//import com.estimote.proximity_sdk.api.EstimoteCloudCredentials
//import com.estimote.proximity_sdk.api.ProximityObserver
//import com.estimote.proximity_sdk.api.ProximityObserverBuilder
//import com.estimote.proximity_sdk.api.ProximityZoneBuilder

//private lateinit var binding: ActivityMainBinding

data class Anchor(val x: Double, val y: Double)

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) , ConfirmDialogInterface {
    private val uwbManager = EstimoteUWBFactory.create()
    //private var bluetoothGatt: BluetoothGatt? = null
    private var job: Job? = null
    private var distJob: Job? = null
    private var isConnected = false
    private val map: MutableMap<String, Int> = mutableMapOf()
    private var a = 0
    private val beacons = mutableListOf<BluetoothDevice>()
    private val connectedDevices = mutableListOf<String>() // 연결된 비콘의 ID 리스트
    //private var observationHandler:  ProximityObserver.Handler? = null
    private val beaconsDist : MutableMap<String, Double> = mutableMapOf() // 다시 연결될 비콘의 ID 저장용 리스트

    val AP1 = Anchor(0.0, 0.0)
    val AP2 = Anchor(2.0, 0.0)
    val AP3 = Anchor(1.0, 1.5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivityMainBinding.inflate(layoutInflater)
        uwbManager.init(this)


        binding.btnStartUwb.setOnClickListener {
            binding.btnLottie.visibility = View.VISIBLE
            binding.btnStartUwb.visibility = View.GONE
            uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
            startUWBScan()
        }
        binding.btnLottie.setOnClickListener {
            binding.btnStartUwb.visibility = View.VISIBLE
            binding.btnLottie.visibility = View.GONE
            uwbManager.disconnectDevice()
            uwbManager.stopDeviceScanning()
            beacons.clear()
        }

        setContentView(binding.root)
    }
    private fun startUWBScan() {
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

                    beaconsDist[deviceId] = distance.toDouble()

                    // 현재 비콘과의 연결을 끊고 다른 비콘과 연결 시도
                    lifecycleScope.launch {
                        disconnectFromBeacon(deviceId)
//                        val permission = Manifest.permission.BLUETOOTH_CONNECT
//                        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
//                            // 권한이 없는 경우
//                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), 1111)
//                        } else {
//                            bluetoothGatt?.disconnect()
//                            bluetoothGatt?.close()
//                        }
                        //bluetoothGatt = null // GATT 객체를 해제하여 새로운 연결에 대비
                        connectToNextBeacon()
                    }
                }
                is EstimoteUWBRangingResult.Error -> {
                    Log.e("UWB Distance", "Error: ${rangingResult.message}")
                }
                else -> Unit
            }
        }.launchIn(lifecycleScope)

        //uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
        uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
        lifecycleScope.launch {
            delay(5000) // 5초 동안만 스캔
            uwbManager.stopDeviceScanning()
        }
    }

    private fun connectToNextBeacon() {
        val nextBeacon = beacons.find { !connectedDevices.contains(it.address) }
        if (nextBeacon != null) {
            lifecycleScope.launch {
                try {
                    uwbManager.connect(nextBeacon, this@MainActivity)
//                    val permission = Manifest.permission.BLUETOOTH_CONNECT
//                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
//                        // 권한이 없는 경우
//                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), 1111)
//                    } else {
//                        bluetoothGatt = nextBeacon.connectGatt(this@MainActivity, false, bluetoothGattCallback)
//                    }
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
            val location = calcUserLocation(beaconsDist["04:42"]!!, beaconsDist["19:3A"]!!, beaconsDist["7C:84"]!!)
            Log.i("UWB", "x : ${location.first} , y : ${location.second}")
            if(location.first < 0 || location.first > 2.0 || location.second < 0 || location.second > 1.5){
                val dialog = CustomDialog(this, 1234)
                // 알림창이 띄워져있는 동안 배경 클릭 막기
                dialog.isCancelable = false
                dialog.show(this.supportFragmentManager, "ConfirmDialog")
                //alarm.ringAlarm()
            }
            connectedDevices.clear()
            connectToNextBeacon()

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
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.UWB_RANGING
//                Manifest.permission.BLUETOOTH,
//                Manifest.permission.BLUETOOTH_ADMIN,
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.UWB_RANGING,
//                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )
    }
    private fun calcUserLocation(dist1 : Double, dist2 : Double, dist3 : Double): Pair<Double, Double> {
        val A = 2 * (AP2.x - AP1.x)
        val B = 2 * (AP2.y - AP1.y)
        val C = dist1 * dist1 - dist2 * dist2 - AP1.x * AP1.x + AP2.x * AP2.x - AP1.y * AP1.y + AP2.y * AP2.y
        val D = 2 * (AP3.x - AP2.x)
        val E = 2 * (AP3.y - AP2.y)
        val F = dist2 * dist2 - dist3 * dist3 - AP2.x * AP2.x + AP3.x * AP3.x - AP2.y * AP2.y + AP3.y * AP3.y

        val user_x = ((F * B) - (E * C)) / ((B * D) - (E * A))
        val user_y = ((F * A) - (D * C)) / ((A * E) - (D * B))

        return Pair(user_x, user_y)
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }
    }

    override fun onDialogClick(id: Int) {
        //finish()
    }
}
