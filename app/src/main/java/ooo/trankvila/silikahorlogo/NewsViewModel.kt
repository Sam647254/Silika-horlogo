package ooo.trankvila.silikahorlogo

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ooo.trankvila.silikahorlogo.komponantoj.TickerTapeEntry
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NewsViewModel : ViewModel() {
    var synthesizeSpeech: ((String) -> Unit)? = null
    val entry = MutableLiveData<TickerTapeEntry>()

    private var next = 0

    init {
        val handler = Handler(Looper.getMainLooper())

        viewModelScope.launch {
            val news = withContext(Dispatchers.IO) {
                fetch()?.let(::JSONObject)?.getJSONArray("articles")?.let { articles ->
                    (0 until articles.length()).map {
                        val article = articles.getJSONObject(it)
                        Headline(article.getString("title"), article.getString("link"))
                    }
                }
            } ?: return@launch
            val updater = object : Runnable {
                override fun run() {
                    entry.postValue(news[next].let {
                        TickerTapeEntry(it.title) {
                            viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    val body = fetchArticle(it.link)
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
        }
    }

    private fun fetch() =
        (URL("https://api.breakingapi.com/news?q=coronavirus&type=headlines&locale=en-US&api_key=$BreakingApiKey")
            .openConnection() as? HttpsURLConnection)?.run {
            requestMethod = "GET"
            try {
                inputStream.bufferedReader().use {
                    it.readText()
                }
            } catch (e: IOException) {
                Log.d("NewsViewModel", errorStream.bufferedReader().readText(), e)
                return null
            }
        }

    private fun fetchArticle(url: String) =
        (URL("https://api.breakingapi.com/articles?api_key=$BreakingApiKey&link=$url").openConnection() as HttpsURLConnection).run {
            requestMethod = "GET"
            inputStream.bufferedReader().use {
                it.readText()
            }
        }.let(::JSONObject).getJSONObject("article").getString("body")
}

private data class Headline(val title: String, val link: String)