package com.tmdstudios.mocktrader.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tmdstudios.mocktrader.R
import com.tmdstudios.mocktrader.models.GameData
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
    private var gameData: MutableLiveData<GameData> = MutableLiveData()

    private var myGameData: GameData
    private var actions: MutableLiveData<ArrayList<String>> = MutableLiveData()
    private var listOfActions: ArrayList<String> = ArrayList()

    init {
        myGameData = GameData(0, 0, 10000.0, 0.0, 10000.0, 50000.0, 50000.0, 0.0)
        gameData.postValue(myGameData)
    }

    fun getGameDataObserver(): MutableLiveData<GameData> {
        return gameData
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

    fun buy(amount: Double){
        if(amount <= gameData.value!!.money){
            myGameData.day++
            requestAPI()
            myGameData.money-=amount
            myGameData.btc+=amount/myGameData.btcPrice
            addAction("Day ${myGameData.day} - Bought ${amount/myGameData.btcPrice} BTC at $${myGameData.btcPrice}")
            myGameData.lastBtcPrice = myGameData.btcPrice
            val r = Random.nextInt(10)/10
            try{
                if(newsData.value!!.effect > 0){
                    val priceChange = 0.025 + (0.05 - 0.033) * r
                    myGameData.btcPrice+=myGameData.btcPrice*priceChange
                }else{
                    val priceChange = 0.033 + (0.05 - 0.033) * r
                    myGameData.btcPrice+=myGameData.btcPrice*priceChange
                }
            }catch(e: Exception){
                Log.d("TraderViewModel", "ISSUE: $e")
            }
            gameData.postValue(myGameData)
        }
    }

    fun sell(){}

    fun skip(){}

}