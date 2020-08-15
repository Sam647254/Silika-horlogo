package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

class StatisticsViewModel : ViewModel() {
    private companion object {
        const val DATA_URL =
            "https://wabi-us-gov-virginia-api.analysis.usgovcloudapi.net/public/reports/querydata?synchronous=true"
        const val CASES_PAYLOAD =
            "{\"version\":\"1.0.0\",\"queries\":[{\"Query\":{\"Commands\":[{\"SemanticQueryDataShapeCommand\":{\"Query\":{\"Version\":2,\"From\":[{\"Name\":\"c\",\"Entity\":\"counts\",\"Type\":0}],\"Select\":[{\"Aggregation\":{\"Expression\":{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Total\"}},\"Function\":0},\"Name\":\"Sum(counts.Total)\"}],\"Where\":[{\"Condition\":{\"In\":{\"Expressions\":[{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Category\"}}],\"Values\":[[{\"Literal\":{\"Value\":\"'Cases'\"}}]]}}}]},\"Binding\":{\"Primary\":{\"Groupings\":[{\"Projections\":[0]}]},\"DataReduction\":{\"DataVolume\":3,\"Primary\":{\"Top\":{}}},\"Version\":1}}}]},\"CacheKey\":\"{\\\"Commands\\\":[{\\\"SemanticQueryDataShapeCommand\\\":{\\\"Query\\\":{\\\"Version\\\":2,\\\"From\\\":[{\\\"Name\\\":\\\"c\\\",\\\"Entity\\\":\\\"counts\\\",\\\"Type\\\":0}],\\\"Select\\\":[{\\\"Aggregation\\\":{\\\"Expression\\\":{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Total\\\"}},\\\"Function\\\":0},\\\"Name\\\":\\\"Sum(counts.Total)\\\"}],\\\"Where\\\":[{\\\"Condition\\\":{\\\"In\\\":{\\\"Expressions\\\":[{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Category\\\"}}],\\\"Values\\\":[[{\\\"Literal\\\":{\\\"Value\\\":\\\"'Cases'\\\"}}]]}}}]},\\\"Binding\\\":{\\\"Primary\\\":{\\\"Groupings\\\":[{\\\"Projections\\\":[0]}]},\\\"DataReduction\\\":{\\\"DataVolume\\\":3,\\\"Primary\\\":{\\\"Top\\\":{}}},\\\"Version\\\":1}}}]}\",\"QueryId\":\"\",\"ApplicationContext\":{\"DatasetId\":\"b9bd8aff-3939-4b9b-bb7f-b562bdc492ad\",\"Sources\":[{\"ReportId\":\"8c0bb640-6b65-4ea6-9146-39a7cbad0314\"}]}}],\"cancelQueries\":[],\"modelId\":344061}"
        const val NEW_CASES_PAYLOAD =
            "{\"version\":\"1.0.0\",\"queries\":[{\"Query\":{\"Commands\":[{\"SemanticQueryDataShapeCommand\":{\"Query\":{\"Version\":2,\"From\":[{\"Name\":\"c\",\"Entity\":\"counts\",\"Type\":0}],\"Select\":[{\"Aggregation\":{\"Expression\":{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"New\"}},\"Function\":0},\"Name\":\"Sum(counts.New)\"}],\"Where\":[{\"Condition\":{\"In\":{\"Expressions\":[{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Category\"}}],\"Values\":[[{\"Literal\":{\"Value\":\"'Cases'\"}}]]}}}]},\"Binding\":{\"Primary\":{\"Groupings\":[{\"Projections\":[0]}]},\"DataReduction\":{\"DataVolume\":3,\"Primary\":{\"Top\":{}}},\"Version\":1}}}]},\"CacheKey\":\"{\\\"Commands\\\":[{\\\"SemanticQueryDataShapeCommand\\\":{\\\"Query\\\":{\\\"Version\\\":2,\\\"From\\\":[{\\\"Name\\\":\\\"c\\\",\\\"Entity\\\":\\\"counts\\\",\\\"Type\\\":0}],\\\"Select\\\":[{\\\"Aggregation\\\":{\\\"Expression\\\":{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"New\\\"}},\\\"Function\\\":0},\\\"Name\\\":\\\"Sum(counts.New)\\\"}],\\\"Where\\\":[{\\\"Condition\\\":{\\\"In\\\":{\\\"Expressions\\\":[{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Category\\\"}}],\\\"Values\\\":[[{\\\"Literal\\\":{\\\"Value\\\":\\\"'Cases'\\\"}}]]}}}]},\\\"Binding\\\":{\\\"Primary\\\":{\\\"Groupings\\\":[{\\\"Projections\\\":[0]}]},\\\"DataReduction\\\":{\\\"DataVolume\\\":3,\\\"Primary\\\":{\\\"Top\\\":{}}},\\\"Version\\\":1}}}]}\",\"QueryId\":\"\",\"ApplicationContext\":{\"DatasetId\":\"b9bd8aff-3939-4b9b-bb7f-b562bdc492ad\",\"Sources\":[{\"ReportId\":\"8c0bb640-6b65-4ea6-9146-39a7cbad0314\"}]}}],\"cancelQueries\":[],\"modelId\":344061}"
    }

