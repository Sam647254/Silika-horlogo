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
import org.joda.time.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal val syncPoint = LocalDate(2018, 1, 1)
internal const val syncDayNumber = 12017 * 364 + 7 * ((12017 / 5) - (12017 / 40) + (12017 / 400))
internal const val daysIn400Years = 400 * 364 + 7 * (400 / 5 - 400 / 40 + 1)
internal const val daysIn40Years = 40 * 364 + 7 * (40 / 5 - 1)
internal const val daysIn5Years = 5 * 364 + 7

class SilicanClockViewModel : ViewModel() {
    private companion object {
        const val DATA_URL =
            "https://wabi-us-gov-virginia-api.analysis.usgovcloudapi.net/public/reports/querydata?synchronous=true"
        const val CASES_PAYLOAD =
            "{\"version\":\"1.0.0\",\"queries\":[{\"Query\":{\"Commands\":[{\"SemanticQueryDataShapeCommand\":{\"Query\":{\"Version\":2,\"From\":[{\"Name\":\"c\",\"Entity\":\"counts\",\"Type\":0}],\"Select\":[{\"Aggregation\":{\"Expression\":{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Total\"}},\"Function\":0},\"Name\":\"Sum(counts.Total)\"}],\"Where\":[{\"Condition\":{\"In\":{\"Expressions\":[{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Category\"}}],\"Values\":[[{\"Literal\":{\"Value\":\"'Cases'\"}}]]}}}]},\"Binding\":{\"Primary\":{\"Groupings\":[{\"Projections\":[0]}]},\"DataReduction\":{\"DataVolume\":3,\"Primary\":{\"Top\":{}}},\"Version\":1}}}]},\"CacheKey\":\"{\\\"Commands\\\":[{\\\"SemanticQueryDataShapeCommand\\\":{\\\"Query\\\":{\\\"Version\\\":2,\\\"From\\\":[{\\\"Name\\\":\\\"c\\\",\\\"Entity\\\":\\\"counts\\\",\\\"Type\\\":0}],\\\"Select\\\":[{\\\"Aggregation\\\":{\\\"Expression\\\":{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Total\\\"}},\\\"Function\\\":0},\\\"Name\\\":\\\"Sum(counts.Total)\\\"}],\\\"Where\\\":[{\\\"Condition\\\":{\\\"In\\\":{\\\"Expressions\\\":[{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Category\\\"}}],\\\"Values\\\":[[{\\\"Literal\\\":{\\\"Value\\\":\\\"'Cases'\\\"}}]]}}}]},\\\"Binding\\\":{\\\"Primary\\\":{\\\"Groupings\\\":[{\\\"Projections\\\":[0]}]},\\\"DataReduction\\\":{\\\"DataVolume\\\":3,\\\"Primary\\\":{\\\"Top\\\":{}}},\\\"Version\\\":1}}}]}\",\"QueryId\":\"\",\"ApplicationContext\":{\"DatasetId\":\"b9bd8aff-3939-4b9b-bb7f-b562bdc492ad\",\"Sources\":[{\"ReportId\":\"8c0bb640-6b65-4ea6-9146-39a7cbad0314\"}]}}],\"cancelQueries\":[],\"modelId\":344061}"
        const val NEW_CASES_PAYLOAD =
            "{\"version\":\"1.0.0\",\"queries\":[{\"Query\":{\"Commands\":[{\"SemanticQueryDataShapeCommand\":{\"Query\":{\"Version\":2,\"From\":[{\"Name\":\"c\",\"Entity\":\"counts\",\"Type\":0}],\"Select\":[{\"Aggregation\":{\"Expression\":{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"New\"}},\"Function\":0},\"Name\":\"Sum(counts.New)\"}],\"Where\":[{\"Condition\":{\"In\":{\"Expressions\":[{\"Column\":{\"Expression\":{\"SourceRef\":{\"Source\":\"c\"}},\"Property\":\"Category\"}}],\"Values\":[[{\"Literal\":{\"Value\":\"'Cases'\"}}]]}}}]},\"Binding\":{\"Primary\":{\"Groupings\":[{\"Projections\":[0]}]},\"DataReduction\":{\"DataVolume\":3,\"Primary\":{\"Top\":{}}},\"Version\":1}}}]},\"CacheKey\":\"{\\\"Commands\\\":[{\\\"SemanticQueryDataShapeCommand\\\":{\\\"Query\\\":{\\\"Version\\\":2,\\\"From\\\":[{\\\"Name\\\":\\\"c\\\",\\\"Entity\\\":\\\"counts\\\",\\\"Type\\\":0}],\\\"Select\\\":[{\\\"Aggregation\\\":{\\\"Expression\\\":{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"New\\\"}},\\\"Function\\\":0},\\\"Name\\\":\\\"Sum(counts.New)\\\"}],\\\"Where\\\":[{\\\"Condition\\\":{\\\"In\\\":{\\\"Expressions\\\":[{\\\"Column\\\":{\\\"Expression\\\":{\\\"SourceRef\\\":{\\\"Source\\\":\\\"c\\\"}},\\\"Property\\\":\\\"Category\\\"}}],\\\"Values\\\":[[{\\\"Literal\\\":{\\\"Value\\\":\\\"'Cases'\\\"}}]]}}}]},\\\"Binding\\\":{\\\"Primary\\\":{\\\"Groupings\\\":[{\\\"Projections\\\":[0]}]},\\\"DataReduction\\\":{\\\"DataVolume\\\":3,\\\"Primary\\\":{\\\"Top\\\":{}}},\\\"Version\\\":1}}}]}\",\"QueryId\":\"\",\"ApplicationContext\":{\"DatasetId\":\"b9bd8aff-3939-4b9b-bb7f-b562bdc492ad\",\"Sources\":[{\"ReportId\":\"8c0bb640-6b65-4ea6-9146-39a7cbad0314\"}]}}],\"cancelQueries\":[],\"modelId\":344061}"
    }

