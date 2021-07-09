package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

class StatisticsViewModel : ViewModel() {
    val statistic = MutableLiveData<DataDisplay>()
    val graph = MutableLiveData<List<Double>>()
    val graph2 = MutableLiveData<List<Double>>()
    val cache = mutableMapOf<Int, DataDisplay>()
    private val responseCache = mutableMapOf<String, String>()

    private val statistics = listOf({
        val data =
            fetch("https://api.covidactnow.org/v2/state/WA.json?apiKey=${CovidActNowKey}").let(
                ::JSONObject
            )
        val count = data.getJSONObject("actuals").getInt("vaccinationsInitiated")
        val ratio = data.getJSONObject("metrics").getDouble("vaccinationsInitiatedRatio")
        TextData(
            formatNumber(count),
            "(%.2f%%)".format(ratio * 100),
            "Washington partially vaccinated population as of ${
                LocalDate.parse(data.getString("lastUpdatedDate"))
                    .let { SilicanDate.fromGregorian(it) }.shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data =
            fetch("https://api.covidactnow.org/v2/state/WA.json?apiKey=${CovidActNowKey}").let(
                ::JSONObject
            )
        val count = data.getJSONObject("actuals").getInt("vaccinationsCompleted")
        val ratio = data.getJSONObject("metrics").getDouble("vaccinationsCompletedRatio")
        TextData(
            formatNumber(count),
            "(%.2f%%)".format(ratio * 100),
            "Washington fully vaccinated population as of ${
                LocalDate.parse(data.getString("lastUpdatedDate"))
                    .let { SilicanDate.fromGregorian(it) }.shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data =
            fetch("https://api.covidactnow.org/v2/state/WA.json?apiKey=${CovidActNowKey}").let(::JSONObject)
        val total = data.getJSONObject("actuals").getInt("cases").let(::formatNumber)
        val new =
            data.getJSONObject("actuals").getInt("newCases").let { if (it > 0) it else null }?.let(::formatNumber)
        TextData(
            total,
            if (new != null) "($new new)" else null,
            "cases in Washington as of ${
                data.getString("lastUpdatedDate").let {
                    LocalDate.parse(it, DateTimeFormat.forPattern("YYYY-MM-dd"))
                }.let(SilicanDate.Companion::fromGregorian).shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data =
            fetch("https://api.covidactnow.org/v2/county/53033.json?apiKey=${CovidActNowKey}").let(::JSONObject)
        val total = data.getJSONObject("actuals").getInt("cases").let(::formatNumber)
        val new =
            data.getJSONObject("actuals").getInt("newCases").let { if (it > 0) it else null }?.let(::formatNumber)
        TextData(
            total,
            if (new != null) "($new new)" else null,
            "cases in King County as of ${
                data.getString("lastUpdatedDate").let {
                    LocalDate.parse(it, DateTimeFormat.forPattern("YYYY-MM-dd"))
                }.let(SilicanDate.Companion::fromGregorian).shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data = fetch("https://api.covid19tracker.ca/summary/split").let(::JSONObject)
            .getJSONArray("data")
        val bcData = (0 until data.length()).first { i ->
            val province = data.getJSONObject(i)
            province.getString("province") == "BC"
        }.let { data.getJSONObject(it) }
        val total = bcData.getInt("total_cases")
        val new = bcData.optInt("change_cases")
        TextData(
            formatNumber(total),
            if (new != 0) "(${formatNumber(new)} new)" else null,
            "cases in BC as of ${
                LocalDate.parse(bcData.getString("date"))
                    .let(SilicanDate.Companion::fromGregorian).shortDate
            } (COVID-19 Tracker Canada)"
        )
    }, {
        val data = fetch("https://api.covid19tracker.ca/summary/split").let(::JSONObject)
            .getJSONArray("data")
        val bcData = (0 until data.length()).first { i ->
            val province = data.getJSONObject(i)
            province.getString("province") == "BC"
        }.let { data.getJSONObject(it) }
        val population = 5110917
        val total = bcData.getInt("total_vaccinations")
        val twice = bcData.getInt("total_vaccinated")
        val net = total - twice
        TextData(
            formatNumber(net),
            "(%.2f%%)".format(net * 100.0 / population),
            "Vaccinated population in BC as of ${
                LocalDate.parse(bcData.getString("date"))
                    .let(SilicanDate.Companion::fromGregorian).shortDate
            } (COVID-19 Tracker Canada)"
        )
    })
    private var statisticIndex = 0

    init {
        val handler = Handler(Looper.getMainLooper())

        val statisticUpdater = object : Runnable {
            override fun run() {
                if (cache.containsKey(statisticIndex)) {
                    statistic.postValue(cache[statisticIndex])
                    statisticIndex = (statisticIndex + 1) % statistics.size
                } else {
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            statistics[statisticIndex]()?.let {
                                cache[statisticIndex] = it
                                statistic.postValue(it)
                                statisticIndex = (statisticIndex + 1) % statistics.size
                            }
                        }
                    }
                }
                handler.postDelayed(this, 10000)
            }
        }
        statisticUpdater.run()

        fetchGraph()
    }

    fun refresh() {
        cache.clear()
        responseCache.clear()
    }

    private fun fetch(url: String) = responseCache.computeIfAbsent(url) {
        (URL(url).openConnection() as HttpsURLConnection).run {
            requestMethod = "GET"
            inputStream.bufferedReader().readText()
        }
    }

    fun fetchGraph() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Log.d("StatisticsViewModel", "Fetching graph data")
                fetch("https://api.covidactnow.org/v2/state/WA.timeseries.json?apiKey=${CovidActNowKey}").let(
                    ::JSONObject
                )
                    .let {
                        it.getJSONArray("metricsTimeseries").let { data ->
                            (data.length() - 1 downTo 0).map { i ->
                                data.getJSONObject(i).optDouble("caseDensity", 0.0)
                            }
                        }
                    }.let(graph::postValue)
                fetch("https://api.covid19tracker.ca/reports/province/bc?fill_dates&stat=cases").let(
                    ::JSONObject
                )
                    .let {
                        it.getJSONArray("data").let { data ->
                            (data.length() - 1 downTo 0).map { i ->
                                data.getJSONObject(i).optDouble("change_cases", 0.0)
                            }
                        }
                    }.let(graph2::postValue)
            }
        }
    }

    companion object {
        fun formatNumber(it: Int) =
            when {
                it >= 1_000_000 -> "%.2fM".format(it / 1_000_000.0)
                it >= 100_000 -> "%dK".format((it / 1000.0).roundToInt())
                it >= 10_000 -> "%,d".format(Locale.CANADA_FRENCH, it)
                else -> it.toString()
            }
    }
}