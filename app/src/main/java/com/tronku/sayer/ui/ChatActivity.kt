package com.tronku.sayer.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bridgefy.sdk.client.BFEnergyProfile
import com.bridgefy.sdk.client.BFEngineProfile
import com.bridgefy.sdk.client.Bridgefy
import com.bridgefy.sdk.client.Message
import com.tronku.sayer.R
import com.tronku.sayer.data.Chat
import com.tronku.sayer.data.ChatDirection
import com.tronku.sayer.utils.Storage
import com.tronku.sayer.utils.Utils
import java.sql.Timestamp

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageView
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        initViews()
        setAdapter()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(object: BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val chat = Chat(intent?.getStringExtra(Storage.CONVERSATION_EXTRA).toString(), ChatDirection.INCOMING)
                    chatAdapter.addMessage(chat)
                }
            }, IntentFilter(intent.getStringExtra(Storage.CONVERSATION_ID)))
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.chat_recyclerview)
        messageEditText = findViewById(R.id.send_message_edittext)
        sendButton = findViewById(R.id.send_button)

        sendButton.setOnClickListener {
            if (messageEditText.text.trim().isNotEmpty()) {
                sendMessage(messageEditText.text.toString())
                messageEditText.setText("")
            }
        }
    }

    private fun setAdapter() {
        chatAdapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = chatAdapter
    }

    private fun sendMessage(msg: String) {
        val chat = Chat(msg, ChatDirection.OUTGOING)
        chatAdapter.addMessage(chat)

        val content = hashMapOf<String, Any>().apply {
            put(Storage.CHAT_PAYLOAD, msg)
        }
        val builder = Message.Builder()
        builder.setContent(content).setReceiverId(intent.getStringExtra(Storage.CONVERSATION_ID))
        Bridgefy.sendMessage(builder.build(), BFEngineProfile.BFConfigProfileLongReach)
    }
}