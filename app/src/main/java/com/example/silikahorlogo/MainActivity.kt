package com.example.silikahorlogo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.lifecycle.Observer
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.Shadow
import androidx.ui.graphics.imageFromResource
import androidx.ui.layout.*
import androidx.ui.material.Surface
import androidx.ui.text.SpanStyle
import androidx.ui.text.TextStyle
import androidx.ui.text.annotatedString
import androidx.ui.text.font.ResourceFont
import androidx.ui.text.font.fontFamily
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.example.silikahorlogo.ui.SilikaHorloĝoTheme
import org.joda.time.Days
import org.joda.time.LocalDate

private val Saira = fontFamily(ResourceFont(R.font.saira_regular))
private val SairaSemibold = fontFamily(ResourceFont(R.font.saira_semibold))
private val TimeShadow = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 10F))
private val shadow = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 3F))
private val startOfShelterInPlace = LocalDate(2020, 3, 3)

private val weekdayColours = listOf(
    R.color.lavender,
    R.color.carnation,
    R.color.sapphire,
    R.color.ruby,
    R.color.amber,
    R.color.fern,
    R.color.slate
)
private val phaseColours = listOf(
    R.color.chaos,
    R.color.serenity,
    R.color.fervidity
)

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        clockViewModel.currentDate.observe(this, Observer { state ->
            setContent {
                Clock(context = this, state = state)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SilikaHorloĝoTheme {
        Greeting("Android")
    }
}

@Composable
fun AwairDataField(value: String, label: String, unit: String = "") {
    Column(horizontalGravity = Alignment.CenterHorizontally) {
        Text(
            annotatedString {
                pushStyle(SpanStyle(fontSize = 50.sp))
                append(value)
                if (unit.isNotEmpty()) {
                    pop()
                    pushStyle(SpanStyle(fontSize = 25.sp))
                    append(' ')
                    append(unit)
                }
            },
            fontFamily = Saira,
            style = shadow,
            modifier = Modifier.padding(horizontal = 15.dp),
            maxLines = 1,
            softWrap = false
        )
        Text(
            label,
            fontSize = 20.sp,
            fontFamily = Saira,
            style = shadow,
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}

@Composable
fun Clock(context: Context, state: State) {
    val resources = context.resources
    val date = state.date
    val time = state.time
    val today = LocalDate.now()
    SilikaHorloĝoTheme(darkTheme = true) {
        Surface {
            Stack {
                Image(
                    asset = imageFromResource(resources, R.drawable.background_top),
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
                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AwairDataField(value = data.score.toString(), label = "Score")
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
                        AwairDataField(value = data.voc.toString(), label = "V.O.C.", unit = "PPB")
                        AwairDataField(
                            value = data.pm25.toString(),
                            label = "PM2.5",
                            unit = "μg/m³"
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxSize().offset(y = 170.dp),
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
                    modifier = Modifier.padding(start = 10.dp, bottom = 10.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "${Days.daysBetween(startOfShelterInPlace, today).days}",
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
                        Modifier.padding(end = 10.dp, bottom = 10.dp).fillMaxSize(),
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
                            text = "cases in Santa Clara as of ${SilicanDate.fromGregorian(it.date.toLocalDate()).shortDate}",
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