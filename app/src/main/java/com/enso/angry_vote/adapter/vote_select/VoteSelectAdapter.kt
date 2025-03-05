package com.enso.angry_vote.adapter.vote_select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.enso.angry_vote.databinding.ItemVoteSelectBinding
import com.enso.angry_vote.model.VoteSelectItem

class VoteSelectAdapter : ListAdapter<VoteSelectItem, VoteSelectAdapter.VoteSelectViewHolder>(diffUtil) {
    private var listener: OnVoteSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteSelectViewHolder {
        return VoteSelectViewHolder(
            ItemVoteSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: VoteSelectViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class VoteSelectViewHolder(
        private val binding: ItemVoteSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener?.onClick(getItem(adapterPosition))
                }
            }
        }
        fun onBind(item: VoteSelectItem) {
            binding.tvContent.text = "${item.nickname} (${item.voteCount})"
        }
    }

    fun setOnVoteSelectListener(listener: OnVoteSelectListener) {
        this.listener = listener
    }

    interface OnVoteSelectListener {
        fun onClick(item: VoteSelectItem)
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<VoteSelectItem>() {
            override fun areItemsTheSame(oldItem: VoteSelectItem, newItem: VoteSelectItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: VoteSelectItem, newItem: VoteSelectItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}