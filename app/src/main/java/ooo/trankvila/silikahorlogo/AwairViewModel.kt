package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class AwairViewModel: ViewModel() {
    val data = MutableLiveData<AwairData?>(null)

    fun launch(requestQueue: RequestQueue) {
        val handler = Handler(Looper.getMainLooper())
        val awairUpdater = object : Runnable {
            override fun run() {
                requestQueue.add(StringRequest("http://192.168.1.127/air-data/latest", {
                    JSONObject(it).let { response ->
                        AwairData(
                            response.getInt("score"),
                            response.getDouble("temp"),
                            response.getDouble("humid"),
                            response.getInt("co2"),
                            response.getInt("voc"),
                            response.getInt("pm25")
                        )
                    }.let(data::postValue)
                }, {
                    Log.e("AwairViewModel", it.message, it.cause)
                }))
                handler.postDelayed(this, 15_000)
            }
        }
        awairUpdater.run()
    }
}