package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AwairViewModel: ViewModel() {
    val data = MutableLiveData<AwairData?>(null)
    private var cleared = false

    init {
        val handler = Handler(Looper.getMainLooper())
        val awairUpdater = object : Runnable {
            override fun run() {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        fetchAwairData().let(data::postValue)
                    }
                }
                if (!cleared) {
                    handler.postDelayed(this, 15000)
                }
            }
        }
        awairUpdater.run()
    }

    override fun onCleared() {
        cleared = true
    }

    private fun fetchAwairData(): AwairData? {
        val url = URL("http://awair-elem-14041c/air-data/latest")
        return (url.openConnection() as? HttpURLConnection)?.run {
            requestMethod = "GET"
            inputStream.bufferedReader().use {
                it.readText().let(::JSONObject).let { response ->
                    AwairData(
                        response.getInt("score"),
                        response.getDouble("temp"),
                        response.getDouble("humid"),
                        response.getInt("co2"),
                        response.getInt("voc"),
                        response.getInt("pm25")
                    )
                }
            }
        }
    }
}