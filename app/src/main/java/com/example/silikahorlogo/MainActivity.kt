package com.example.silikahorlogo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.lifecycle.Observer
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.tooling.preview.Preview
import com.example.silikahorlogo.ui.SilikaHorloĝoTheme

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clockViewModel.currentDate.observe(this, Observer { (date, time) ->
            setContent {
                SilikaHorloĝoTheme {
                    Column {
                        Text(text = date.toString())
                        Text(text = time.toString())
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