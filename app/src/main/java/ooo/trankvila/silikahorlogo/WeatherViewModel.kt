package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.joda.time.LocalDate

class WeatherViewModel : ViewModel() {
    val data = MutableLiveData<WeatherData?>(null)
    val requestUrl = "https://api.openweathermap.org/data/2.5/weather?id=5375480&appid=${weatherApiKey}"
    var response: WeatherResponse? = null

    fun launch(requestQueue: RequestQueue) {
        val handler = Handler(Looper.getMainLooper())
        val refresher = object : Runnable {
            override fun run() {
                requestQueue.add(JsonObjectRequest(requestUrl, null, { response ->
                    this@WeatherViewModel.response = WeatherResponse(
                        response.getJSONArray("weather").getJSONObject(0).getInt("id"),
                        response.getJSONArray("weather").getJSONObject(0).getString("main"),
                        response.getJSONObject("main").getDouble("temp"),
                        response.getJSONObject("main").getDouble("temp_min"),
                        response.getJSONObject("main").getDouble("temp_max"),
                        response.getJSONObject("main").getInt("humidity"),
                        response.getJSONObject("wind").getDouble("speed"),
                        response.getJSONObject("deg").getInt("deg"),
                        response.getJSONObject("sys").getLong("sunrise").let(::LocalDate),
                        response.getJSONObject("sys").getLong("sunset").let(::LocalDate)
                    )
                }, {
                    Log.e("WeatherViewModel", it.message, it.cause)
                }))
                handler.postDelayed(this, 3_600_600)
            }
        }
        refresher.run()
    }
}

sealed class WeatherData
data class TextData(val text1: String, val text2: String, val caption: String): WeatherData()
data class IconData(val drawable: Int, val text: String, val caption: String): WeatherData()
data class TimeData(val time: SilicanTime, val caption: String): WeatherData()

data class WeatherResponse(
    val id: Int,
    val weather: String,
    val temperature: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val wind: Double,
    val windDegree: Int,
    val sunrise: LocalDate,
    val sunset: LocalDate
)