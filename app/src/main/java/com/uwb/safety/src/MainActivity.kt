package com.uwb.safety.src

import android.os.Bundle
import android.util.Log
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
import android.view.View
import com.estimote.uwb.api.exceptions.ConnectionTimeout
import com.uwb.safety.config.ApplicationClass
import com.uwb.safety.config.ApplicationClass.Companion.USER_DIST
import com.uwb.safety.config.ApplicationClass.Companion.USER_X
import com.uwb.safety.config.ApplicationClass.Companion.USER_Y
import com.uwb.safety.config.ApplicationClass.Companion.editor
import com.uwb.safety.config.ApplicationClass.Companion.sSharedPreferences
import com.uwb.safety.config.BaseActivity
import com.uwb.safety.databinding.ActivityMainBinding
import com.uwb.safety.src.model.CarRes
import com.uwb.safety.src.model.CarResponse
import com.uwb.safety.src.model.UserRes
import com.uwb.safety.util.ConfirmDialogInterface
import com.uwb.safety.util.CustomDialog
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.pow

//import com.estimote.proximity_sdk.api.EstimoteCloudCredentials
//import com.estimote.proximity_sdk.api.ProximityObserver
//import com.estimote.proximity_sdk.api.ProximityObserverBuilder
//import com.estimote.proximity_sdk.api.ProximityZoneBuilder

//private lateinit var binding: ActivityMainBinding

