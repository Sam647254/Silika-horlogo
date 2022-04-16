package ooo.trankvila.silikahorlogo

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ooo.trankvila.silikahorlogo.komponantoj.Clock
import ooo.trankvila.silikahorlogo.komponantoj.DataBar
import ooo.trankvila.silikahorlogo.komponantoj.DataFieldBar
import ooo.trankvila.silikahorlogo.komponantoj.Pomodoro
import ooo.trankvila.silikahorlogo.ui.*
import org.joda.time.LocalDateTime

class MainActivity : ComponentActivity() {
    private val clockViewModel: ClockViewModel by viewModels()
    private val awairViewModel: AwairViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val dataBarViewModel by viewModels<DataBarViewModel>()

    private lateinit var volleyQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        volleyQueue = Volley.newRequestQueue(this)

        val preferences = getPreferences(MODE_PRIVATE)

        setContent {
            val clockFormatState =
                remember { mutableStateOf(preferences.getBoolean("useSilican", false)) }

            val clockState: LocalDateTime by clockViewModel.currentDate.observeAsState(LocalDateTime.now())
            val awairDataState: AwairData? by awairViewModel.data.observeAsState()
            val dataBarDataState: DataBar? by dataBarViewModel.currentData.observeAsState()
            val secondDataState: DataBar? by dataBarViewModel.secondData.observeAsState()

            awairViewModel.launch(volleyQueue)
            weatherViewModel.launch(volleyQueue)
            dataBarViewModel.launch(volleyQueue)

            SilikaHorloĝoTheme(darkTheme = true) {
                Surface {
                    val silican = SilicanDateTime.fromLocalDateTime(clockState)
                    Background(applicationContext, silican.date, silican.time)
                    Crossfade(dataBarDataState) { dataBar ->
                        if (dataBar == null) {
                            Text(
                                text = "Loading...",
                                fontFamily = Saira,
                                style = shadow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 40.sp,
                            )
                        } else {
                            DataFieldBar(dataBar) {
                                dataBarViewModel.refresh()
                            }
                        }
                    }
                    Box(modifier = Modifier.offset(y = if (awairDataState == null) (-20).dp else 0.dp)) {
                        Clock(clockState, onClick = {
                            val prev = preferences.getBoolean("useSilican", false)
                            preferences.edit {
                                putBoolean("useSilican", !prev)
                                clockFormatState.value = !prev
                                makeFullscreen()
                            }
                        }, useSilican = clockFormatState.value)
                    }
                    Box(modifier = Modifier
                        .offset(y = 410.dp)
                        .fillMaxWidth()
                        .height(40.dp)) {
                        Pomodoro {
                            RingtoneManager.getRingtone(
                                applicationContext,
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            ).play()
                        }
                    }
                    Box(modifier = Modifier.offset(y = 460.dp)) {
                        Crossfade(secondDataState) { dataBar ->
                            if (dataBar == null) {
                                Text(
                                    text = "Loading...",
                                    fontFamily = Saira,
                                    style = shadow,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 40.sp,
                                )
                            } else {
                                DataFieldBar(dataBar) {
                                    dataBarViewModel.refreshSecond()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        makeFullscreen()
    }

    private fun makeFullscreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

@Composable
fun Background(context: Context, date: SilicanDate, time: SilicanTime) {
    val resources = context.resources
    Image(
        painterResource(
            R.drawable.background_top
        ),
        null,
        colorFilter = ColorFilter.tint(
            resources.getColor(weekdayColours[date.weekday - 1], null)
                .let(::Color)
        )
    )
    Image(
        painterResource(
            R.drawable.background_bottom
        ),
        null,
        colorFilter = ColorFilter.tint(
            resources.getColor(phaseColours[time.phase], null)
                .let(::Color)
        )
    )
}
