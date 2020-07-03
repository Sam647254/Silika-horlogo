package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.joda.time.LocalDate

private val startOfShelterInPlace = LocalDate(2020, 3, 3)

class WeatherViewModel : ViewModel() {
    val data = MutableLiveData<DataDisplay?>(null)
    val requestUrl =
        "https://api.openweathermap.org/data/2.5/weather?id=5375480&appid=${weatherApiKey}&units=metric"
    var dataDisplays = listOf<DataDisplay>()
    private var next = 0

    fun launch(requestQueue: RequestQueue) {
        val handler = Handler(Looper.getMainLooper())
        val updater = object : Runnable {
            override fun run() {
                data.postValue(dataDisplays[next])
                next = (next + 1) % dataDisplays.size
                handler.postDelayed(this, 12_000)
            }
        }
        val refresher = object : Runnable {
            override fun run() {
                requestQueue.add(JsonObjectRequest(requestUrl, null, { response ->
                    val weatherResponse = WeatherResponse(
                        response.getJSONArray("weather").getJSONObject(0).getInt("id"),
                        response.getJSONArray("weather").getJSONObject(0).getString("main"),
                        response.getJSONObject("main").getDouble("temp"),
                        response.getJSONObject("main").getDouble("temp_min"),
                        response.getJSONObject("main").getDouble("temp_max"),
                        response.getJSONObject("main").getInt("humidity"),
                        response.getJSONObject("wind").getDouble("speed"),
                        response.getJSONObject("wind").getInt("deg"),
                        response.getJSONObject("sys").getLong("sunrise").let(::LocalDate),
                        response.getJSONObject("sys").getLong("sunset").let(::LocalDate)
                    )
                    val start = dataDisplays.isEmpty()
                    dataDisplays = listOf(
                        TextData(weatherResponse.weather, null, "Current conditions"),
                        TextData(
                            "%.1f".format(weatherResponse.temperature),
                            "°C",
                            "Current temperature"
                        )
                    )
                    if (start) updater.run()
                }, {
                    Log.e("WeatherViewModel", it.message, it.cause)
                }))
                handler.postDelayed(this, 3_600_600)
            }
        }
        refresher.run()
    }
}

sealed class DataDisplay
data class TextData(val text1: String, val text2: String?, val caption: String) : DataDisplay()
data class IconData(val drawable: Int, val text: String, val caption: String) : DataDisplay()
data class TimeData(val time: SilicanTime, val caption: String) : DataDisplay()
data class LazyTextData(val text1: () -> String, val text2: (() -> String)?, val caption: String) :
    DataDisplay()

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