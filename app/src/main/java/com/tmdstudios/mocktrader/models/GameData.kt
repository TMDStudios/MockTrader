package com.tmdstudios.mocktrader.models

data class GameData(
    var day: Int,
    var lastDay: Int,
    var money: Double,
    var btc: Double,
    var total: Double,
    var btcPrice: Double,
    var lastBtcPrice: Double,
    var trend: Double
)