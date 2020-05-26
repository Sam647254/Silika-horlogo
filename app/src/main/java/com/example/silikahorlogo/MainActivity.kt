package com.example.silikahorlogo

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.lifecycle.Observer
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.graphics.Shadow
import androidx.ui.graphics.imageFromResource
import androidx.ui.layout.*
import androidx.ui.material.Surface
import androidx.ui.text.TextStyle
import androidx.ui.text.font.ResourceFont
import androidx.ui.text.font.fontFamily
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.example.silikahorlogo.ui.SilikaHorloĝoTheme

private val Saira = fontFamily(ResourceFont(R.font.saira_regular))
private val SairaSemibold = fontFamily(ResourceFont(R.font.saira_semibold))
private val TimeShadow = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 10F))
private val shadow = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 3F))

private val weekdayColours = listOf(
    R.color.lavender,
    R.color.carnation,
    R.color.sapphire,
    R.color.ruby,
    R.color.amber,
    R.color.fern,
    R.color.slate
)
private val phaseColours = listOf(
    R.color.chaos,
    R.color.serenity,
    R.color.fervidity
)

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        clockViewModel.currentDate.observe(this, Observer { (date, time) ->
            setContent {
                SilikaHorloĝoTheme(darkTheme = true) {
                    Surface {
                        Stack {
                            Image(
                                asset = imageFromResource(resources, R.drawable.background_top),
                                colorFilter = ColorFilter.tint(
                                    resources.getColor(weekdayColours[date.weekday - 1], null)
                                        .let(::Color)
                                )
                            )
                            Image(
                                asset = imageFromResource(
                                    resources,
                                    R.drawable.background_bottom
                                ),
                                colorFilter = ColorFilter.tint(
                                    resources.getColor(phaseColours[time.phase], null)
                                        .let(::Color)
                                )
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalGravity = Alignment.CenterHorizontally
                            ) {
                                Column {
                                    Box(Modifier.offset(y = 10.dp)) {
                                        Text(
                                            date.year.toString(),
                                            fontSize = 46.sp,
                                            style = shadow,
                                            fontFamily = Saira
                                        )
                                    }
                                    Text(
                                        date.textDate, fontSize = 54.sp,
                                        fontFamily = Saira,
                                        style = shadow
                                    )
                                }
                                Box(Modifier.offset(y = (-50).dp)) {
                                    Text(
                                        "${time.hour} ${time.minute.toString().padStart(2, '0')}",
                                        fontSize = 260.sp,
                                        fontFamily = SairaSemibold,
                                        style = TimeShadow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SilikaHorloĝoTheme {
        Greeting("Android")
    }
}