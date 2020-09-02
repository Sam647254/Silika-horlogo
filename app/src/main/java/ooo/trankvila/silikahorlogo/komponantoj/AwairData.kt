package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.animation.Transition
import androidx.compose.animation.core.*
import androidx.compose.animation.core.AnimationConstants.Infinite
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.annotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ooo.trankvila.silikahorlogo.AwairData
import ooo.trankvila.silikahorlogo.ui.*

@Composable
fun <T : Comparable<T>> AwairDataField(
    value: T,
    label: String,
    unit: String = "",
    normalRange: ClosedRange<T>,
    warning1Range: ClosedRange<T>
) {
    val color = when (value) {
        in normalRange -> Color.White
        in warning1Range -> if (value > normalRange.endInclusive) warning3 else warning1
        else -> if (value > warning1Range.endInclusive) warning4 else warning2
    }
    val opacity = FloatPropKey("opacity")
    val pulse = transitionDefinition<String> {
        state("start") {
            this[opacity] = 1F
        }
        state("end") {
            this[opacity] = 0.4F
        }
        transition("start", "end") {
            opacity using repeatable(Infinite, tween(1500, easing = FastOutLinearInEasing))
        }
    }
    Transition(
        definition = pulse,
        toState = if (value in warning1Range) "start" else "end",
        initState = "start"
    ) { state ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier =
            Modifier.drawOpacity(state[opacity])
        ) {
            Text(
                annotatedString {
                    pushStyle(SpanStyle(fontSize = 50.sp))
                    append(if (value is Int) value.toString() else "%.1f".format(value))
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

}

@Composable
fun AwairDataStrip(data: AwairData, offset: Dp) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().offset(y = offset),
        horizontalArrangement = Arrangement.Center
    ) {
        AwairDataField(
            value = data.score,
            label = "Score",
            normalRange = 80..100,
            warning1Range = 60..100
        )
        AwairDataField(
            value = data.temperature,
            label = "Temperature",
            unit = "°C",
            normalRange = 18.0..25.0,
            warning1Range = 16.0..30.0
        )
        AwairDataField(
            value = data.humidity,
            label = "Humidity",
            unit = "%",
            normalRange = 35.0..55.0,
            warning1Range = 20.0..65.0
        )
        AwairDataField(
            value = data.co2,
            label = "Carbon dioxide",
            unit = "PPM",
            normalRange = 0..600,
            warning1Range = 0..1000
        )
        AwairDataField(
            value = data.voc,
            label = "V.O.C.",
            unit = "PPB",
            normalRange = 0..333,
            warning1Range = 0..1000
        )
        AwairDataField(
            value = data.pm25,
            label = "PM2.5",
            unit = "μg/m³",
            normalRange = 0..20,
            warning1Range = 0..55
        )
    }
}