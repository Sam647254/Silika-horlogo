package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.min

private val gap = 3
private val barWidth = 10F

@Composable
fun Fonto(stats: List<Double>, stats2: List<Double>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val numberOfBars = (size.width / (barWidth + gap) + 2).toInt()
        val drop = stats.takeWhile { it == 0.0 }.size.coerceAtMost(stats2.takeWhile { it == 0.0 }.size)

        val bars = stats.subList(drop, min(stats.size, numberOfBars))
        val max = bars.maxOrNull()!!
        bars.forEachIndexed { index, bar ->
            val dx = size.width - index * (barWidth + gap) - barWidth
            val dy = bar / max * 0.99F * size.height / 2
            drawRect(
                Color.White.copy(alpha = 0.3F),
                Offset(dx, (size.height - dy).toFloat()),
                Size(barWidth, dy.toFloat())
            )
        }

        val bars2 = stats2.subList(drop, min(stats2.size, numberOfBars))
        val max2 = bars2.maxOrNull()!!
        bars2.forEachIndexed { index, bar ->
            val dx = size.width - index * (barWidth + gap) - barWidth
            drawRect(
                Color.White.copy(alpha = 0.3F),
                Offset(dx, 0F),
                Size(barWidth, (bar / max2 * 0.99F * size.height / 2).toFloat())
            )
        }
    }
}