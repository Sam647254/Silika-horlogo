package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.offset
import androidx.ui.layout.padding
import androidx.ui.text.SpanStyle
import androidx.ui.text.annotatedString
import androidx.ui.unit.dp
import androidx.ui.unit.sp

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