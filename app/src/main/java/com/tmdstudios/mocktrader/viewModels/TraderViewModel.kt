package com.tmdstudios.mocktrader.viewModels

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.tmdstudios.mocktrader.models.GameData
import com.tmdstudios.mocktrader.models.News
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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
        if(myGameData.day==1){addAction("")}
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
                addAction(String.format("Day ${myGameData.day} - Bought %.8f BTC at $%.3f", amount/myGameData.btcPrice, myGameData.btcPrice))
                myGameData.total = myGameData.btc*myGameData.btcPrice+myGameData.money
                updatePrice()
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
                addAction(String.format("Day ${myGameData.day} - Sold %.8f BTC at $%.3f", amount/myGameData.btcPrice, myGameData.btcPrice))
                myGameData.total = myGameData.btc*myGameData.btcPrice+myGameData.money
                updatePrice()
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
        updatePrice()
    }

    private fun updatePrice(){
        myGameData.lastBtcPrice = myGameData.btcPrice
        // Add price volatility (linked to effect)
        try{
            val r = Random.nextDouble(0.01, 0.055)
            val priceDifference = if(newsData.value!!.effect>0){0.05-r}else{0.01-r}
            myGameData.btcPrice += myGameData.btcPrice * (newsData.value!!.effect+priceDifference)
            if(myGameData.btcPrice<10000){myGameData.btcPrice=10000.0+100*priceDifference}
            myGameData.trend = (1-myGameData.lastBtcPrice/myGameData.btcPrice) * 100
            gameData.postValue(myGameData)
        }catch(e: Exception){
            errorMessage.postValue("Unable to get news data\nCheck internet connection")
            Log.d("TraderViewModel", "ISSUE: $e")
        }
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
            // Save ArrayList in sharedPreferences
            val actionsData = Gson().toJson(listOfActions)
            putString("actions", actionsData)
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
        val savedActions = sharedPreferences.getString("actions", null)
        if(savedActions!=null){
            val gson = Gson()
            listOfActions = gson.fromJson(savedActions, ArrayList<String>()::class.java)
        }

        actions.postValue(listOfActions)
    }

    fun resetData(sharedPreferences: SharedPreferences){
        myGameData = GameData(0, 0, 10000.0, 0.0, 10000.0, 50000.0, 50000.0, 0.0)
        gameData.postValue(myGameData)
        saveData(sharedPreferences)
    }
}