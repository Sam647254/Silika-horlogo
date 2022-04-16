package ooo.trankvila.silikahorlogo.komponantoj

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ooo.trankvila.silikahorlogo.SilicanTime
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow
import org.joda.time.DateTime
import org.joda.time.Minutes

private const val PomodoroDuration = 25

@Composable
fun Pomodoro(onTimerEnded: () -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    var currentState by remember { mutableStateOf(PomodoroState.STOPPED) }
    var stopTime: DateTime? by remember { mutableStateOf(null) }
    val progress = remember { Animatable(0F) }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable {
                if (currentState == PomodoroState.STOPPED) {
                    stopTime = DateTime
                        .now()
                        .withPeriodAdded(Minutes.minutes(PomodoroDuration), 1)
                    currentState = PomodoroState.RUNNING
                    handler.postDelayed({
                        currentState = PomodoroState.ENDED
                        onTimerEnded()
                    }, PomodoroDuration * 60 * 1000L)
                    scope.launch {
                        progress.animateTo(
                            1F,
                            tween(PomodoroDuration * 60 * 1000, easing = LinearEasing)
                        )
                    }
                } else {
                    stopTime = null
                    currentState = PomodoroState.STOPPED
                    scope.launch {
                        progress.animateTo(0F, snap())
                    }
                }
            },
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentState == PomodoroState.RUNNING || currentState == PomodoroState.ENDED) {
            Text(
                if (currentState == PomodoroState.RUNNING) "Timer ends at ${
                    SilicanTime.fromStandard(
                        stopTime!!.toLocalTime()
                    )
                }" else "Timer ended",
                fontFamily = Saira, style = shadow, fontSize = 25.sp
            )
        }
        Canvas(
            Modifier
                .fillMaxWidth(0.7F)
                .height(4.dp)
        ) {
            drawRect(Color.White, size = Size(progress.value * this.size.width, this.size.height))
            drawRect(Color.White, alpha = 0.4F)
        }
    }
}

enum class PomodoroState {
    STOPPED,
    RUNNING,
    ENDED,
}