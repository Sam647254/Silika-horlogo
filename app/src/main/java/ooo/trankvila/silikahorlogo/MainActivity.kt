package ooo.trankvila.silikahorlogo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ooo.trankvila.silikahorlogo.komponantoj.*
import ooo.trankvila.silikahorlogo.ui.*
import org.joda.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    private val clockViewModel: ClockViewModel by viewModels()
    private val awairViewModel: AwairViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()
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
            val graphState: List<Double>? by statisticsViewModel.graph.observeAsState()
            val graph2State: List<Double>? by statisticsViewModel.graph2.observeAsState()
            val dataBarItemState: DataBarItem by dataBarViewModel.current.observeAsState(DataBarItem.CurrentWeather)
            val dataBarDataState: DataBar? by dataBarViewModel.currentData.observeAsState()
            val dataBarHeldState: Boolean by dataBarViewModel.held.observeAsState(false)

            awairViewModel.launch(volleyQueue)
            weatherViewModel.launch(volleyQueue)
            dataBarViewModel.launch(volleyQueue)

            SilikaHorloĝoTheme(darkTheme = true) {
                Surface {
                    val silican = SilicanDateTime.fromLocalDateTime(clockState)
                    Background(applicationContext, silican.date, silican.time)
                    graphState?.let { graph1 ->
                        graph2State?.let { graph2 ->
                            Fonto(stats = graph1, stats2 = graph2)
                        }
                    }
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
                                statisticsViewModel.fetchGraph()
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
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(30.dp), contentAlignment = Alignment.BottomCenter
                    ) {
                        DataCategoryBar(dataBarItemState, dataBarHeldState, dataBarViewModel::holdSelected)
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
        imageFromResource(
            resources,
            R.drawable.background_top
        ),
        null,
        colorFilter = ColorFilter.tint(
            resources.getColor(weekdayColours[date.weekday - 1], null)
                .let(::Color)
        )
    )
    Image(
        imageFromResource(
            resources,
            R.drawable.background_bottom
        ),
        null,
        colorFilter = ColorFilter.tint(
            resources.getColor(phaseColours[time.phase], null)
                .let(::Color)
        )
    )
}