    val statistic = MutableLiveData<DataDisplay>()
    val graph = MutableLiveData<List<Int>>()
    val cache = mutableMapOf<Int, DataDisplay>()
    private val statistics = listOf({
        val total = fetchData(CASES_PAYLOAD) ?: return@listOf null
        val new = fetchData(NEW_CASES_PAYLOAD) ?: return@listOf null
        TextData(
            formatNumber(total),
            "(${formatNumber(new)} new)",
            "cases in Santa Clara (Santa Clara Public Health)"
        )
    }, {
        val data =
            fetch("https://data.sccgov.org/resource/59wk-iusg.json?city=Mountain View").let(::JSONArray)

        TextData(
            data.getJSONObject(0).getString("cases"), "(${
                data.getJSONObject(0).getString("rate")
            }/100K)", "cases in Mountain View (Santa Clara Open Data Portal)"
        )
    }, {
        val data =
            fetch("https://data.covidactnow.org/latest/us/states/WA.OBSERVED_INTERVENTION.json").let(
                ::JSONObject
            )
        TextData(
            "%.3f".format(data.getJSONObject("projections").getDouble("Rt")),
            null,
            "Washington R-effective as of ${
                LocalDate.parse(data.getString("lastUpdatedDate"))
                    .let { SilicanDate.fromGregorian(it) }.shortDate
            } (CovidActNow.org)"
        )
    }, {
        val data =
            fetch("https://data.covidactnow.org/latest/us/counties/53033.OBSERVED_INTERVENTION.json").let(
                ::JSONObject
            )
        TextData(
            "%.3f".format(data.getJSONObject("projections").getDouble("Rt")),
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
        fetch("https://api.covid19tracker.ca/reports/province/bc?fill_dates&stat=cases").let(::JSONObject).let {
            it.getJSONArray("data").let { data ->
                (data.length() - 1 downTo 0).map { i ->
                    data.getJSONObject(i).optInt("change_cases", 0)
                }
            }
        }

    private fun fetchData(payload: String): Int? {
        val url = URL(DATA_URL)
        return (url.openConnection() as? HttpsURLConnection)?.run {
            requestMethod = "POST"
            doOutput = true
            with(outputStream.bufferedWriter()) {
                write(payload)
                flush()
            }
            inputStream.bufferedReader().readText().let(::JSONObject)
                .getJSONArray("results")
                .getJSONObject(0)
                .getJSONObject("result")
                .getJSONObject("data")
                .getJSONObject("dsr")
                .getJSONArray("DS")
                .getJSONObject(0)
                .getJSONArray("PH")
                .getJSONObject(0)
                .getJSONArray("DM0")
                .getJSONObject(0)
                .getInt("M0")
        }
    }

    private fun formatNumber(it: Int) =
        if (it >= 100_000) "%dK".format((it / 1000.0).roundToInt())
        else if (it >= 10_000) "%,d".format(Locale.CANADA_FRENCH, it)
        else it.toString()
}