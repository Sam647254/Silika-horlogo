package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ooo.trankvila.silikahorlogo.AwairData
import ooo.trankvila.silikahorlogo.ui.*

@Composable
fun <T : Comparable<T>> DataBarField(
    value: T,
    label: String,
    unit: String = "",
    normalRange: ClosedRange<Int>? = null,
    warning1Range: ClosedRange<Int>? = null
) {
    val color = if (normalRange != null && warning1Range != null) {
        when (value) {
            is Int -> {
                when (value) {
                    in normalRange -> Color.White
                    in warning1Range -> if (value > normalRange.endInclusive) warning3 else warning1
                    else -> if (value > warning1Range.endInclusive) warning4 else warning2
                }
            }
            is Double -> {
                when (value.toInt()) {
                    in normalRange -> Color.White
                    in warning1Range -> if (value > normalRange.endInclusive) warning3 else warning1
                    else -> if (value > warning1Range.endInclusive) warning4 else warning2
                }
            }
            else -> Color.White
        }
    } else {
        Color.White
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            buildAnnotatedString {
                pushStyle(SpanStyle(fontSize = 50.sp))
                append(
                    when (value) {
                        is Int -> value.toString()
                        is String -> value
                        else -> "%.1f".format(
                            value
                        )
                    }
                )
                if (unit.isNotEmpty()) {
                    pop()
                    pushStyle(SpanStyle(fontSize = 25.sp))
                    append(' ')
                    append(unit)
                }
            },
            fontFamily = Saira,
            style = shadow,
            modifier = Modifier.padding(horizontal = 15.dp),
            maxLines = 1,
            softWrap = false,
            color = color
        )
        Text(
            label,
            fontSize = 20.sp,
            fontFamily = Saira,
            style = shadow,
            modifier = Modifier.offset(y = (-10).dp),
            color = color
        )
    }

}