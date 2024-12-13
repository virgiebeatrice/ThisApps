package com.example.thisapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for RecyclerView
class DiaryAdapter(private val diaryEntries: List<DiaryEntry>) :
    RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    // Creates a new ViewHolder when RecyclerView needs a new one
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_after_diary, parent, false) // Inflate the layout for the item
        return DiaryViewHolder(view)
    }

    // Binds the data to the view holder for the current position
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryEntry = diaryEntries[position]
        holder.bind(diaryEntry) // Binding data to the view holder
        // Atur background sesuai mood
        val moodBackground = when (diaryEntry.mood) {
            "Happy 😄" -> R.drawable.gradient_happy
            "Sad 😢" -> R.drawable.gradient_sad
            "Angry 😡" -> R.drawable.gradient_angry
            "Scared 😨" -> R.drawable.gradient_scared
            "Surprised 😲" -> R.drawable.gradient_surprised
            else -> R.drawable.gradient_neutral
        }
        holder.itemView.setBackgroundResource(moodBackground)

    }


    // Returns the size of the data list
    override fun getItemCount(): Int = diaryEntries.size

    // ViewHolder class to hold the view references
    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val snippetTextView: TextView = itemView.findViewById(R.id.snippetTextView)

        // Bind data to the views
        fun bind(diaryEntry: DiaryEntry) {
            titleTextView.text = diaryEntry.title
            dateTextView.text = diaryEntry.date
            snippetTextView.text = diaryEntry.snippet
        }
    }
}
