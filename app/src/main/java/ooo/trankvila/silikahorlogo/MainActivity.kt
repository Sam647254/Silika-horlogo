package ooo.trankvila.silikahorlogo

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Transition
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.state
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.Observer
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.polly.AmazonPollyPresigningClient
import com.amazonaws.services.polly.model.Engine
import com.amazonaws.services.polly.model.OutputFormat
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ooo.trankvila.silikahorlogo.komponantoj.*
import ooo.trankvila.silikahorlogo.ui.SilikaHorloĝoTheme
import ooo.trankvila.silikahorlogo.ui.phaseColours
import ooo.trankvila.silikahorlogo.ui.weekdayColours
import org.joda.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    private val clockViewModel: ClockViewModel by viewModels()
    private val awairViewModel: AwairViewModel by viewModels()
    private val statisticsViewModel: StatisticsViewModel by viewModels()
    private val newsViewModel: NewsViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

    private lateinit var credentialsProvider: CognitoCredentialsProvider
    private lateinit var pollyClient: AmazonPollyPresigningClient
    private lateinit var volleyQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        volleyQueue = Volley.newRequestQueue(this)

        newsViewModel.synthesizeSpeech = { text ->
            if (!::credentialsProvider.isInitialized) {
                credentialsProvider = CognitoCachingCredentialsProvider(
                    applicationContext,
                    CognitoIdentityPoolId,
                    Regions.US_WEST_2
                )
                pollyClient = AmazonPollyPresigningClient(credentialsProvider)
            }
            val request = SynthesizeSpeechPresignRequest().apply {
                this.text = text
                voiceId = "Emma"
                engine = Engine.Neural
                setOutputFormat(OutputFormat.Mp3)
            }
            val url = pollyClient.getPresignedSynthesizeSpeechUrl(request)
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(url.toString())
                prepareAsync()
                setOnPreparedListener {
                    it.start()
                }
                setOnCompletionListener {
                    it.release()
                }
            }
        }

        val handler = Handler(Looper.getMainLooper())
        val preferences = getPreferences(MODE_PRIVATE)

        setContent {
            val clockState = state { LocalDateTime.now() }
            val awairDataState = state<AwairData?> { null }
            val statisticsState = state<DataDisplay?> { null }
            val statisticsTransitionState = state { "visible" }
            val weatherTransitionState = state { "visible" }
            val graphState = state<List<Int>?> { null }
            val newsState = state<TickerTapeEntry?> { null }
            val weatherState = state<DataDisplay?> { null }
            val clockFormatState = state { preferences.getBoolean("useSilican", false) }

            clockViewModel.currentDate.observe(this, Observer { newState ->
                clockState.value = newState
            })

            awairViewModel.launch(volleyQueue)
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

            statisticsViewModel.graph.observe(this, Observer { data ->
                graphState.value = data
            })

            newsViewModel.entry.observe(this, Observer { entry ->
                newsState.value = entry
            })
            newsViewModel.launch(volleyQueue)

            weatherViewModel.data.observe(this, Observer { data ->
                weatherTransitionState.value = "invisible"
                handler.postDelayed({
                    weatherState.value = data
                    weatherTransitionState.value = "visible"
                }, 1000)
            })
            weatherViewModel.launch(volleyQueue)

            val opacity = FloatPropKey("Opacity")
            val fadeTransition = transitionDefinition<String> {
                state("visible") {
                    this[opacity] = 1.0F
                }
                state("invisible") {
                    this[opacity] = 0F
                }

                transition("visible" to "invisible") {
                    opacity using tween(500, easing = FastOutLinearInEasing)
                }

                transition("invisible" to "visible") {
                    opacity using tween(500, easing = FastOutSlowInEasing)
                }
            }

            SilikaHorloĝoTheme(darkTheme = true) {
                Surface {
                    Stack {
                        val silican = SilicanDateTime.fromLocalDateTime(clockState.value)
                        Background(applicationContext, silican.date, silican.time)
                        newsState.value?.let {
                            TickerTape(entry = it)
                        }
                        graphState.value?.let {
                            Fonto(stats = it)
                        }
                        awairDataState.value?.let {
                            AwairDataStrip(it, if (newsState.value != null) 40.dp else 10.dp)
                        }
                        Box(modifier = Modifier.offset(y = if (awairDataState.value == null) (-20).dp else 0.dp)) {
                            Clock(clockState.value, onClick = {
                                val prev = preferences.getBoolean("useSilican", false)
                                preferences.edit {
                                    putBoolean("useSilican", !prev)
                                    clockFormatState.value = !prev
                                }
                            }, useSilican = clockFormatState.value)
                        }
                        Transition(
                            definition = fadeTransition, toState =
                            weatherTransitionState.value
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(10.dp)
                                    .drawOpacity(it[opacity]),
                                alignment = Alignment.BottomStart
                            ) {
                                weatherState.value?.let {
                                    StatisticDisplay(
                                        statistic = it,
                                        onClick = {},
                                        alignment = Alignment.Start
                                    )
                                }
                            }
                        }
                        Transition(
                            definition = fadeTransition,
                            toState = statisticsTransitionState.value
                        ) { state ->
                            Box(
                                modifier = Modifier.fillMaxSize().padding(10.dp)
                                    .drawOpacity(state[opacity]),
                                alignment = Alignment.BottomEnd
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
