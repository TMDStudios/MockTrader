package com.tmdstudios.mocktrader.viewModels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
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

    private var myGameData = GameData(0, 0, 10000.0, 0.0, 10000.0, 50000.0, 50000.0, 0.0)
    private var actions: MutableLiveData<ArrayList<String>> = MutableLiveData()
    private var listOfActions: ArrayList<String> = ArrayList()

    private var errorMessage: MutableLiveData<String> = MutableLiveData()

    init {
        gameData.postValue(myGameData)
        handleNews()
    }

    fun getErrorMessageObserver(): MutableLiveData<String> {
        return errorMessage
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

    private fun handleNews(){
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
        if(amount>0){
            if(amount <= gameData.value!!.money){
                myGameData.day++
                handleNews()
                myGameData.money-=amount
                myGameData.btc+=amount/myGameData.btcPrice
                addAction("Day ${myGameData.day} - Bought ${amount/myGameData.btcPrice} BTC at $${myGameData.btcPrice}")
                myGameData.lastBtcPrice = myGameData.btcPrice
                myGameData.total = myGameData.btc*myGameData.btcPrice+myGameData.money
                val priceDifference = updatePrice()
                if(priceDifference!=0.0){
                    myGameData.btcPrice+=priceDifference
                    gameData.postValue(myGameData)
                }else{
                    errorMessage.postValue("Unable to get news data\nCheck internet connection")
                }
            }else{
                errorMessage.postValue("Insufficient funds!")
            }
        }else{
            errorMessage.postValue("Amount must be greater than 0")
        }
    }

    fun sell(amount: Double){
        if(amount>0){
            if(amount <= gameData.value!!.btc * gameData.value!!.btcPrice){
                myGameData.day++
                handleNews()
                myGameData.money+=amount
                myGameData.btc-=amount/myGameData.btcPrice
                addAction("Day ${myGameData.day} - Sold ${amount/myGameData.btcPrice} BTC at $${myGameData.btcPrice}")
                myGameData.lastBtcPrice = myGameData.btcPrice
                myGameData.total = myGameData.btc*myGameData.btcPrice+myGameData.money
                val priceDifference = updatePrice()
                if(priceDifference!=0.0){
                    myGameData.btcPrice+=priceDifference
                    gameData.postValue(myGameData)
                }else{
                    errorMessage.postValue("Unable to get news data\nCheck internet connection")
                }
            }else{
                errorMessage.postValue("Insufficient funds!")
            }
        }else{
            errorMessage.postValue("Amount must be greater than 0")
        }
    }

    fun skip(){
        myGameData.day++
        handleNews()
        addAction("Day ${myGameData.day} - Skipped")
        val priceDifference = updatePrice()
        if(priceDifference!=0.0){
            myGameData.btcPrice+=priceDifference
            gameData.postValue(myGameData)
        }else{
            errorMessage.postValue("Unable to get news data\nCheck internet connection")
        }
    }

    private fun updatePrice(): Double{
        var priceDifference = 0.0
        // Add price volatility (linked to effect)
        try{
            val r = Random.nextInt(10)/10
            priceDifference = if(newsData.value!!.effect > 0){
                val priceChange = 0.025 + (0.05 - 0.033) * r
                myGameData.btcPrice*priceChange
            }else{
                val priceChange = 0.033 + (0.05 - 0.033) * r
                myGameData.btcPrice*priceChange
            }

            // Add news effect
            myGameData.btcPrice += myGameData.btcPrice * newsData.value!!.effect
            myGameData.trend = (1-myGameData.lastBtcPrice/myGameData.btcPrice) * 100
        }catch(e: Exception){
            Log.d("TraderViewModel", "ISSUE: $e")
        }

        return priceDifference
    }

    fun saveData(sharedPreferences: SharedPreferences){
        with(sharedPreferences.edit()) {
            putInt("day", myGameData.day)
            putInt("lastDay", myGameData.lastDay)
            putString("money", myGameData.money.toString())
            putString("btc", myGameData.btc.toString())
            putString("total", myGameData.total.toString())
            putString("btcPrice", myGameData.btcPrice.toString())
            putString("lastBtcPrice", myGameData.lastBtcPrice.toString())
            putString("trend", myGameData.trend.toString())
            apply()
        }
    }

    fun loadData(sharedPreferences: SharedPreferences){
        myGameData = GameData(
            sharedPreferences.getInt("day", 0),
            sharedPreferences.getInt("lastDay", 0),
            sharedPreferences.getString("money", "10000").toString().toDouble(),
            sharedPreferences.getString("btc", "0").toString().toDouble(),
            sharedPreferences.getString("total", "10000").toString().toDouble(),
            sharedPreferences.getString("btcPrice", "50000").toString().toDouble(),
            sharedPreferences.getString("lastBtcPrice", "50000").toString().toDouble(),
            sharedPreferences.getString("trend", "0").toString().toDouble()
        )
        gameData.postValue(myGameData)
    }

    fun resetData(sharedPreferences: SharedPreferences){
        myGameData = GameData(0, 0, 10000.0, 0.0, 10000.0, 50000.0, 50000.0, 0.0)
        gameData.postValue(myGameData)
        saveData(sharedPreferences)
    }
}