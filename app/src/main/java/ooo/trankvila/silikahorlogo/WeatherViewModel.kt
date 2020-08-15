package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

private val startOfShelterInPlace = LocalDate(2020, 3, 3)

class WeatherViewModel : ViewModel() {
    val data = MutableLiveData<DataDisplay?>(null)
    val requestUrl =
        "https://api.openweathermap.org/data/2.5/weather?id=6159905&appid=${weatherApiKey}&units=metric"
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
                        response.getJSONObject("sys").getLong("sunrise").let { it * 1000 }
                            .let(::LocalDateTime),
                        response.getJSONObject("sys").getLong("sunset").let { it * 1000 }
                            .let(::LocalDateTime)
                    )
                    val start = dataDisplays.isEmpty()
                    dataDisplays = listOf(
                        TextData(weatherResponse.weather, null, "Current conditions"),
                        TextData(
                            "%.1f".format(weatherResponse.temperature),
                            "°C",
                            "Current temperature"
                        ),
                        TextData(weatherResponse.humidity.toString(), "%", "Relative humidity"),
                        TextData(
                            "%.1f".format(weatherResponse.wind),
                            "km/h", "${windDirection(weatherResponse.windDegree)} wind"
                        ),
                        TimeData(weatherResponse.sunrise.toLocalTime(), "Sunrise"),
                        TimeData(weatherResponse.sunset.toLocalTime(), "Sunset"),
                        LazyTextData({
                            Days.daysBetween(startOfShelterInPlace, LocalDate.now()).days.toString()
                        }, null, "days of shelter-in-place")
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
data class TimeData(val time: LocalTime, val caption: String) : DataDisplay()
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
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime
)

private fun windDirection(degree: Int) = when (degree) {
    in 0..25, in 335..359 -> "Northerly"
    in 26..65 -> "North-easterly"
    in 66..115 -> "Easterly"
    in 116..155 -> "South-easterly"
    in 156..205 -> "Southerly"
    in 206..245 -> "South-westerly"
    in 246..305 -> "Westerly"
    else -> "North-westerly"
}