package com.example.silikahorlogo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.lifecycle.Observer
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.offset
import androidx.ui.text.TextStyle
import androidx.ui.text.font.ResourceFont
import androidx.ui.text.font.fontFamily
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.example.silikahorlogo.ui.SilikaHorloĝoTheme

private val Saira = fontFamily(ResourceFont(R.font.saira_regular))
private val SairaSemibold = fontFamily(ResourceFont(R.font.saira_semibold))

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clockViewModel.currentDate.observe(this, Observer { (date, time) ->
            setContent {
                SilikaHorloĝoTheme {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalGravity = Alignment.CenterHorizontally
                    ) {
                        Column(Modifier.offset(y = 60.dp)) {
                            Text(
                                date.year.toString(), style = TextStyle(fontSize = 36.sp),
                                fontFamily = Saira
                            )
                            Text(
                                date.textDate, style = TextStyle(fontSize = 44.sp),
                                fontFamily = Saira
                            )
                        }
                        Text(
                            "${time.hour} ${time.minute}", fontSize = 240.sp,
                            fontFamily = SairaSemibold
                        )
                    }
                }
            }
        })
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