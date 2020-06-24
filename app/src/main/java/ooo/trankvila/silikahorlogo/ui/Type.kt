package ooo.trankvila.silikahorlogo.ui

import androidx.ui.graphics.Color
import androidx.ui.graphics.Shadow
import androidx.ui.material.Typography
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontWeight
import androidx.ui.text.font.ResourceFont
import androidx.ui.text.font.fontFamily
import androidx.ui.unit.sp
import ooo.trankvila.silikahorlogo.R

// Set of Material typography styles to start with
val typography = Typography(
        body1 = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
        )
        /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)
val Saira = fontFamily(ResourceFont(R.font.saira_regular))
val SairaSemibold = fontFamily(ResourceFont(R.font.saira_semibold))
val TimeShadow = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 10F))

val shadow = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 3F))
val shadowStyle = Shadow(color = Color.White, blurRadius = 3F)