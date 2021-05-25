package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow

enum class DataBarItem {
    CurrentWeather,
    IndoorConditions,
    BCStats,
    WashingtonStats,
    FoodMenu
}

val dataBarItems = listOf(
        "Current\nweather" to DataBarItem.CurrentWeather,
        "Indoor\nconditions" to DataBarItem.IndoorConditions,
        "BC\nCOVID-19 stats" to DataBarItem.BCStats,
        "Washington\nCOVID-19 stats" to DataBarItem.WashingtonStats,
        "Food menu" to DataBarItem.FoodMenu,
)

@Composable
fun DataCategoryBar(current: DataBarItem, held: Boolean, onClick: (DataBarItem) -> Unit) {
    Row(
            Modifier.fillMaxWidth(),
            Arrangement.Center,
            Alignment.CenterVertically
    ) {
        for ((label, item) in dataBarItems) {
            Text(
                    text = label,
                    fontSize = 30.sp,
                    fontFamily = Saira,
                    style = shadow,
                    modifier = Modifier
                            .alpha(if (current == item) 1F else if (held) 0.1F else 0.5F)
                            .padding(horizontal = 15.dp)
                            .clickable { onClick(item) },
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
            )
        }
    }
}

data class DataBarFieldData(
        val value: Any,
        val unit: String,
        val label: String,
        val normalRange: ClosedRange<Int>? = null,
        val warning1Range: ClosedRange<Int>? = null
)

data class DataBar(val fields: List<DataBarFieldData>, val caption: String? = null)

@Composable
fun DataFieldBar(dataBar: DataBar, onClick: () -> Unit) {
    if (dataBar.caption == null) {
        Column(
                Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp)
                        .clickable { onClick() }
        ) {
            for (field in dataBar.fields) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                            buildAnnotatedString {
                                pushStyle(SpanStyle(fontSize = 20.sp))
                                append(field.label)
                                append("  ")
                                pushStyle(SpanStyle(fontSize = 35.sp))
                                append(field.value as String)
                            },
                            style = shadow,
                            fontFamily = Saira,
                            maxLines = 1
                    )
                }
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onClick() }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                for (field in dataBar.fields) {
                    when (val value = field.value) {
                        is Int -> {
                            DataBarField(
                                    value = value,
                                    label = field.label,
                                    unit = field.unit,
                                    normalRange = field.normalRange,
                                    warning1Range = field.warning1Range
                            )
                        }
                        is Double -> {
                            DataBarField(
                                    value = value,
                                    label = field.label,
                                    unit = field.unit,
                            )
                        }
                        is String -> {
                            DataBarField(value = value, label = field.label, unit = field.unit)
                        }
                    }
                }
            }
            Text(text = dataBar.caption, fontFamily = Saira, style = shadow, fontSize = 17.sp)
        }
    }
}