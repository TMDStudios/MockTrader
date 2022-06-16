package com.tmdstudios.mocktrader.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tmdstudios.mocktrader.R
import com.tmdstudios.mocktrader.models.News
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import kotlin.random.Random

class TraderViewModel: ViewModel() {
    private var newsData: MutableLiveData<News> = MutableLiveData()

    private var day = 0
    private val lastDay = 0
    private val money = 10000.0
    private val btc = 0.0
    private val total = 10000.0
    private val btcPrice = 50000.0
    private val lastBtcPrice = 50000.0
    private val trend = 0.0
    private var actions: MutableLiveData<ArrayList<String>> = MutableLiveData()
    private var listOfActions: ArrayList<String> = ArrayList()

    init {

    }

    fun getActionsObserver(): MutableLiveData<ArrayList<String>> {
        return actions
    }

    fun getNewsDataObserver(): MutableLiveData<News> {
        return newsData
    }

    fun requestAPI(){
        CoroutineScope(IO).launch {
            val data = async { fetchData() }.await()
            if(data.isNotEmpty()){
                populateRV(data)
            }else{
                Log.d("TraderViewModel", "Unable to get data")
            }
        }
    }

    private fun fetchData(): String{
        var response = ""
        try{
            response = URL("https://raw.githubusercontent.com/TMDStudios/json_files/main/random_news.json").readText()
        }catch(e: Exception){
            Log.d("TraderViewModel", "ISSUE: $e")
        }
        return response
    }

    private suspend fun populateRV(result: String){
        withContext(Main){
            val jsonArray = JSONArray(result)
            var newsItem = Random.nextInt(jsonArray.length())
            val source = jsonArray.getJSONObject(newsItem).getString("source")
            val news = jsonArray.getJSONObject(newsItem).getString("news")
            val effect = jsonArray.getJSONObject(newsItem).getString("effect")
            newsData.postValue(News(source, news, effect.toDouble()))
        }
    }

    fun addAction(actionMsg: String){
        listOfActions.add(actionMsg)
        actions.postValue(listOfActions)
    }

}