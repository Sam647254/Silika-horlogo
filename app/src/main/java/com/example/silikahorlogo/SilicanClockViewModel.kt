package com.example.silikahorlogo

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.*
import org.joda.time.field.MillisDurationField
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.min

internal val syncPoint = LocalDate(2018, 1, 1)
internal const val syncDayNumber = 12017 * 364 + 7 * ((12017 / 5) - (12017 / 40) + (12017 / 400))
internal const val daysIn400Years = 400 * 364 + 7 * (400 / 5 - 400 / 40 + 1)
internal const val daysIn40Years = 40 * 364 + 7 * (40 / 5 - 1)
internal const val daysIn5Years = 5 * 364 + 7

class SilicanClockViewModel : ViewModel() {
    val currentDate = MutableLiveData<State>()
    var state: State = State(
        SilicanDate.fromGregorian(LocalDate.now()),
        SilicanTime.fromStandard(LocalTime.now()),
        null
    )
        set(value) {
            field = value
            currentDate.postValue(value)
        }

    init {
        val handler = Handler(Looper.getMainLooper())
        val updater = object : Runnable {
            override fun run() {
                val now = LocalTime.now()
                state = state.copy(
                    date = SilicanDate.fromGregorian(LocalDate.now()),
                    time = SilicanTime.fromStandard(
                        LocalTime.now()
                    )
                )
                val nextMinute =
                    now.withFieldAdded(DurationFieldType.minutes(), 1).withSecondOfMinute(0)
                handler.postDelayed(this, Period(now, nextMinute).millis.toLong())
            }
        }
        updater.run()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                fetchCasesCount()?.let {
                    state = state.copy(casesCount = it)
                }
            }
        }
    }

    private fun fetchCasesCount(): CasesCount? {
        val url = URL(
            "https://data.ca.gov/api/3/action/datastore_search_sql?sql=select " +
                    "totalcountconfirmed, newcountconfirmed, date from " +
                    "\"926fd08f-cc91-4828-af38-bd45de97f8c3\" where county = 'Santa Clara' order by " +
                    "date desc limit 1"
        )
        return (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            inputStream.bufferedReader().readText().let(::JSONObject).getJSONObject("result")
                .getJSONArray("records")
                .getJSONObject(0)
                .let { result ->
                    CasesCount(
                        result.getString("totalcountconfirmed").let(String::toFloat).toInt(),
                        result.getString("newcountconfirmed").let(String::toInt),
                        result.getString("date").let(LocalDateTime::parse)
                    )
                }
        }
    }
}

data class State(val date: SilicanDate, val time: SilicanTime, val casesCount: CasesCount?)

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

data class CasesCount(val total: Int, val new: Int, val date: LocalDateTime)

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
}