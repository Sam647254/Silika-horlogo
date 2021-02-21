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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
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
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.auth.CognitoCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.polly.AmazonPollyPresigningClient
import com.amazonaws.services.polly.model.Engine
import com.amazonaws.services.polly.model.OutputFormat
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import ooo.trankvila.silikahorlogo.komponantoj.AwairDataStrip
import ooo.trankvila.silikahorlogo.komponantoj.Clock
import ooo.trankvila.silikahorlogo.komponantoj.Fonto
import ooo.trankvila.silikahorlogo.komponantoj.StatisticDisplay
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
            val clockFormatState =
                remember { mutableStateOf(preferences.getBoolean("useSilican", false)) }

            val clockState: LocalDateTime by clockViewModel.currentDate.observeAsState(LocalDateTime.now())
            val awairDataState: AwairData? by awairViewModel.data.observeAsState()
            val statisticsState: DataDisplay? by statisticsViewModel.statistic.observeAsState()
            val graphState: List<Int>? by statisticsViewModel.graph.observeAsState()
            val graph2State: List<Int>? by statisticsViewModel.graph2.observeAsState()
            val weatherState: DataDisplay? by weatherViewModel.data.observeAsState()

            awairViewModel.launch(volleyQueue)
            newsViewModel.launch(volleyQueue)
            weatherViewModel.launch(volleyQueue)

            SilikaHorloĝoTheme(darkTheme = true) {
                Surface {
                    val silican = SilicanDateTime.fromLocalDateTime(clockState)
                    Background(applicationContext, silican.date, silican.time)
                    graphState?.let { graph1 ->
                        graph2State?.let { graph2 ->
                            Fonto(stats = graph1, stats2 = graph2)
                        }
                    }
                    awairDataState?.let {
                        AwairDataStrip(it, 10.dp)
                    }
                    Box(modifier = Modifier.offset(y = if (awairDataState == null) (-20).dp else 0.dp)) {
                        Clock(clockState, onClick = {
                            val prev = preferences.getBoolean("useSilican", false)
                            preferences.edit {
                                putBoolean("useSilican", !prev)
                                clockFormatState.value = !prev
                            }
                        }, useSilican = clockFormatState.value)
                    }
                    Crossfade(weatherState) { weather ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            weather?.let {
                                StatisticDisplay(
                                    statistic = it,
                                    onClick = {},
                                    alignment = Alignment.Start
                                )
                            }
                        }
                    }
                    Crossfade(statisticsState) { statistics ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            statistics?.let {
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
