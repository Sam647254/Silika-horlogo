package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
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
    val graph = MutableLiveData<List<Int>>()
    val cache = mutableMapOf<Int, DataDisplay>()
    private val statistics = listOf({
        val data = fetch("https://api.covid19tracker.ca/summary/split").let(::JSONObject)
            .getJSONArray("data")
        val bcData = (0 until data.length()).first { i ->
            val province = data.getJSONObject(i)
            province.getString("province") == "BC"
        }.let { data.getJSONObject(it) }
        val total = bcData.getInt("total_cases")
        val new = bcData.optInt("change_cases", 0)
        TextData(
            formatNumber(total),
            "(${formatNumber(new)} new)",
            "cases in BC as of ${
                LocalDate.parse(bcData.getString("date"))
                    .let(SilicanDate.Companion::fromGregorian).shortDate
            } (COVID-19 Tracker Canada)"
        )
    }, {
        val data =
            fetch("https://api.covidactnow.org/v2/state/WA.json?apiKey=${CovidActNowKey}").let(
                ::JSONObject
            )
        TextData(
            data.getJSONObject("metrics").getDouble("infectionRate").let { "%.3f".format(it) },
            null,
            "Washington R-effective as of ${
                LocalDate.parse(data.getString("lastUpdatedDate"))
                    .let { SilicanDate.fromGregorian(it) }.shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data =
            fetch("https://api.covidactnow.org/v2/county/53033.json?apiKey=${CovidActNowKey}").let(
                ::JSONObject
            )
        TextData(
            data.getJSONObject("metrics").getDouble("infectionRate").let { "%.3f".format(it) },
            null,
            "King County R-effective as of ${
                LocalDate.parse(data.getString("lastUpdatedDate"))
                    .let(SilicanDate.Companion::fromGregorian).shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data =
            fetch("https://covidtracking.com/api/v1/states/wa/current.json").let(::JSONObject)
        val total = data.getInt("positive").let(::formatNumber)
        val new = data.getInt("positiveIncrease").let(::formatNumber)
        TextData(
            total,
            "($new new)",
            "cases in Washington as of ${
                data.getInt("date").let {
                    LocalDate.parse(it.toString(), DateTimeFormat.forPattern("YYYYMMdd"))
                }.let(SilicanDate.Companion::fromGregorian).shortDate
            } (The COVID Tracking Project)"
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

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fetchGraph().let(graph::postValue)
            }
        }
    }

    private fun fetch(url: String) =
        (URL(url).openConnection() as? HttpsURLConnection)?.run {
            requestMethod = "GET"
            addRequestProperty("X-App-Token", SocrataAppToken)
            inputStream.bufferedReader().readText()
        }

    private fun fetchGraph() =
        fetch("https://api.covid19tracker.ca/reports/province/bc?fill_dates&stat=cases").let(::JSONObject)
            .let {
                it.getJSONArray("data").let { data ->
                    (data.length() - 1 downTo 0).map { i ->
                        data.getJSONObject(i).optInt("change_cases", 0)
                    }
                }
            }

    private fun formatNumber(it: Int) =
        if (it >= 100_000) "%dK".format((it / 1000.0).roundToInt())
        else if (it >= 10_000) "%,d".format(Locale.CANADA_FRENCH, it)
        else it.toString()
}