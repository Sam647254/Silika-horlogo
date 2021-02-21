package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow

@Composable
fun TickerTape(entry: TickerTapeEntry) {
    Crossfade(current = entry) {
        Text(
            text = entry.title,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                .clickable(onClick = entry.onClick),
            style = shadow,
            fontFamily = Saira,
            textAlign = TextAlign.Center,
            fontSize = 25.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

data class TickerTapeEntry(val title: String, val onClick: () -> Unit)