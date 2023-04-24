package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R

class ItemAdapter(val items: List<String>) :
    RecyclerView.Adapter<ItemAdapter.TownViewHolder>() {

    class TownViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItem: TextView = itemView.findViewById(R.id.tvText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TownViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return TownViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TownViewHolder, position: Int) {
        holder.tvItem.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}