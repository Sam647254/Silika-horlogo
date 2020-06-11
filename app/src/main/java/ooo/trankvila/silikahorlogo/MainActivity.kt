package ooo.trankvila.silikahorlogo

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.ui.core.setContent
import ooo.trankvila.silikahorlogo.komponantoj.Clock
import org.joda.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val clockViewModel: SilicanClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        clockViewModel.currentDate.observe(this, Observer { state ->
            setContent {
                Clock(
                    this,
                    state,
                    clockViewModel::refreshCasesCount
                )
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

