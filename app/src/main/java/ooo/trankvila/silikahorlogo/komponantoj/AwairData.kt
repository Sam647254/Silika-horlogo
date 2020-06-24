package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.text.SpanStyle
import androidx.ui.text.annotatedString
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.AwairData
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow

@Composable
fun AwairDataField(value: String, label: String, unit: String = "") {
    Column(horizontalGravity = Alignment.CenterHorizontally) {
        Text(
            annotatedString {
                pushStyle(SpanStyle(fontSize = 50.sp))
                append(value)
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
            softWrap = false
        )
        Text(
            label,
            fontSize = 20.sp,
            fontFamily = Saira,
            style = shadow,
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}

@Composable
fun AwairDataStrip(data: AwairData) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().offset(y = 40.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        AwairDataField(
            value = data.score.toString(),
            label = "Score"
        )
        AwairDataField(
            value = "%.1f".format(data.temperature),
            label = "Temperature",
            unit = "°C"
        )
        AwairDataField(
            value = "%.1f".format(data.humidity),
            label = "Humidity",
            unit = "%"
        )
        AwairDataField(
            value = data.co2.toString(),
            label = "Carbon dioxide",
            unit = "PPM"
        )
        AwairDataField(
            value = data.voc.toString(),
            label = "V.O.C.",
            unit = "PPB"
        )
        AwairDataField(
            value = data.pm25.toString(),
            label = "PM2.5",
            unit = "μg/m³"
        )
    }
}