    val currentDate = MutableLiveData<State>()
    var state: State = State(
        SilicanDate.fromGregorian(LocalDate.now()),
        SilicanTime.fromStandard(LocalTime.now()),
        null,
        null
    )
        set(value) {
            field = value
            currentDate.postValue(value)
        }

    init {
        val handler = Handler(Looper.getMainLooper())
        val awairUpdater = Runnable {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val awair = fetchAwairData()
                    state = state.copy(
                        awairData = awair
                    )
                }
            }
        }
        val clockUpdater = object : Runnable {
            override fun run() {
                awairUpdater.run()
                val now = LocalTime.now()
                state = state.copy(
                    date = SilicanDate.fromGregorian(LocalDate.now()),
                    time = SilicanTime.fromStandard(
                        LocalTime.now()
                    )
                )
                val nextMinute =
                    now.withFieldAdded(DurationFieldType.minutes(), 1).withSecondOfMinute(0)
                handler.postDelayed(
                    this,
                    Period(now, nextMinute, PeriodType.millis()).millis.toLong()
                )
            }
        }
        awairUpdater.run()
        clockUpdater.run()
        val casesCountUpdater = object : Runnable {
            override fun run() {
                refreshCasesCount()
                val now = LocalDateTime.now()
                val nextUpdate = LocalDate.now().plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT)
                handler.postDelayed(
                    this,
                    Period(now, nextUpdate, PeriodType.millis()).millis.toLong()
                )
            }
        }
        casesCountUpdater.run()
    }

    fun refreshCasesCount() {
        Log.d(this::class.simpleName, "Refreshing cases count")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fetchCasesCount()?.let {
                    state = state.copy(casesCount = it)
                }
            }
        }
    }

    private fun fetchCasesCount(): CasesCount? {
        val total = fetchData(CASES_PAYLOAD) ?: return null
        val new = fetchData(NEW_CASES_PAYLOAD) ?: return null
        return CasesCount(total, new)
    }

    private fun fetchAwairData(): AwairData? {
        val url = URL("http://awair-elem-14041c/air-data/latest")
        return (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            inputStream.bufferedReader().use {
                it.readText().let(::JSONObject).let { response ->
                    AwairData(
                        response.getInt("score"),
                        response.getDouble("temp"),
                        response.getDouble("humid"),
                        response.getInt("co2"),
                        response.getInt("voc"),
                        response.getInt("pm25")
                    )
                }
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
}

data class State(
    val date: SilicanDate,
    val time: SilicanTime,
    val casesCount: CasesCount?,
    val awairData: AwairData?
)

data class AwairData(
    val score: Int,
    val temperature: Double,
    val humidity: Double,
    val co2: Int,
    val voc: Int,
    val pm25: Int
)

data class SilicanTime(val phase: Int, val hour: Int, val minute: Int) {
    companion object {
        fun fromStandard(time: LocalTime): SilicanTime {
            val now = LocalTime.now()
            val hour = now.hourOfDay % 8
            val phase = now.hourOfDay / 8
            return SilicanTime(phase, hour, now.minuteOfHour)
        }
    }
}

data class CasesCount(val total: Int, val new: Int)

data class SilicanDate(val year: Int, val season: Int, val week: Int, val weekday: Int) {
    companion object {
        fun fromGregorian(date: LocalDate): SilicanDate {
            val difference = Days.daysBetween(syncPoint, date).days
            val dayNumber = syncDayNumber + difference
            val years400 = dayNumber / daysIn400Years
            val remain400 = dayNumber % daysIn400Years
            val years40 = remain400 / daysIn40Years
            val remain40 = remain400 % daysIn40Years
            val years5 = remain40 / daysIn5Years
            val remain5 = remain40 % daysIn5Years
            val remainingYears = remain5 / 364
            val remainingDays = remain5 % 364
            val year = years400 * 400 + years40 * 40 + years5 * 5 + Math.min(remainingYears, 5)
            val dayOfYear = if (remainingYears == 6) 364 + remainingDays else remainingDays
            val season = if (dayOfYear > 364) 4 else dayOfYear / (364 / 4) + 1
            val dayOfSeason =
                if (dayOfYear > 364) 364 / 4 + dayOfYear % (364 / 4) else dayOfYear % (364 / 4)
            val weekOfSeason = dayOfSeason / 7 + 1
            val dayOfWeek = dayOfYear % 7 + 1
            val day = (dayOfYear % 28) + 1
            return SilicanDate(
                year + 1,
                season,
                weekOfSeason,
                dayOfWeek
            )
        }

        private val seasons = listOf("Nevari", "Penari", "Sevari", "Venari")
        private val weeks = listOf(
            "Ateluna",
            "Beviruto",
            "Deruna",
            "Elito",
            "Feridina",
            "Geranito",
            "Lunamarina",
            "Miraliluto",
            "Peridina",
            "Samerito",
            "Timina",
            "Verato",
            "Wilaluna",
            "Zeroto"
        )
        private val weekdays = listOf(
            "Boromika",
            "Ferimanika",
            "Lusinika",
            "Navimilika",
            "Perinatika",
            "Relikanika",
            "Temiranika"
        )
    }

    val textDate get() = "${seasons[season - 1]} ${weeks[week - 1]} ${weekdays[weekday - 1]}"
    val shortDate get() = "$year ${seasons[season - 1][0]}${weeks[week - 1][0]}${weekdays[weekday - 1][0]}"
    val currentSeason = seasons[season - 1]
    val currentWeek = weeks[week - 1]
    val currentWeekday = weekdays[weekday - 1]
}