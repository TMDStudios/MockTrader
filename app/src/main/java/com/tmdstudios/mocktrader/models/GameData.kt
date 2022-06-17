package com.tmdstudios.mocktrader.models

data class GameData(
    var day: Int,
    val lastDay: Int,
    var money: Double,
    var btc: Double,
    val total: Double,
    var btcPrice: Double,
    var lastBtcPrice: Double,
    val trend: Double
)