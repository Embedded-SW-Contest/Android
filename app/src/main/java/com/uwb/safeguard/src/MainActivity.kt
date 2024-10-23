package com.uwb.safeguard.src

import SSEClient
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
import android.content.Intent
import android.view.View
import com.estimote.uwb.api.exceptions.ConnectionTimeout
import com.uwb.safeguard.config.ApplicationClass.Companion.USER_DIST
import com.uwb.safeguard.config.ApplicationClass.Companion.USER_X
import com.uwb.safeguard.config.ApplicationClass.Companion.USER_Y
import com.uwb.safeguard.config.ApplicationClass.Companion.carInfo
import com.uwb.safeguard.config.ApplicationClass.Companion.editor
import com.uwb.safeguard.config.ApplicationClass.Companion.sSharedPreferences
import com.uwb.safeguard.config.BaseActivity
import com.uwb.safeguard.databinding.ActivityMainBinding
import com.uwb.safeguard.src.model.CarResponse
import com.uwb.safeguard.src.model.UserRes
import com.uwb.safeguard.util.ConfirmDialogInterface
import com.uwb.safeguard.util.CustomDialog
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.sin

data class Beacon(val id : String, var dist : Double, val x: Double, val y: Double)

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) , ConfirmDialogInterface , MainActivityInterface {
    val uwbManager = EstimoteUWBFactory.create()
    private var job: Job? = null
    private var a = 0
    private val beacons = mutableListOf<BluetoothDevice>()
    private val connectedDevices = mutableListOf<String>() // 연결된 비콘의 ID 리스트
    private val beaconsDist : MutableMap<String, Double> = mutableMapOf() // 다시 연결될 비콘의 ID 저장용 리스트
    private val beaconList = arrayListOf<Beacon>(
                     Beacon("03:03",987654321.0,1.19,1.35) // 내부 노란색
                    ,Beacon("20:36",987654321.0,0.14,1.35) // 내부 흰색
                    ,Beacon("98:C3",987654321.0,0.665,3.05) // 내부 갈색
                    ,Beacon("04:42",987654321.0,0.0,0.0) // 외부 흰색
                    ,Beacon("19:3A",987654321.0,0.87,0.0) // 외부 노란색
                    ,Beacon("7C:84",987654321.0,0.435,1.85)) // 외부 갈색
    private val distValues = arrayListOf<Beacon>()
    private var p_flag = false
    private var d_flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uwbManager.init(this)

        binding.btnStartUwb.setOnClickListener {
            binding.btnLottie.visibility = View.VISIBLE
            binding.btnStartUwb.visibility = View.GONE
            resetAndRestartUWBScan()
        }
        binding.btnLottie.setOnClickListener {
            binding.btnStartUwb.visibility = View.VISIBLE
            binding.btnLottie.visibility = View.GONE
            uwbManager.disconnectDevice()
            uwbManager.stopDeviceScanning()
        }

        setContentView(binding.root)

        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )

        // Start GpsService
        val gpsServiceIntent = Intent(this, GpsService::class.java)
        startService(gpsServiceIntent)
        Log.d("MainActivity", "GpsService started") // 서비스 시작 로그

        // SSE 클라이언트 초기화 및 시작
        val sseClient = SSEClient("https://uwb-safeguard.shop/api/cars")
        sseClient.startListening()
    }
    private fun startUWBScan() {
        uwbManager.startDeviceScanning(this) // 비콘 스캐닝 시작
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
                    var b_flag = false
                    for(i in 0 until 6){
                        for(j in 0 until 3){
                            if(beaconList[i].id == deviceId){
                                beaconList[i].dist = distance.toDouble()
                                b_flag = true
                                Log.i("UWB_INPUT", "Device: ${beacons[j].address}, Distance: ${beaconList[i].dist}")
                                break;
                            }
                        }
                        if(b_flag) {
                            break
                        }
                    }
                    // 현재 비콘과의 연결을 끊고 다른 비콘과 연결 시도
                    lifecycleScope.launch {
                        disconnectFromBeacon(deviceId)
                        delay(500) // 약간의 지연 후 다시 연결 시도 100 -> 500
                        connectToNextBeacon()
                    }
                }
                is EstimoteUWBRangingResult.Error -> {
                    Log.e("UWB Distance", "Error: ${rangingResult.message}")
                }
                else -> Unit
            }
        }.launchIn(lifecycleScope)

        lifecycleScope.launch {
            delay(5000) // 5초 동안만 스캔
            uwbManager.stopDeviceScanning()
        }
    }

    private suspend fun connectToNextBeacon() {
        val nextBeacon = beacons.find { !connectedDevices.contains(it.address) }
        d_flag = false
        p_flag = false
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

            for(i in beaconList){
                if(i.dist != 987654321.0){
                    Log.i("BeaconList", "추가됨")
                    distValues.add(i)
                }
            }

            val location = calcUserLocation(distValues[0].dist,distValues[1].dist,distValues[2].dist)
            val carUserDist = calCarUserDistance((distValues[0].x + distValues[1].x)/2, 0.0, location.first, location.second)
            Log.i("UWB", "x : ${location.first} , y : ${location.second} , car_user_distance : ${carUserDist}")
            val newGPS = moveInCustomDirection(currentLatitude, currentLongitude, location.first, location.second, carInfo.heading)
            Log.i("NEW_GPS", "userX = ${newGPS.first}, userY = ${newGPS.second}")
            // 사각형 범위 내에 있는지 확인
            if (isPointInRectangle(location.first, location.second)) {
                Log.i("UWB", "User is inside the rectangle. Stopping UWB functions.")
                uwbManager.disconnectDevice()
                uwbManager.stopDeviceScanning()
                resetButtonState()
                return // 사각형 범위 내에 있으면 더 이상 연결 시도하지 않고 종료
            }
            val userRes = UserRes(
                uniNum = "SafeGuard",
                userX = newGPS.first,
                userY = newGPS.second,
                userDist = carUserDist,
                userLat = currentLatitude,
                userLon = currentLongitude,
                userflag = 1
            )
            MainService(this).tryPostUser(userRes)
            if(sSharedPreferences.getFloat(USER_DIST, 0.0F) == 0.0F){ // 처음 거리를 측정한 경우 저장만
                editor.putFloat(USER_X, location.first.toFloat())
                editor.putFloat(USER_Y, location.first.toFloat())
                editor.putFloat(USER_DIST, carUserDist.toFloat())
                editor.apply()
            }else{ // 이전 측정값이 있다면 이전 값과 비교
                Log.i("UWB_LOGGGG", "이전값 있음")
                binding.tvLog.text = "."
                // 사분면 확인 -> 1, 2사분면에 그대로 있는지 또는 벗어났는지
                if(abs(location.first) > 0.2* PI && location.second < 0){
                    Log.i("UWB_LOGGGG", "1, 2 사분면의 0.2PI 이내에 있음")
                    binding.tvLog.text = "."
                    // 거리가 줄었는지 확인
                    if(carUserDist.toFloat() < sSharedPreferences.getFloat(USER_DIST, 0.0F)){ // sSharedPreferences에 저장된 값이 0이 아님은 앞의 if에서 처리함
                        Log.i("UWB_LOGGGG", "거리 줄어듦 확인")
                        binding.tvLog.text = "."
                        // 거리가 줄었다면 user_flag = true로 서버에 업로드
                        if(carInfo.braking_distance > carUserDist){
                            Log.i("UWB_LOGGGG", "RINGINGGGGGGGGG")
                            //binding.tvLog.text = binding.tvLog.text.toString() + "RINGINGGGGGGGGG\n"
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
            var cnt = 0;
            while((!p_flag && !d_flag) && cnt < 10){
                Log.i("DELAY", "정지중 $p_flag , $d_flag")
                delay(1000)
                cnt++
            }
            connectToNextBeacon()

        }
    }

    private fun disconnectFromBeacon(deviceId: String) {
        try {
            uwbManager.disconnectDevice()
            Log.i("UWB", "Disconnected from beacon: $deviceId")
            connectedDevices.remove(deviceId) // 연결된 디바이스 리스트에서 제거
        } catch (e: Exception) {
            Log.e("UWB", "Failed to disconnect from beacon: $deviceId, Error: ${e.message}")
        }
    }

    private fun resetButtonState() {
        // 버튼 상태를 원래대로 복원
        binding.btnLottie.visibility = View.GONE
        binding.btnStartUwb.visibility = View.VISIBLE
        MainService(this).tryDeleteUser("SafeGuard")
    }

    private fun resetAndRestartUWBScan() {
        // 비콘리스트와 거리 데이터 Clear
        beacons.clear()
        beaconsDist.clear()
        connectedDevices.clear()

        Log.i("UWB", "Beacon data cleared. Restarting UWB scan...")

        uwbManager.disconnectDevice()
        uwbManager.stopDeviceScanning()

        lifecycleScope.launch {
            delay(1000)
            startUWBScan() // UWB 탐색 시작
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        showCustomToast("onDestroy")
        // GpsService 멈추기
        val gpsServiceIntent = Intent(this, GpsService::class.java)
        stopService(gpsServiceIntent)
        editor.clear()
        editor.apply()
        job?.cancel()  // 작업 취소
    }

    private fun calcUserLocation(dist1 : Double, dist2 : Double, dist3 : Double): Pair<Double, Double> {
        val A = 2 * (distValues[1].x - distValues[0].x)
        val B = 2 * (distValues[1].y - distValues[0].y)
        val C = dist1 * dist1 - dist2 * dist2 - distValues[0].x * distValues[0].x + distValues[1].x * distValues[1].x - distValues[0].y * distValues[0].y + distValues[1].y * distValues[1].y
        val D = 2 * (distValues[2].x - distValues[1].x)
        val E = 2 * (distValues[2].y - distValues[1].y)
        val F = dist2 * dist2 - dist3 * dist3 - distValues[1].x * distValues[1].x + distValues[2].x * distValues[2].x - distValues[1].y * distValues[1].y + distValues[2].y * distValues[2].y

        val user_x = ((F * B) - (E * C)) / ((B * D) - (E * A))
        val user_y = ((F * A) - (D * C)) / ((A * E) - (D * B))

        return Pair(user_x, user_y)
    }

    // 점이 사각형 내부에 있는지 확인하는 함수
    private fun isPointInRectangle(px: Double, py: Double): Boolean {
        val minX = 0.0
        val maxX = 1.95
        val minY = 0.0
        val maxY = 4.93

        return (px in (minX..maxX)) && (py in (minY..maxY))
    }

    fun calCarUserDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val distance = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
        return distance
    }

    override fun onDialogClick(id: Int) {

    }

    // 사용자 정의 방향을 기준으로 이동할 좌표 계산
    fun moveInCustomDirection(lat: Double, lon: Double, dx: Double, dy: Double, heading: Double): Pair<Double, Double> {
        val earthRadius = 6378137.0 // 지구 반경 (미터)
        // 방위각에 따라 이동 방향 변환
        val theta = Math.toRadians(heading)

        // 사용자가 정의한 기준에 맞게 x, y 좌표 변환
        val adjustedDx = dx * cos(theta) - dy * sin(theta)  // x 방향 (왼쪽/오른쪽 이동)
        val adjustedDy = dx * sin(theta) + dy * cos(theta)  // y 방향 (앞/뒤 이동, y가 -일 때 앞으로 이동)

        // 위도 계산 (y축 이동 반영, 음수 방향은 북쪽으로)
        val newLat = lat + (adjustedDy / earthRadius) * (180 / Math.PI)

        // 경도 계산 (x축 이동 반영, 위도에 따른 경도 변화 고려)
        val newLon = lon + (adjustedDx / (earthRadius * cos(Math.toRadians(lat)))) * (180 / Math.PI)

        return Pair(newLat, newLon)
    }

    override fun onGetCarSuccess(response: List<CarResponse>) {
        Log.i("Embedded_Car","${response[0].uni_num}, ${response[0].car_lat}, ${response[0].car_lon}, ${response[0].braking_distance}")
    }

    override fun onGetCarFailure(message: String) {
        showCustomToast(message)
        Log.i("Embedded_Car_ERROR",message)
    }

    override fun onDeleteUserSuccess(response: ResponseBody) {
        Log.i("Embedded_Car",response.toString())
        d_flag = true
    }

    override fun onDeleteUserFailure(message: String) {
        showCustomToast(message)
        Log.i("Embedded_Car_ERROR",message)
    }

    override fun onPostUserSuccess(response: ResponseBody) {
        Log.i("Embedded_Car",response.toString())
        p_flag = true
    }

    override fun onPostUserFailure(message: String) {
        showCustomToast(message)
        Log.i("Embedded_Car_ERROR",message)
    }
}
