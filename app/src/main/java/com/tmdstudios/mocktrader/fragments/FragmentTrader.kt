package com.tmdstudios.mocktrader.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tmdstudios.mocktrader.R
import com.tmdstudios.mocktrader.adapters.TxnAdapter
import com.tmdstudios.mocktrader.viewModels.TraderViewModel
import java.lang.Exception

class FragmentTrader : Fragment() {

    private lateinit var viewModel: TraderViewModel
    private lateinit var rvTxn: RecyclerView
    private lateinit var rvAdapter: TxnAdapter
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_trader, container, false)

        sharedPreferences = this.requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        rvTxn = view.findViewById(R.id.rvTxn)
        rvAdapter = TxnAdapter()
        rvTxn.adapter = rvAdapter
        rvTxn.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(this).get(TraderViewModel::class.java)

        if(sharedPreferences.getBoolean("reset", false)){
            with(sharedPreferences.edit()) {
                putBoolean("reset", false)
                apply()
            }
            viewModel.resetData(sharedPreferences)
        }

        viewModel.loadData(sharedPreferences)

        viewModel.addAction(getString(R.string.welcome))

        viewModel.getNewsDataObserver().observe(viewLifecycleOwner, Observer {
            latestNews -> run {
                view.findViewById<TextView>(R.id.tvNewsSource).text = latestNews.source
                view.findViewById<TextView>(R.id.tvNews).text = latestNews.news
            }
        })
        viewModel.getGameDataObserver().observe(viewLifecycleOwner, Observer {
            gameData -> run {
                view.findViewById<TextView>(R.id.tvDay).text = "Day ${gameData.day}"
                view.findViewById<TextView>(R.id.tvBtcPrice).text = String.format("BTC Price: $%.3f", gameData.btcPrice)
                view.findViewById<TextView>(R.id.tvTrend).text = String.format("Trend: %.3f", gameData.trend)+"%"
                when{
                    gameData.trend < 0 -> view.findViewById<TextView>(R.id.tvTrend).setTextColor(Color.RED)
                    gameData.trend > 0 -> view.findViewById<TextView>(R.id.tvTrend).setTextColor(Color.rgb(0,185,15))
                    else -> view.findViewById<TextView>(R.id.tvTrend).setTextColor(Color.BLACK)
                }
                view.findViewById<TextView>(R.id.tvMoney).text = String.format("Money: $%.2f", gameData.money)
                view.findViewById<TextView>(R.id.tvBTC).text = String.format("BTC: %.8f", gameData.btc)
                view.findViewById<TextView>(R.id.tvTotalWealth).text = String.format("Total Wealth: $%.2f", gameData.total)
                viewModel.saveData(sharedPreferences)
            }
        })
        viewModel.getActionsObserver().observe(viewLifecycleOwner, Observer {
            actionList -> rvAdapter.setData(actionList.reversed())
        })
        viewModel.getErrorMessageObserver().observe(viewLifecycleOwner, Observer {
            errorMessage -> Snackbar.make(view.findViewById(R.id.clMain), errorMessage, Snackbar.LENGTH_LONG).show()
        })

        view.findViewById<Button>(R.id.btHome).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_fragmentTrader_to_fragmentHome)
        }

        view.findViewById<Button>(R.id.btBuy).setOnClickListener {
            try{
                viewModel.buy(view.findViewById<EditText>(R.id.etAmount).text.toString().toDouble())
            }catch(e: Exception){
                viewModel.buy(0.0)
            }
        }

        view.findViewById<Button>(R.id.btSell).setOnClickListener {
            try{
                viewModel.sell(view.findViewById<EditText>(R.id.etAmount).text.toString().toDouble())
            }catch(e: Exception){
                viewModel.sell(0.0)
            }
        }

        view.findViewById<Button>(R.id.btSkip).setOnClickListener {
            viewModel.skip()
        }

        return view
    }

}