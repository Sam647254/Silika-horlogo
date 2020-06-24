package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ooo.trankvila.silikahorlogo.komponantoj.TickerTapeEntry

class NewsViewModel : ViewModel() {
    val entry = MutableLiveData<TickerTapeEntry>()
    val entries = (0..10).map {
        TickerTapeEntry("Ticker tape entry for $it") {
            Log.d("NewsViewModel", "Ticker tape entry $it clicked")
        }
    }

    private var next = 0

    init {
        val handler = Handler(Looper.getMainLooper())
        val updater = object : Runnable {
            override fun run() {
                entry.postValue(entries[next])
                next = (next + 1) % entries.size
                handler.postDelayed(this, 15_000)
            }
        }
        updater.run()
    }
}