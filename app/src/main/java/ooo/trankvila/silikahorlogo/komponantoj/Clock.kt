package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.SilicanDateTime
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.SairaSemibold
import ooo.trankvila.silikahorlogo.ui.TimeShadow
import ooo.trankvila.silikahorlogo.ui.shadow
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