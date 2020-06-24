package ooo.trankvila.silikahorlogo.komponantoj

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.animation.IntPropKey
import androidx.animation.LinearEasing
import androidx.animation.TweenBuilder
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.compose.mutableStateOf
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.animation.Transition
import androidx.ui.animation.animate
import androidx.ui.core.Modifier
import androidx.ui.core.onChildPositioned
import androidx.ui.core.onPositioned
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.text.SpanStyle
import androidx.ui.text.annotatedString
import androidx.ui.text.style.TextAlign
import androidx.ui.unit.*
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow
import ooo.trankvila.silikahorlogo.ui.shadowStyle

@Composable
fun TickerTape(entries: List<TickerTapeEntry>) {
    val handler = Handler(Looper.getMainLooper())
    val tickerTapeState = state { 0 }
    val updater = object : Runnable {
        override fun run() {
            tickerTapeState.value = (tickerTapeState.value + 1) % entries.size
            handler.postDelayed(this, 15_000)
        }
    }
    handler.postDelayed(updater, 15000)
    Crossfade(current = tickerTapeState.value) {
        val entry = entries[it]
        Text(
            text = entry.title,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            style = shadow,
            fontFamily = Saira,
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
    }
}

data class TickerTapeEntry(val title: String, val onClick: () -> Unit)