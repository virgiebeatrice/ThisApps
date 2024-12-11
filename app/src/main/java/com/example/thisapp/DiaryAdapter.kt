import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.thisapp.DiaryEntry
import com.example.thisapp.R

class DiaryAdapter(private val diaryList: List<DiaryEntry>) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diaryEntry = diaryList[position]
        holder.timeClock.text = diaryEntry.date
        holder.title.text = diaryEntry.title
        holder.content.text = diaryEntry.content
    }

    override fun getItemCount(): Int = diaryList.size

    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeClock: TextView = itemView.findViewById(R.id.timeClock)
        val title: TextView = itemView.findViewById(R.id.title)
        val content: TextView = itemView.findViewById(R.id.content)
    }
}
