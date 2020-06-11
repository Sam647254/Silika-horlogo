package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class StatisticsViewModel: ViewModel() {
    private companion object {
        const val DATA_URL =
            "https://wabi-us-gov-virginia-api.analysis.usgovcloudapi.net/public/reports/querydata?synchronous=true"
        const val CASES_PAYLOAD =
            "{\"version\":\"1.0.0\",\"queries\":[{\"Query\":{\"Commands\":[{\"SemanticQueryDataShapeCommand\":{\"Query\":{\"Version\":2,\"From\":[{\"Name\":\"c\",\"Entity\":\"counts\",\"Type\":0}],\"Select\":[{\"Aggregation\":{\"Expression\":{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Total\"}},\"Function\":0},\"Name\":\"Sum(counts.Total)\"}],\"Where\":[{\"Condition\":{\"In\":{\"Expressions\":[{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Category\"}}],\"Values\":[[{\"Literal\":{\"Value\":\"'Cases'\"}}]]}}}]},\"Binding\":{\"Primary\":{\"Groupings\":[{\"Projections\":[0]}]},\"DataReduction\":{\"DataVolume\":3,\"Primary\":{\"Top\":{}}},\"Version\":1}}}]},\"CacheKey\":\"{\\\"Commands\\\":[{\\\"SemanticQueryDataShapeCommand\\\":{\\\"Query\\\":{\\\"Version\\\":2,\\\"From\\\":[{\\\"Name\\\":\\\"c\\\",\\\"Entity\\\":\\\"counts\\\",\\\"Type\\\":0}],\\\"Select\\\":[{\\\"Aggregation\\\":{\\\"Expression\\\":{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Total\\\"}},\\\"Function\\\":0},\\\"Name\\\":\\\"Sum(counts.Total)\\\"}],\\\"Where\\\":[{\\\"Condition\\\":{\\\"In\\\":{\\\"Expressions\\\":[{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Category\\\"}}],\\\"Values\\\":[[{\\\"Literal\\\":{\\\"Value\\\":\\\"'Cases'\\\"}}]]}}}]},\\\"Binding\\\":{\\\"Primary\\\":{\\\"Groupings\\\":[{\\\"Projections\\\":[0]}]},\\\"DataReduction\\\":{\\\"DataVolume\\\":3,\\\"Primary\\\":{\\\"Top\\\":{}}},\\\"Version\\\":1}}}]}\",\"QueryId\":\"\",\"ApplicationContext\":{\"DatasetId\":\"b9bd8aff-3939-4b9b-bb7f-b562bdc492ad\",\"Sources\":[{\"ReportId\":\"8c0bb640-6b65-4ea6-9146-39a7cbad0314\"}]}}],\"cancelQueries\":[],\"modelId\":344061}"
        const val NEW_CASES_PAYLOAD =
            "{\"version\":\"1.0.0\",\"queries\":[{\"Query\":{\"Commands\":[{\"SemanticQueryDataShapeCommand\":{\"Query\":{\"Version\":2,\"From\":[{\"Name\":\"c\",\"Entity\":\"counts\",\"Type\":0}],\"Select\":[{\"Aggregation\":{\"Expression\":{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"New\"}},\"Function\":0},\"Name\":\"Sum(counts.New)\"}],\"Where\":[{\"Condition\":{\"In\":{\"Expressions\":[{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Category\"}}],\"Values\":[[{\"Literal\":{\"Value\":\"'Cases'\"}}]]}}}]},\"Binding\":{\"Primary\":{\"Groupings\":[{\"Projections\":[0]}]},\"DataReduction\":{\"DataVolume\":3,\"Primary\":{\"Top\":{}}},\"Version\":1}}}]},\"CacheKey\":\"{\\\"Commands\\\":[{\\\"SemanticQueryDataShapeCommand\\\":{\\\"Query\\\":{\\\"Version\\\":2,\\\"From\\\":[{\\\"Name\\\":\\\"c\\\",\\\"Entity\\\":\\\"counts\\\",\\\"Type\\\":0}],\\\"Select\\\":[{\\\"Aggregation\\\":{\\\"Expression\\\":{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"New\\\"}},\\\"Function\\\":0},\\\"Name\\\":\\\"Sum(counts.New)\\\"}],\\\"Where\\\":[{\\\"Condition\\\":{\\\"In\\\":{\\\"Expressions\\\":[{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Category\\\"}}],\\\"Values\\\":[[{\\\"Literal\\\":{\\\"Value\\\":\\\"'Cases'\\\"}}]]}}}]},\\\"Binding\\\":{\\\"Primary\\\":{\\\"Groupings\\\":[{\\\"Projections\\\":[0]}]},\\\"DataReduction\\\":{\\\"DataVolume\\\":3,\\\"Primary\\\":{\\\"Top\\\":{}}},\\\"Version\\\":1}}}]}\",\"QueryId\":\"\",\"ApplicationContext\":{\"DatasetId\":\"b9bd8aff-3939-4b9b-bb7f-b562bdc492ad\",\"Sources\":[{\"ReportId\":\"8c0bb640-6b65-4ea6-9146-39a7cbad0314\"}]}}],\"cancelQueries\":[],\"modelId\":344061}"
    }

    val statistic = MutableLiveData<Statistic>()
    val cache = mutableMapOf<Int, Statistic>()
    private val statistics = listOf({
        val total = fetchData(CASES_PAYLOAD) ?: return@listOf null
        val new = fetchData(NEW_CASES_PAYLOAD) ?: return@listOf null
        Statistic(total.toString(), "($new new)", "cases in Santa Clara")
    })
    private var statisticIndex = 0

    init {
        val handler = Handler(Looper.getMainLooper())

        val statisticUpdater = object : Runnable {
            override fun run() {
                if (cache.containsKey(statisticIndex)) {
                    statistic.postValue(cache[statisticIndex])
                } else {
                    viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            statistics[statisticIndex]()?.let {
                                cache[statisticIndex] = it
                                statistic.postValue(it)
                            }
                        }
                    }
                }
                statisticIndex = (statisticIndex + 1) % statistics.size
                handler.postDelayed(this, 20000)
            }
        }
        statisticUpdater.run()
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
}

typealias StatisticSource = () -> Statistic?

data class Statistic(val number: String, val subnumber: String?, val caption: String)