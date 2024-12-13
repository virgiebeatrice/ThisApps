package com.example.thisapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thisapp.HistoryItem
import com.example.thisapp.R

class HistoryAdapter(private val historyList: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Menghubungkan layout history_item.xml dengan ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.bind(historyItem)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.timeClock)
        private val titleTextView: TextView = itemView.findViewById(R.id.title)
        private val contentTextView: TextView = itemView.findViewById(R.id.content)

        fun bind(historyItem: HistoryItem) {
            // Mengikat data ke masing-masing TextView
            dateTextView.text = historyItem.date
            titleTextView.text = historyItem.title
            contentTextView.text = historyItem.content
        }
    }
}
