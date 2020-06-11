package ooo.trankvila.silikahorlogo.komponantoj

import android.content.Context
import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.imageFromResource
import androidx.ui.layout.*
import androidx.ui.material.Surface
import androidx.ui.text.SpanStyle
import androidx.ui.text.annotatedString
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.R
import ooo.trankvila.silikahorlogo.SilicanDateTime
import ooo.trankvila.silikahorlogo.State
import ooo.trankvila.silikahorlogo.ui.*
import org.joda.time.Days
import org.joda.time.LocalDate

private val startOfShelterInPlace = LocalDate(2020, 3, 3)

@Composable
fun Clock(dateTime: SilicanDateTime) {
    val date = dateTime.date
    val time = dateTime.time
    Row(
        modifier = Modifier.fillMaxSize()
            .offset(y = 170.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(horizontalGravity = Alignment.End) {
            Text(
                date.year.toString(),
                fontSize = 36.sp,
                style = shadow,
                fontFamily = Saira
            )
            Text(
                date.currentSeason,
                fontSize = 54.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = (-15).dp)
            )
            Text(
                date.currentWeek,
                fontSize = 54.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = (-30).dp)
            )
            Text(
                date.currentWeekday,
                fontSize = 54.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = (-45).dp)
            )
        }
        Text(
            "${time.hour} ${time.minute.toString()
                .padStart(2, '0')}",
            fontSize = 240.sp,
            fontFamily = SairaSemibold,
            style = TimeShadow,
            modifier = Modifier.offset(y = (-40).dp)
        )
    }
}