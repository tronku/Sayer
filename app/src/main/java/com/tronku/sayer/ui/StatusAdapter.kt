package com.tronku.sayer.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter: ListAdapter<Status, StatusAdapter.StatusViewHolder>(StatusDiffCallback) {

    inner class StatusViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
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
}