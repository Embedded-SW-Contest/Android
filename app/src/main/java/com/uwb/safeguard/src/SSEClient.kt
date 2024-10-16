import android.util.Log
import com.uwb.safeguard.config.ApplicationClass.Companion.carInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException

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
                        //Log.d("SSE", "First event: $firstObject")
                        carInfo.car_id = firstObject.getInt("car_id").toLong()
                        carInfo.car_lat = firstObject.getDouble("car_lat")
                        carInfo.car_lon = firstObject.getDouble("car_lon")
                        carInfo.uni_num = firstObject.getString("uni_num")
                        carInfo.braking_distance = firstObject.getInt("braking_distance").toDouble()
//                        Log.d("SSE", "카인포: $carInfo")
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
}
