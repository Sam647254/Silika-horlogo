package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import ooo.trankvila.silikahorlogo.komponantoj.DataBar
import ooo.trankvila.silikahorlogo.komponantoj.DataBarFieldData
import ooo.trankvila.silikahorlogo.komponantoj.DataBarItem
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class DataBarViewModel : ViewModel() {
    val current = MutableLiveData(DataBarItem.CurrentWeather)
    val currentData = MutableLiveData<DataBar?>(null)
    val secondData = MutableLiveData<DataBar?>(null)
    val held = MutableLiveData(true)
    private val items = DataBarItem.values()
    private var currentItem = items.size - 1
    private val cache = mutableMapOf<DataBarItem, DataBar>()
    private val cacheTTL = mapOf(
        DataBarItem.CurrentWeather to 900,
        DataBarItem.IndoorConditions to 15
    )
    private lateinit var requestQueue: RequestQueue
    private lateinit var shifter: Runnable
    private lateinit var handler: Handler
    private val fetchers: List<(RequestQueue, (DataBar) -> Unit) -> Unit> = listOf({ queue, cb ->
        // Current weather
        queue.add(JsonObjectRequest(
            "https://api.openweathermap.org/data/2.5/weather?id=5786882&appid=${weatherApiKey}&units=metric",
            null,
            { response ->
                val time = DateTime(
                    response.getLong("dt") * 1000,
                    DateTimeZone.forOffsetMillis(response.getInt("timezone") * 1000)
                )
                val date = SilicanDateTime(
                    SilicanDate.fromGregorian(time.toLocalDate()),
                    SilicanTime.fromStandard(time.toLocalTime())
                )
                val dataBar = DataBar(
                    listOfNotNull(
                        DataBarFieldData(
                            response.getJSONArray("weather").getJSONObject(0).getString("main"),
                            "",
                            "Weather"
                        ),
                        DataBarFieldData(
                            response.getJSONObject("main").getDouble("temp"),
                            "°C",
                            "Temperature"
                        ),
                        DataBarFieldData(
                            response.getJSONObject("main").getInt("humidity"),
                            "%",
                            "Humidity"
                        ),
                        DataBarFieldData(
                            response.getJSONObject("wind").getDouble("speed") * 3.6,
                            "km/h",
                            "${
                                windDirection(
                                    response.getJSONObject("wind").getInt("deg")
                                )
                            } wind"
                        ),
                        response.getJSONObject("wind").optDouble("gust").let { gust ->
                            if (gust.isNaN()) return@let null
                            DataBarFieldData(
                                gust * 3.6,
                                "km/h",
                                "Gust"
                            )
                        },
                        response.optJSONObject("rain")?.optDouble("3h")?.let { rain ->
                            if (rain.isNaN()) return@let null
                            DataBarFieldData(
                                rain,
                                "mm",
                                "3-hour rain"
                            )
                        },
                        response.optJSONObject("snow")?.optDouble("3h")?.let { snow ->
                            if (snow.isNaN()) return@let null
                            DataBarFieldData(
                                snow / 10.0,
                                "cm",
                                "3-hour snow"
                            )
                        }
                    ),
                    "Weather in Bellevue, WA as of $date (OpenWeatherMap.org)"
                )
                cb(dataBar)
            }, { error ->
                Log.e("DataBarViewModel", error.message, error.cause)
            }
        ))
    }, { queue, cb ->
        // Awair data
        queue.add(JsonObjectRequest("http://10.0.0.207/air-data/latest", null, { response ->
            val time = DateTime(response.getString("timestamp"), DateTimeZone.UTC)
            val date = SilicanDateTime.fromLocalDateTime(
                time.withZone(
                    DateTimeZone.forTimeZone(
                        TimeZone.getTimeZone("America/Vancouver")
                    )
                ).toLocalDateTime()
            )
            val dataBar = DataBar(
                listOf(
                    DataBarFieldData(
                        value = response.getInt("score"),
                        label = "Score",
                        unit = "",
                        normalRange = 80..100,
                        warning1Range = 60..100
                    ),
                    DataBarFieldData(
                        value = response.getDouble("temp"),
                        label = "Temperature",
                        unit = "°C",
                        normalRange = 18..25,
                        warning1Range = 16..30
                    ),
                    DataBarFieldData(
                        value = response.getDouble("humid"),
                        label = "Humidity",
                        unit = "%",
                        normalRange = 35..55,
                        warning1Range = 20..65
                    ),
                    DataBarFieldData(
                        value = response.getInt("co2"),
                        label = "Carbon dioxide",
                        unit = "PPM",
                        normalRange = 0..600,
                        warning1Range = 0..1000
                    ),
                    DataBarFieldData(
                        value = response.getInt("voc"),
                        label = "V.O.C.",
                        unit = "PPB",
                        normalRange = 0..333,
                        warning1Range = 0..1000
                    ),
                    DataBarFieldData(
                        value = response.getInt("pm25"),
                        label = "PM2.5",
                        unit = "μg/m³",
                        normalRange = 0..20,
                        warning1Range = 0..55
                    ),
                ),
                caption = "Awair stats as of $date"
            )
            cb(dataBar)
        }, { error ->
            Log.e("DataBarViewModel", error.message, error.cause)
        }))
    }, { queue, cb ->
        // BC stats
        queue.add(
            JsonObjectRequest(
                "https://api.covid19tracker.ca/summary/split",
                null,
                { response ->
                    val data = response.getJSONArray("data")
                    val bcData = (0 until data.length()).first { i ->
                        val province = data.getJSONObject(i)
                        province.getString("province") == "BC"
                    }.let { data.getJSONObject(it) }
                    val population = 5110917
                    val total = bcData.getInt("total_vaccinations")
                    val twice = bcData.getInt("total_vaccinated")
                    val net = total - twice
                    val dataBar = DataBar(
                        listOfNotNull(
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(bcData.getInt("total_cases")),
                                label = "Total cases",
                                unit = ""
                            ),
                            bcData.optInt("change_cases").let { new ->
                                if (new == 0) return@let null
                                DataBarFieldData(
                                    StatisticsViewModel.formatNumber(new),
                                    "",
                                    "New cases",
                                )
                            },
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(total),
                                "",
                                label = "Total vaccinations"
                            ),
                            bcData.optInt("change_vaccinations").let { new ->
                                if (new == 0) return@let null
                                DataBarFieldData(
                                    StatisticsViewModel.formatNumber(new),
                                    "",
                                    "New vaccinations",
                                )
                            },
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(twice),
                                "",
                                label = "Fully vaccinated"
                            ),
                        ),
                        caption = "Stats as of ${
                            LocalDate.parse(bcData.getString("date"))
                                .let(SilicanDate.Companion::fromGregorian)
                        } (COVID-19 Tracker Canada)"
                    )
                    cb(dataBar)
                },
                { error ->
                    Log.e("DataBarViewModel", error.message, error.cause)
                })
        )
    }, { queue, cb ->
        // WA stats
        queue.add(
            JsonObjectRequest(
                "https://api.covidactnow.org/v2/state/WA.json?apiKey=${CovidActNowKey}",
                null,
                { response ->
                    val time = LocalDate.parse(
                        response.getString("lastUpdatedDate"),
                        DateTimeFormat.forPattern("YYYY-MM-dd")
                    )
                    val count = response.getJSONObject("actuals").getInt("vaccinationsInitiated")
                    val ratio =
                        response.getJSONObject("metrics").getDouble("vaccinationsInitiatedRatio")
                    val fullCount =
                        response.getJSONObject("actuals").getInt("vaccinationsCompleted")
                    val fullRatio =
                        response.getJSONObject("metrics").getDouble("vaccinationsCompletedRatio")
                    val dataBar = DataBar(
                        listOf(
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(count),
                                "(%.2f%%)".format(ratio * 100.0),
                                "Partially vaccinated"
                            ),
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(fullCount),
                                "(%.2f%%)".format(fullRatio * 100.0),
                                "Fully vaccinated"
                            ),
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(
                                    response.getJSONObject("actuals").getInt("cases")
                                ),
                                "",
                                "Total cases"
                            ),
                            DataBarFieldData(
                                StatisticsViewModel.formatNumber(
                                    response.getJSONObject("actuals").getInt("newCases")
                                ),
                                "",
                                "New cases"
                            )
                        ),
                        "Stats as of ${SilicanDate.fromGregorian(time)} (CovidActNow.org)"
                    )
                    cb(dataBar)
                },
                { error ->
                    Log.e("DataBarViewModel", error.message, error.cause)
                })
        )
    }, { queue, cb ->
        queue.add(JsonArrayRequest(mealsUrl, { response ->
            val today = LocalDate.now()
            val todayMenu = (0 until response.length()).firstOrNull { i ->
                val date = response.getJSONObject(i).getString("date").let(LocalDate::parse)
                date.equals(today)
            }?.let(response::getJSONObject)
            val tomorrowMenu = (0 until response.length()).firstOrNull { i ->
                val date = response.getJSONObject(i).getString("date").let(LocalDate::parse)
                date.equals(today.plusDays(1))
            }?.let(response::getJSONObject)
            val time = LocalTime.now().hourOfDay
            val todayLunch =
                DataBarFieldData(todayMenu?.optString("lunch") ?: "Unplanned", "", "Today's lunch")
            val todayDinner = DataBarFieldData(
                todayMenu?.optString("dinner") ?: "Unplanned",
                "",
                "Today's dinner"
            )
            val tomorrowLunch =
                DataBarFieldData(
                    tomorrowMenu?.optString("lunch") ?: "Unplanned",
                    "",
                    "Tomorrow's lunch"
                )
            val tomorrowDinner =
                DataBarFieldData(
                    tomorrowMenu?.optString("dinner") ?: "Unplanned",
                    "",
                    "Tomorrow's dinner"
                )
            val menu = when {
                time < 15 -> {
                    DataBar(listOf(todayLunch, todayDinner))
                }
                time < 22 -> {
                    DataBar(listOf(todayDinner, tomorrowLunch))
                }
                else -> {
                    DataBar(listOf(tomorrowLunch, tomorrowDinner))
                }
            }
            cb(menu)
        }, { error ->
            Log.e("DataBarViewModel", error.message, error.cause)
        }))
    })

    fun launch(requestQueue: RequestQueue) {
        this.requestQueue = requestQueue
        if (!::handler.isInitialized) {
            handler = Handler(Looper.getMainLooper())
            cacheTTL.forEach { (item, TTL) ->
                val remover = object : Runnable {
                    override fun run() {
                        fetchers[item.ordinal](requestQueue) { data ->
                            updateDisplay(item, data)
                        }
                        handler.postDelayed(this, TTL * 1000L)
                    }
                }
                remover.run()
            }
            shifter = object : Runnable {
                override fun run() {
                    goToItem((currentItem + 1) % items.size)
                    handler.postDelayed(this, 15_000)
                }
            }
        }
        beginShifter()
    }

    fun holdSelected(selected: DataBarItem) {
        if (held.value == true && selected == current.value) {
            held.postValue(false)
            beginShifter()
        } else {
            held.postValue(true)
            goToItem(selected.ordinal)
            stopShifter()
        }
    }

    fun refresh() {
        val currentItem = current.value ?: return
        cache.remove(currentItem)
        goToItem(currentItem.ordinal)
        if (held.value == false) {
            stopShifter()
            beginShifter()
        }
    }

    fun refreshSecond() {
    }

    private fun beginShifter() {
        // handler.postDelayed(shifter, 15_000)
    }

    private fun stopShifter() {
        // handler.removeCallbacks(shifter)
    }

    private fun goToItem(item: Int) {
        currentItem = item
        current.postValue(items[currentItem])
        if (cache.containsKey(items[currentItem])) {
            currentData.postValue(cache[items[currentItem]]!!)
        } else {
            currentData.postValue(null)
            fetchers[currentItem](requestQueue) { data ->
                updateDisplay(items[currentItem], data)
            }
        }
    }

    private fun updateDisplay(item: DataBarItem, dataBar: DataBar) {
//        if (current.value == item) {
//            currentData.postValue(dataBar)
//        }
        if (item == DataBarItem.CurrentWeather) {
            currentData.postValue(dataBar)
        } else if (item == DataBarItem.IndoorConditions) {
            secondData.postValue(dataBar)
        }
        cache[item] = dataBar
    }
}