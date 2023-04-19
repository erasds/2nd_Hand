package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R

class TownAdapter(val towns: List<String>) :
    RecyclerView.Adapter<TownAdapter.TownViewHolder>() {

    class TownViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTown: TextView = itemView.findViewById(R.id.tvTown)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TownViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_town, parent, false)
        return TownViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TownViewHolder, position: Int) {
        holder.tvTown.text = towns[position]
    }

    override fun getItemCount(): Int = towns.size
}