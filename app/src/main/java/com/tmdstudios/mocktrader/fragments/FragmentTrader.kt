package com.tmdstudios.mocktrader.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tmdstudios.mocktrader.R
import com.tmdstudios.mocktrader.adapters.TxnAdapter
import com.tmdstudios.mocktrader.viewModels.TraderViewModel

class FragmentTrader : Fragment() {

    private lateinit var viewModel: TraderViewModel
    private lateinit var rvTxn: RecyclerView
    private lateinit var rvAdapter: TxnAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_trader, container, false)

        rvTxn = view.findViewById(R.id.rvTxn)
        rvAdapter = TxnAdapter()
        rvTxn.adapter = rvAdapter
        rvTxn.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(this).get(TraderViewModel::class.java)
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
            view.findViewById<TextView>(R.id.tvBtcPrice).text = "BTC Price: $${gameData.btcPrice}"
            view.findViewById<TextView>(R.id.tvTrend).text = "Trend: ${gameData.trend}%"
            view.findViewById<TextView>(R.id.tvMoney).text = "Money: $${gameData.money}"
            view.findViewById<TextView>(R.id.tvBTC).text = "BTC: ${gameData.btc}"
            view.findViewById<TextView>(R.id.tvTotalWealth).text = "Total Wealth: $${gameData.total}"
        }
        })
        viewModel.getActionsObserver().observe(viewLifecycleOwner, Observer {
            actionList -> rvAdapter.setData(actionList)
        })

        view.findViewById<Button>(R.id.btHome).setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_fragmentTrader_to_fragmentHome)
        }

        view.findViewById<Button>(R.id.btBuy).setOnClickListener {
            viewModel.buy(100.0)
        }

        view.findViewById<Button>(R.id.btSkip).setOnClickListener {
            viewModel.requestAPI()
        }

        return view
    }

}