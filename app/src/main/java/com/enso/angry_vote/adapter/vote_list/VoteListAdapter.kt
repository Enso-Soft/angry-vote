package com.enso.angry_vote.adapter.vote_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.enso.angry_vote.databinding.ItemVoteBinding
import com.enso.angry_vote.databinding.ItemVoteSelectBinding
import com.enso.angry_vote.model.VoteItem
import com.enso.angry_vote.model.VoteSelectItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VoteListAdapter : ListAdapter<VoteItem, VoteListAdapter.VoteViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        return VoteViewHolder(
            ItemVoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class VoteViewHolder(
        private val binding: ItemVoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: VoteItem) {
            binding.tvNickname.text = item.nickname
            binding.tvReason.text = item.reason
            binding.tvDate.text = toFormattedDateString(item.timestamp)
        }

        private fun toFormattedDateString(time: Long): String {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
            val date = Date(time) // Long 타임스탬프를 Date 객체로 변환
            return dateFormat.format(date)
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<VoteItem>() {
            override fun areItemsTheSame(oldItem: VoteItem, newItem: VoteItem): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: VoteItem, newItem: VoteItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}