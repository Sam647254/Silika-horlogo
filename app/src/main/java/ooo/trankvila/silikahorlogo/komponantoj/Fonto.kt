package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.foundation.Canvas
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.layout.fillMaxSize
import androidx.ui.unit.dp

private val gap = 1
private val barWidth = 5F

@Composable
fun Fonto(stats: Sequence<Int>) {
    val paint = remember {
        Color.White.copy(alpha = 0.4F)
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val bars = stats.take((size.width / (gap + barWidth)).toInt()).toList()
        val max = bars.max()!!
        bars.forEachIndexed { index, bar ->
            drawRect(
                paint,
                Offset(size.width - index * (barWidth + gap) - barWidth, bar / max * 0.95F),
                Size(barWidth, bar / max * 0.95F)
            )
        }
    }
}