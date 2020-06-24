package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.foundation.Canvas
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.Color
import androidx.ui.layout.fillMaxSize

private val gap = 3
private val barWidth = 10F

@Composable
fun Fonto(stats: List<Int>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val bars = stats.take((size.width / (gap + barWidth)).toInt() + 1).toList()
        val max = bars.max()!!
        bars.forEachIndexed { index, bar ->
            val dx = size.width - index * (barWidth + gap) - barWidth
            val dy = bar / max.toFloat() * 0.95F * size.height
            drawRect(
                Color.White.copy(alpha = 0.3F),
                Offset(dx, size.height - dy),
                Size(barWidth, bar / max.toFloat() * 0.95F * size.height)
            )
        }
    }
}