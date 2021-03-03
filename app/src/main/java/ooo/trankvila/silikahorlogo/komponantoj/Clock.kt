package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ooo.trankvila.silikahorlogo.SilicanDateTime
import ooo.trankvila.silikahorlogo.ui.*
import org.joda.time.LocalDateTime

@Composable
fun Clock(dateTime: LocalDateTime, onClick: () -> Unit, useSilican: Boolean) {
    val silican = SilicanDateTime.fromLocalDateTime(dateTime)
    val date = silican.date
    val time = silican.time
    val proportion = (dateTime.minuteOfHour + dateTime.hourOfDay * 60) / (24 * 60F)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = if (useSilican) 180.dp else 220.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.End) {
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
                if (useSilican) date.currentWeek else dateTime.toString("MMM d"),
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
            modifier = Modifier.offset(x = 10.dp, y = if (useSilican) (-40).dp else (-90).dp)
        )
    }
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        drawRect(
//            Brush.horizontalGradient(
//                0F to chaos,
//                7.7F / 24F to chaos,
//                8.3F / 24F to serenity,
//                15.7F / 24F to serenity,
//                16.3F / 24 to fervidity,
//                1F to fervidity
//            ),
//            topLeft = Offset(0F, 600F),
//            size = Size(size.width, 6F),
//            alpha = 0.5F
//        )
//        drawRect(
//            Brush.radialGradient(
//                0F to Color.Transparent,
//                1F to Color.White,
//                center = Offset.Zero,
//                radius = 23F
//            ),
//            topLeft = Offset(proportion * size.width - 40, 580F),
//            size = Size(40F, 23F)
//        )
//    }
}