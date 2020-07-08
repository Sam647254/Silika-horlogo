package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.JsonReader
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.*
import ooo.trankvila.silikahorlogo.komponantoj.TickerTapeEntry
import org.json.JSONObject
import java.io.IOException
import java.lang.Runnable
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NewsViewModel : ViewModel() {
    var synthesizeSpeech: ((String) -> Unit)? = null
    val entry = MutableLiveData<TickerTapeEntry>()

    private var next = 0

    fun launch(requestQueue: RequestQueue) {
        val handler = Handler(Looper.getMainLooper())

        requestQueue.add(
            JsonObjectRequest(
                "https://api.breakingapi.com/news?q=coronavirus&type=headlines&locale=en-US&api_key=$BreakingApiKey",
                null, { response ->
                    val news = response.getJSONArray("articles").let { articles ->
                        (0 until articles.length()).map {
                            val article = articles.getJSONObject(it)
                            Headline(article.getString("title"), article.getString("link"))
                        }
                    }

                    val updater = object : Runnable {
                        override fun run() {
                            entry.postValue(news[next].let {
                                TickerTapeEntry(it.title) {
                                    viewModelScope.launch {
                                        withContext(Dispatchers.IO) {
                                            val body = fetchArticle(requestQueue, it.link)
                                            synthesizeSpeech?.invoke(body)
                                        }
                                    }
                                }
                            })
                            next = (next + 1) % news.size
                            handler.postDelayed(this, 15_000)
                        }
                    }
                    updater.run()
                }, { error ->
                    Log.d("NewsViewModel", error.message, error.cause)
                })
        )
    }

    private suspend fun fetchArticle(requestQueue: RequestQueue, url: String): String {
        val result = CompletableDeferred<String>()
        requestQueue.add(JsonObjectRequest(url, null, { response ->
            result.complete(response.getJSONObject("article").getString("body"))
        }, {
            result.completeExceptionally(it.cause ?: Exception("Unknown error"))
        }))
        return result.await()
    }
}

private data class Headline(val title: String, val link: String)