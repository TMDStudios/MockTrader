package com.tmdstudios.mocktrader.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tmdstudios.mocktrader.databinding.TradeActionBinding

class TxnAdapter(): RecyclerView.Adapter<TxnAdapter.TxnViewHolder>() {
    class TxnViewHolder(val binding: TradeActionBinding): RecyclerView.ViewHolder(binding.root)

    private var messages = emptyList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxnViewHolder {
        return TxnViewHolder(TradeActionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TxnViewHolder, position: Int) {
        holder.binding.apply {
            tvMessage.text = messages[position]
        }
    }

    override fun getItemCount() = messages.size

    fun setData(messages: List<String>){
        this.messages = messages
        notifyDataSetChanged()
    }
}