package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

private val gap = 3
private val barWidth = 10F

@Composable
fun Fonto(stats: List<Int>, stats2: List<Int>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val drop = stats.takeWhile { it == 0 }.size.coerceAtMost(stats2.takeWhile { it == 0 }.size)

        val bars = stats.subList(drop, stats.size)
        val max = bars.maxOrNull()!!
        bars.forEachIndexed { index, bar ->
            val dx = size.width - index * (barWidth + gap) - barWidth
            val dy = bar / max.toFloat() * 0.99F * size.height / 2
            drawRect(
                Color.White.copy(alpha = 0.3F),
                Offset(dx, size.height - dy),
                Size(barWidth, dy)
            )
        }

        val bars2 = stats2.subList(drop, stats.size)
        val max2 = bars2.max()!!
        bars2.forEachIndexed { index, bar ->
            val dx = size.width - index * (barWidth + gap) - barWidth
            drawRect(
                Color.White.copy(alpha = 0.3F),
                Offset(dx, 0F),
                Size(barWidth, bar / max2.toFloat() * 0.99F * size.height / 2)
            )
        }
    }
}