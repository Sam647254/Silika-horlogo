package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawLayer
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.text.SpanStyle
import androidx.ui.text.TextLayoutResult
import androidx.ui.text.annotatedString
import androidx.ui.unit.IntPx
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.Statistic
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow

@Composable
fun StatisticDisplay(
    statistic: Statistic,
    onClick: () -> Unit,
    alignment: Alignment.Horizontal
) {
    Column(modifier = Modifier.clickable(onClick = onClick), horizontalGravity = alignment) {
        Text(
            text = annotatedString {
                pushStyle(SpanStyle(fontSize = 80.sp))
                append(statistic.number)
                statistic.subnumber?.let {
                    pop()
                    pushStyle(SpanStyle(fontSize = 50.sp))
                    append(' ')
                    append(it)
                }
            },
            fontSize = 80.sp,
            fontFamily = Saira,
            style = shadow,
            modifier = Modifier.offset(y = 25.dp)
        )
        Text(
            text = statistic.caption,
            fontSize = 20.sp,
            fontFamily = Saira,
            style = shadow
        )
    }
}