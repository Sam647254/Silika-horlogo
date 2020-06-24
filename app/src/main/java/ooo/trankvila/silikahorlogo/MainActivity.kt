package ooo.trankvila.silikahorlogo

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.animation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.setValue
import androidx.compose.state
import androidx.lifecycle.Observer
import androidx.ui.animation.Transition
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
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
import androidx.ui.unit.ipx
import androidx.ui.unit.px
import ooo.trankvila.silikahorlogo.komponantoj.AwairDataStrip
import ooo.trankvila.silikahorlogo.komponantoj.Clock
import ooo.trankvila.silikahorlogo.komponantoj.StatisticDisplay
import ooo.trankvila.silikahorlogo.ui.SilikaHorloĝoTheme
import ooo.trankvila.silikahorlogo.ui.phaseColours
import ooo.trankvila.silikahorlogo.ui.weekdayColours
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.Period

private val startOfShelterInPlace = LocalDate(2020, 3, 3)

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()
    private val awairViewModel: AwairViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val handler = Handler(Looper.getMainLooper())

        setContent {
            val clockState = state { SilicanDateTime.now }
            val awairDataState = state<AwairData?> { null }
            val statisticsState = state<Statistic?> { null }
            val statisticsTransitionState = state { "visible" }

            clockViewModel.currentDate.observe(this, Observer { newState ->
                clockState.value = newState
            })

            awairViewModel.data.observe(this, Observer { newData ->
                awairDataState.value = newData
            })

            statisticsViewModel.statistic.observe(this, Observer {
                statisticsTransitionState.value = "invisible"
                handler.postDelayed({
                    statisticsState.value = it
                    statisticsTransitionState.value = "visible"
                }, 1000)
            })

            val opacity = FloatPropKey("Opacity")
            val fadeTransition = transitionDefinition {
                state("visible") {
                    this[opacity] = 1.0F
                }
                state("invisible") {
                    this[opacity] = 0F
                }

                transition("visible" to "invisible") {
                    opacity using tween {
                        duration = 500
                        easing = FastOutLinearInEasing
                    }
                }

                transition("invisible" to "visible") {
                    opacity using tween {
                        duration = 500
                        easing = FastOutSlowInEasing
                    }
                }
            }

            SilikaHorloĝoTheme(darkTheme = true) {
                Surface {
                    Stack {
                        Background(applicationContext, clockState.value.date, clockState.value.time)
                        awairDataState.value?.let {
                            AwairDataStrip(it)
                        }
                        Clock(clockState.value)
                        Box(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            gravity = Alignment.BottomStart
                        ) {
                            ShelterInPlaceCounter()
                        }
                        Transition(
                            definition = fadeTransition,
                            toState = statisticsTransitionState.value
                        ) { state ->
                            Box(
                                modifier = Modifier.fillMaxSize().padding(10.dp)
                                    .drawOpacity(state[opacity]),
                                gravity = Alignment.BottomEnd
                            ) {
                                statisticsState.value?.let {
                                    StatisticDisplay(
                                        statistic = it,
                                        onClick = {},
                                        alignment = Alignment.End
                                    )
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
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

@Composable
fun ShelterInPlaceCounter() {
    val duration = Days.daysBetween(startOfShelterInPlace, LocalDate.now()).days
    StatisticDisplay(
        statistic = Statistic(
            duration.toString(),
            null,
            "days of shelter-in-place"
        ), onClick = {}, alignment = Alignment.Start
    )
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