data class Anchor(val x: Double, val y: Double)

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) , ConfirmDialogInterface , MainActivityInterface {
    private val uwbManager = EstimoteUWBFactory.create()
    //private var bluetoothGatt: BluetoothGatt? = null
    private var job: Job? = null
    private var a = 0
    private val beacons = mutableListOf<BluetoothDevice>()
    private val connectedDevices = mutableListOf<String>() // 연결된 비콘의 ID 리스트
    //private var observationHandler:  ProximityObserver.Handler? = null
    private val beaconsDist : MutableMap<String, Double> = mutableMapOf() // 다시 연결될 비콘의 ID 저장용 리스트

    val AP1 = Anchor(0.0, 0.0)
    val AP2 = Anchor(1.5, 0.0)
    val AP3 = Anchor(0.75, 2.0)

    private lateinit var carInfo : CarResponse
    private var carInfoFlag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uwbManager.init(this)

        binding.btnStartUwb.setOnClickListener {
            binding.btnLottie.visibility = View.VISIBLE
            binding.btnStartUwb.visibility = View.GONE
            uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
            resetAndRestartUWBScan()
        }
        binding.btnLottie.setOnClickListener {
            binding.btnStartUwb.visibility = View.VISIBLE
            binding.btnLottie.visibility = View.GONE
            uwbManager.disconnectDevice()
            uwbManager.stopDeviceScanning()
        }

        setContentView(binding.root)
    }
    private fun startUWBScan() {
        //uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
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
                    if(!carInfoFlag){
                        MainService(this).tryGetCar() // 제동거리 확인을 위한 api 호출 -> 비동기 작업이라 먼저 호출 필요
                        carInfoFlag = true
                    }
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
                        delay(300) // 약간의 지연 후 다시 연결 시도 100 -> 500
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
            val carUserDist = calCarUserDistance((AP1.x + AP2.x)/2, 0.0, location.first, location.second)
            Log.i("UWB", "x : ${location.first} , y : ${location.second} , car_user_distance : ${carUserDist}")
            binding.tvX.text = "X : " + String.format("%.4f", location.first)
            binding.tvY.text = "Y : " + String.format("%.4f", location.second)
            binding.tvDist.text = "Dist : " + String.format("%.4f", carUserDist)

            //Log.i("DBTest", "x : ${sSharedPreferences.getFloat(USER_X, 0.0F).toString()}")
            // 먼저 제동거리이내에 사람이 있는지 확인 해야함. -> 코드 작성 필요
            // if ~

            if(sSharedPreferences.getFloat(USER_DIST, 0.0F) == 0.0F){ // 처음 거리를 측정한 경우 저장만
                editor.putFloat(USER_X, location.first.toFloat())
                editor.putFloat(USER_Y, location.first.toFloat())
                editor.putFloat(USER_DIST, carUserDist.toFloat())
                editor.apply()
            }else{ // 이전 측정값이 있다면 이전 값과 비교
                Log.i("UWB_LOGGGG", "이전값 있음")
                binding.tvLog.text = binding.tvLog.text.toString() + "이전값 있음\n"
                // 사분면 확인 -> 1, 2사분면에 그대로 있는지 또는 벗어났는지
                if(abs(location.first) > 0.2* PI && location.second < 0){
                    Log.i("UWB_LOGGGG", "1, 2 사분면의 0.2PI 이내에 있음")
                    binding.tvLog.text = binding.tvLog.text.toString() + "1, 2 사분면의 0.2PI 이내에 있음\n"
                    // 거리가 줄었는지 확인
                    if(carUserDist.toFloat() < sSharedPreferences.getFloat(USER_DIST, 0.0F)){ // sSharedPreferences에 저장된 값이 0이 아님은 앞의 if에서 처리함
                        Log.i("UWB_LOGGGG", "거리 줄어듦 확인")
                        binding.tvLog.text = binding.tvLog.text.toString() + "거리 줄어듦 확인\n"
                        // 거리가 줄었다면 user_flag = true로 서버에 업로드
                        if(carInfo.braking_distance > carUserDist){
                            Log.i("UWB_LOGGGG", "RINGINGGGGGGGGG")
                            binding.tvLog.text = binding.tvLog.text.toString() + "RINGINGGGGGGGGG\n"
                            val dialog = CustomDialog(this, 1234)
                            // 알림창이 띄워져있는 동안 배경 클릭 막기
                            dialog.isCancelable = false
                            dialog.show(this.supportFragmentManager, "ConfirmDialog")
                            //alarm.ringAlarm()
                            connectedDevices.clear()
                            connectToNextBeacon()
                        }
                    }
                }
            }
            // 모든 처리 끝내고 저장해야함
            editor.putFloat(USER_X, location.first.toFloat())
            editor.putFloat(USER_Y, location.first.toFloat())
            editor.putFloat(USER_DIST, carUserDist.toFloat())
            editor.apply()
            connectedDevices.clear()
            connectToNextBeacon()
//            if(location.first < 0 || location.first > 1.5 || location.second < 0 || location.second > 2.0){
////                val dialog = CustomDialog(this, 1234)
////                // 알림창이 띄워져있는 동안 배경 클릭 막기
////                dialog.isCancelable = false
////                dialog.show(this.supportFragmentManager, "ConfirmDialog")
////                //alarm.ringAlarm()
////                connectedDevices.clear()
////                connectToNextBeacon()
//                //showCustomToast("instead.... running")
//            }else if(location.first > 0 || location.first < 1.5 || location.second > 0 || location.second < 2.0){
//                uwbManager.disconnectDevice() // 차량 내부에 있다고 판단
//                uwbManager.stopDeviceScanning() // UWB 기능 종료
//            }else{
//                connectedDevices.clear()
//                connectToNextBeacon()
//            }

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

    private fun resetAndRestartUWBScan() {
        // Clear beacons list and distance data
        beacons.clear()
        beaconsDist.clear()
        connectedDevices.clear()

        // Log the reset event
        Log.i("UWB", "Beacon data cleared. Restarting UWB scan...")

        // Restart the scanning process
        uwbManager.disconnectDevice()
        uwbManager.stopDeviceScanning()

        lifecycleScope.launch {
            delay(1000) // Optional delay to ensure proper reset
            startUWBScan() // Restart the UWB scan
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showCustomToast("onDestroy")
        editor.clear()
        editor.apply()
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

    fun calCarUserDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val distance = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
        return distance
    }

//    private val bluetoothGattCallback = object : BluetoothGattCallback() {
//        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                // successfully connected to the GATT Server
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                // disconnected from the GATT Server
//            }
//        }
//    }

    override fun onDialogClick(id: Int) {
        //finish()
    }

    override fun onGetCarSuccess(response: List<CarResponse>) {
        carInfo = CarResponse(response[0].car_id, response[0].car_lat, response[0].car_lon, response[0].uni_num, response[0].braking_distance)
        Log.i("Embedded_Car","${response[0].uni_num}, ${response[0].car_lat}, ${response[0].car_lon}, ${response[0].braking_distance}")
    }

    override fun onGetCarFailure(message: String) {
        showCustomToast(message)
        Log.i("Embedded_Car_ERROR",message.toString())
    }
}
