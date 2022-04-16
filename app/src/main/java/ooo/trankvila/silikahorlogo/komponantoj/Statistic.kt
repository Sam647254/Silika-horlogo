package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ooo.trankvila.silikahorlogo.*
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.phases
import ooo.trankvila.silikahorlogo.ui.shadow

@Composable
fun StatisticDisplay(
    statistic: DataDisplay,
    onClick: () -> Unit,
    alignment: Alignment.Horizontal
) {
    Column(modifier = Modifier.clickable(onClick = onClick), horizontalAlignment = alignment) {
        when (statistic) {
            is TextData -> {
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(fontSize = 80.sp))
                        append(statistic.text1)
                        statistic.text2?.let {
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
            is TimeData -> {
                val time = SilicanTime.fromStandard(statistic.time)
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(fontSize = 80.sp, color = phases[time.phase]))
                        append(time.hour.toString())
                        append(' ')
                        pop()
                        append(time.minute.toString().padStart(2, '0'))
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
            is LazyTextData -> {
                val text1 = statistic.text1()
                val text2 = statistic.text2?.invoke()
                Text(
                    text = buildAnnotatedString {
                        pushStyle(SpanStyle(fontSize = 80.sp))
                        append(text1)
                        text2?.let {
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
            else -> TODO()
        }
    }
}