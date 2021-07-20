package com.tronku.sayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tronku.sayer.R
import com.tronku.sayer.utils.Utils

class StatusAdapter(private val itemClickListener: LocationItemClickListener):
    ListAdapter<Status, RecyclerView.ViewHolder>(StatusDiffCallback) {

    inner class SyncedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val statusText = itemView.findViewById<TextView>(R.id.status_text)
        private val timeText = itemView.findViewById<TextView>(R.id.date_text)

        fun bind(status: Status) {
            statusText.text = status.status
            timeText.text = Utils.getDateTime(status.timestamp)
        }
    }

    inner class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val timeText = itemView.findViewById<TextView>(R.id.date_text)
        private val deviceId = itemView.findViewById<TextView>(R.id.device_id_text)
        private val latLongText = itemView.findViewById<TextView>(R.id.lat_long_text)
        private val distanceText = itemView.findViewById<TextView>(R.id.distance_text)
        private val sendMessageButton = itemView.findViewById<ImageView>(R.id.send_message_button)

        fun bind(status: Status) {
            val data = status.status.split(';')
            timeText.text = Utils.getDateTime(status.timestamp)
            deviceId.text = "id: ${data[0]}"
            latLongText.text = String.format("%.3f, %.3f", data[1].toDouble(), data[2].toDouble())
            distanceText.text = Utils.getDistance(data[1].toDouble(), data[2].toDouble())

            sendMessageButton.setOnClickListener {
                itemClickListener.onChatClicked(data[0])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            ReceivedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.received_layout, parent, false))
        } else {
            SyncedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.synced_layout, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (currentList[position].type.ordinal == 0) {
            (holder as ReceivedViewHolder).bind(currentList[position])
        } else {
            (holder as SyncedViewHolder).bind(currentList[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type.ordinal
    }

    object StatusDiffCallback: DiffUtil.ItemCallback<Status>() {
        override fun areItemsTheSame(oldItem: Status, newItem: Status): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Status, newItem: Status): Boolean {
            return oldItem.status == newItem.status &&
                    oldItem.timestamp == newItem.timestamp
        }
    }

    interface LocationItemClickListener {
        fun onChatClicked(deviceId: String)
    }

}