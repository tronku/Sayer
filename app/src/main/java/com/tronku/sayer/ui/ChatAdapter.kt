package com.tronku.sayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tronku.sayer.R
import com.tronku.sayer.data.Chat
import com.tronku.sayer.data.ChatDirection

class ChatAdapter: RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val chatList = arrayListOf<Chat>()

    inner class ChatViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

        fun bind(chat: Chat) {
            val msgText = view.findViewById<TextView>(R.id.message_text)
            msgText.text = chat.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            when(viewType) {
                ChatDirection.INCOMING.ordinal -> {
                    LayoutInflater.from(parent.context).inflate(R.layout.incoming_msg_layout, parent, false)
                }
                else -> {
                    LayoutInflater.from(parent.context).inflate(R.layout.outgoing_msg_layout, parent, false)
                }
            }
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return chatList[position].direction.ordinal
    }

    fun addMessage(chat: Chat) {
        chatList.add(0, chat)
        notifyItemInserted(0)
    }
}