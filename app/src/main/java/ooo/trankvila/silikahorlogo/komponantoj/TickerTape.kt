package ooo.trankvila.silikahorlogo.komponantoj

import androidx.animation.IntPropKey
import androidx.animation.LinearEasing
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.ui.animation.Transition
import androidx.ui.animation.animate
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow

@Composable
fun TickerTape(entries: List<TickerTapeEntry>) {
    val offsetProperty = IntPropKey("offset")
    val scroll = transitionDefinition {
        state("start") {
            this[offsetProperty] = 0
        }
        state("end") {
            this[offsetProperty] = -2000
        }

        transition("start" to "end") {
            offsetProperty using tween {
                duration = 20000
                easing = LinearEasing
            }
        }
    }
    Transition(definition = scroll, toState = "end", initState = "start") {
        Row(
            modifier = Modifier.padding(top = 8.dp).offset(x = it[offsetProperty].dp)
                .widthIn(Dp.Hairline, Dp.Infinity)
        ) {
            entries.forEachIndexed { index, tickerTapeEntry ->
                Text(
                    text = tickerTapeEntry.text,
                    style = shadow,
                    fontFamily = Saira,
                    fontSize = 25.sp,
                    maxLines = 1,
                    softWrap = false
                )
                if (index < entries.size - 1) {
                    Text(
                        text = "|",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontFamily = Saira,
                        fontSize = 25.sp
                    )
                }
            }
        }
    }
}

data class TickerTapeEntry(val text: String, val onClick: () -> Unit)