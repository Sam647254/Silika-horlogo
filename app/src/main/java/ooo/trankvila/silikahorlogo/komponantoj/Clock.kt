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
import ooo.trankvila.silikahorlogo.State
import ooo.trankvila.silikahorlogo.ui.*
import org.joda.time.Days
import org.joda.time.LocalDate

private val startOfShelterInPlace = LocalDate(2020, 3, 3)

@Composable
fun Clock(context: Context, state: State, reŝarĝiKazojn: () -> Unit) {
    val resources = context.resources
    val date = state.date
    val time = state.time
    val today = LocalDate.now()
    SilikaHorloĝoTheme(darkTheme = true) {
        Surface {
            Stack {
                Image(
                    asset = imageFromResource(
                        resources,
                        R.drawable.background_top
                    ),
                    colorFilter = ColorFilter.tint(
                        resources.getColor(weekdayColours[date.weekday - 1], null)
                            .let(::Color)
                    )
                )
                Image(
                    asset = imageFromResource(
                        resources,
                        R.drawable.background_bottom
                    ),
                    colorFilter = ColorFilter.tint(
                        resources.getColor(phaseColours[time.phase], null)
                            .let(::Color)
                    )
                )
                state.awairData?.let { data ->
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AwairDataField(
                            value = data.score.toString(),
                            label = "Score"
                        )
                        AwairDataField(
                            value = "%.1f".format(data.temperature),
                            label = "Temperature",
                            unit = "°C"
                        )
                        AwairDataField(
                            value = "%.1f".format(data.humidity),
                            label = "Humidity",
                            unit = "%"
                        )
                        AwairDataField(
                            value = data.co2.toString(),
                            label = "Carbon dioxide",
                            unit = "PPM"
                        )
                        AwairDataField(
                            value = data.voc.toString(),
                            label = "V.O.C.",
                            unit = "PPB"
                        )
                        AwairDataField(
                            value = data.pm25.toString(),
                            label = "PM2.5",
                            unit = "μg/m³"
                        )
                    }
                }
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
                Column(
                    modifier = Modifier.padding(
                        start = 10.dp,
                        bottom = 10.dp
                    )
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "${Days.daysBetween(
                            startOfShelterInPlace,
                            today
                        ).days}",
                        fontSize = 80.sp,
                        fontFamily = Saira,
                        style = shadow,
                        modifier = Modifier.offset(y = 25.dp)
                    )
                    Text(
                        text = "days since shelter-in-place",
                        fontSize = 20.sp,
                        fontFamily = Saira,
                        style = shadow
                    )
                }
                state.casesCount?.let {
                    Column(
                        Modifier.padding(
                            end = 10.dp,
                            bottom = 10.dp
                        ).fillMaxSize().clickable(
                            onClick = reŝarĝiKazojn
                        ),
                        horizontalGravity = Alignment.End,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = annotatedString {
                                pushStyle(SpanStyle(fontSize = 80.sp))
                                append("${it.total} ")
                                pop()
                                pushStyle(SpanStyle(fontSize = 50.sp))
                                append("(+${it.new})")
                            },
                            fontSize = 80.sp,
                            fontFamily = Saira,
                            style = shadow,
                            modifier = Modifier.offset(y = 25.dp)
                        )
                        Text(
                            text = "cases in Santa Clara",
                            fontSize = 20.sp,
                            fontFamily = Saira,
                            style = shadow
                        )
                    }
                }
            }
        }
    }
}