package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

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