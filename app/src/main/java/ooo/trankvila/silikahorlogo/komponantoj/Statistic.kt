package ooo.trankvila.silikahorlogo.komponantoj

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.text.SpanStyle
import androidx.ui.text.annotatedString
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.ui.Saira
import ooo.trankvila.silikahorlogo.ui.shadow

@Composable
fun Statistic(number: String, subnumber: String? = null, caption: String, onClick: () -> Unit) {
    Column(
        Modifier.padding(end = 10.dp, bottom = 10.dp),
        horizontalGravity = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(modifier = Modifier.clickable(onClick = onClick)) {
            Text(
                text = annotatedString {
                    pushStyle(SpanStyle(fontSize = 80.sp))
                    append(number)
                    subnumber?.let {
                        pop()
                        pushStyle(SpanStyle(fontSize = 50.sp))
                        append(it)
                    }
                },
                fontSize = 80.sp,
                fontFamily = Saira,
                style = shadow,
                modifier = Modifier.offset(y = 25.dp)
            )
            Text(
                text = caption,
                fontSize = 20.sp,
                fontFamily = Saira,
                style = shadow
            )
        }
    }
}