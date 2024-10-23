import android.util.Log
import com.uwb.safeguard.config.ApplicationClass.Companion.CAR_LAT
import com.uwb.safeguard.config.ApplicationClass.Companion.CAR_LON
import com.uwb.safeguard.config.ApplicationClass.Companion.USER_DIST
import com.uwb.safeguard.config.ApplicationClass.Companion.USER_X
import com.uwb.safeguard.config.ApplicationClass.Companion.USER_Y
import com.uwb.safeguard.config.ApplicationClass.Companion.carInfo
import com.uwb.safeguard.config.ApplicationClass.Companion.editor
import com.uwb.safeguard.config.ApplicationClass.Companion.sSharedPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import kotlin.math.*

class SSEClient(private val url: String) {

    private val client = OkHttpClient()

    fun startListening() {
        val request = Request.Builder()
            .url(url)
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d("SSE", "Connection opened")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    // JSON 배열 파싱
                    val jsonArray = JSONArray(data)
                    if (jsonArray.length() > 0) {
                        // 첫 번째 객체 가져오기
                        val firstObject = jsonArray.getJSONObject(0)
                        var heading = 0.0
                        if(sSharedPreferences.getFloat(CAR_LAT, 0.0F) == 0.0F) { // 처음 gps를 측정한 경우 저장만
                            editor.putFloat(CAR_LAT, firstObject.getDouble("car_lat").toFloat())
                            editor.putFloat(CAR_LON, firstObject.getDouble("car_lon").toFloat())
                            editor.apply()
                        }else{
                            heading = calculateHeading(
                                 sSharedPreferences.getFloat(CAR_LAT, 0.0F).toDouble()
                                ,sSharedPreferences.getFloat(CAR_LON, 0.0F).toDouble()
                                ,firstObject.getDouble("car_lat")
                                ,firstObject.getDouble("car_lon"))
                        }

                        //Log.d("SSE", "First event: $firstObject")
                        carInfo.car_id = firstObject.getInt("car_id").toLong()
                        carInfo.car_lat = firstObject.getDouble("car_lat")
                        carInfo.car_lon = firstObject.getDouble("car_lon")
                        carInfo.uni_num = firstObject.getString("uni_num")
                        carInfo.braking_distance = firstObject.getInt("braking_distance").toDouble()
                        carInfo.heading = heading
                        // 현재값 업데이트
                        editor.putFloat(CAR_LAT, firstObject.getDouble("car_lat").toFloat())
                        editor.putFloat(CAR_LON, firstObject.getDouble("car_lon").toFloat())
                        editor.apply()
                        Log.d("SSE", "카인포: $carInfo")
                    } else {
                        Log.d("SSE", "No data available")
                    }
                } catch (e: JSONException) {
                    Log.e("SSE", "JSON parsing error: ${e.message}")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("SSE", "Connection closed")
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e("SSE", "Error: ${t?.message}")
            }
        }

        EventSources.createFactory(client).newEventSource(request, listener)
    }

    fun calculateHeading(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = Math.toRadians(lon2 - lon1)

        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)

        var heading = Math.toDegrees(atan2(y, x))

        // heading 값을 0-360 범위로 변환
        if (heading < 0) {
            heading += 360.0
        }

        return heading
    }
}
