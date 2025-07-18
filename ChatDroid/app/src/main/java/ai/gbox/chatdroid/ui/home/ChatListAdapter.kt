package ai.gbox.chatdroid.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ai.gbox.chatdroid.databinding.ItemChatBinding
import ai.gbox.chatdroid.network.ChatTitleIdResponse
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private var items: List<ChatTitleIdResponse> = emptyList(),
    private val onChatClick: (ChatTitleIdResponse) -> Unit = {},
    private val onChatLongClick: (ChatTitleIdResponse) -> Unit = {}
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    fun update(list: List<ChatTitleIdResponse>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatTitleIdResponse) {
            binding.tvTitle.text = item.title
            
            // Format the updated time
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = Date(item.updatedAt * 1000) // Convert from seconds to milliseconds
            binding.tvTime.text = dateFormat.format(date)
            
            // Set click listeners
            binding.root.setOnClickListener {
                onChatClick(item)
            }
            
            binding.root.setOnLongClickListener {
                onChatLongClick(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
} 