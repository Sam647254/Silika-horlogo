package ooo.trankvila.silikahorlogo

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.setValue
import androidx.compose.state
import androidx.lifecycle.Observer
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.imageFromResource
import androidx.ui.layout.Row
import androidx.ui.layout.Stack
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.material.Surface
import androidx.ui.unit.dp
import ooo.trankvila.silikahorlogo.komponantoj.AwairDataStrip
import ooo.trankvila.silikahorlogo.komponantoj.Clock
import ooo.trankvila.silikahorlogo.komponantoj.StatisticDisplay
import ooo.trankvila.silikahorlogo.ui.SilikaHorloĝoTheme
import ooo.trankvila.silikahorlogo.ui.phaseColours
import ooo.trankvila.silikahorlogo.ui.weekdayColours
import org.joda.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()
    private val awairViewModel: AwairViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val clockState = state { SilicanDateTime.now }
            val awairDataState = state<AwairData?> { null }
            val statisticsState = state<Statistic?> { null }

            clockViewModel.currentDate.observe(this, Observer { newState ->
                clockState.value = newState
            })

            awairViewModel.data.observe(this, Observer { newData ->
                awairDataState.value = newData
            })

            statisticsViewModel.statistic.observe(this, Observer {
                statisticsState.value = it
            })

            SilikaHorloĝoTheme(darkTheme = true) {
                Surface {
                    Stack {
                        Background(applicationContext, clockState.value.date, clockState.value.time)
                        awairDataState.value?.let {
                            AwairDataStrip(it)
                        }
                        Clock(clockState.value)
                        Box(
                            modifier = Modifier.fillMaxSize().gravity(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            statisticsState.value?.let {
                                StatisticDisplay(statistic = it, onClick = {})
                            }
                        }
                    }
                }
            }
        }
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
fun Background(context: Context, date: SilicanDate, time: SilicanTime) {
    val resources = context.resources
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
    }
}
