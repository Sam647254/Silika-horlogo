package com.example.silikahorlogo

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.joda.time.*
import org.joda.time.field.MillisDurationField
import kotlin.math.min

internal val syncPoint = LocalDate(2018, 1, 1)
internal const val syncDayNumber = 12017 * 364 + 7 * ((12017 / 5) - (12017 / 40) + (12017 / 400))
internal const val daysIn400Years = 400 * 364 + 7 * (400 / 5 - 400 / 40 + 1)
internal const val daysIn40Years = 40 * 364 + 7 * (40 / 5 - 1)
internal const val daysIn5Years = 5 * 364 + 7

class SilicanClockViewModel : ViewModel() {
    val currentDate = MutableLiveData<Pair<SilicanDate, SilicanTime>>()

    init {
        val handler = Handler(Looper.getMainLooper())
        val updater = object : Runnable {
            override fun run() {
                val now = LocalTime.now()
                val hour = now.hourOfDay % 8
                val phase = now.hourOfDay / 8
                currentDate.postValue(
                    SilicanDate.fromGregorian(
                        LocalDate.now()
                    ) to SilicanTime(
                        phase,
                        hour,
                        now.minuteOfHour
                    )
                )
                val nextMinute =
                    now.withFieldAdded(DurationFieldType.minutes(), 1).withSecondOfMinute(0)
                handler.postDelayed(this, Period(now, nextMinute).millis.toLong())
            }
        }
        updater.run()
    }
}

data class SilicanTime(val phase: Int, val hour: Int, val minute: Int)

data class SilicanDate(val year: Int, val month: Int, val day: Int) {
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
            val month = dayOfYear / 28
            val day = (dayOfYear % 28) + 1
            return SilicanDate(
                year + 1,
                min(month + 1, 13),
                if (month == 13) day + 28 else day
            )
        }
    }
}