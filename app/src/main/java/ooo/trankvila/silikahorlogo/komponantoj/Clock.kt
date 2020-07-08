package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.SilicanDateTime
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.SairaSemibold
import ooo.trankvila.silikahorlogo.ui.TimeShadow
import ooo.trankvila.silikahorlogo.ui.shadow
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

@Composable
fun Clock(dateTime: LocalDateTime, onClick: () -> Unit, useSilican: Boolean) {
    val silican = SilicanDateTime.fromLocalDateTime(dateTime)
    val date = silican.date
    val time = silican.time
    Row(
        modifier = Modifier.fillMaxSize()
            .offset(y = if (useSilican) 190.dp else 220.dp).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(horizontalGravity = Alignment.End) {
            if (useSilican) {
                Text(
                    if (useSilican) date.year.toString() else dateTime.year.toString(),
                    fontSize = 36.sp,
                    style = shadow,
                    fontFamily = Saira
                )
            }
            Text(
                if (useSilican) date.currentSeason else dateTime.year.toString(),
                fontSize = 54.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = (-15).dp)
            )
            Text(
                if (useSilican) date.currentWeek else dateTime.toString("MMMM d"),
                fontSize = 54.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = (-30).dp)
            )
            Text(
                if (useSilican) date.currentWeekday else dateTime.toString("EEEE"),
                fontSize = 54.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = (-45).dp)
            )
        }
        Text(
            if (useSilican) "${time.hour} ${
                time.minute.toString()
                    .padStart(2, '0')
            }" else dateTime.toString("HH:mm"),
            fontSize = 240.sp,
            fontFamily = SairaSemibold,
            style = TimeShadow,
            modifier = Modifier.offset(x = 10.dp, y = if (useSilican) (-50).dp else (-90).dp)
        )
    }
}