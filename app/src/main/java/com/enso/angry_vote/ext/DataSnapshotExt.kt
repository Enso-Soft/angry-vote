package com.enso.angry_vote.ext

import com.enso.angry_vote.model.Vote
import com.enso.angry_vote.model.VoteSelect
import com.enso.angry_vote.model.VoteItem
import com.google.firebase.database.DataSnapshot

/**
 * DataSnapshot을 VoteItem 리스트로 변환
 */
fun DataSnapshot.toVoteList(): List<VoteItem> {
    return children.mapNotNull { voteSnapshot ->
        voteSnapshot.getValue(Vote::class.java)?.let { vote ->
            VoteItem(
                uid = voteSnapshot.key ?: generateFallbackKey(vote),
                id = vote.id,
                nickname = vote.nickname,
                reason = vote.reason,
                timestamp = vote.timestamp
            )
        }
    }
}

/**
 * 키가 null인 경우 대체 키 생성
 */
private fun generateFallbackKey(vote: Vote): String {
    return "${vote.id}_${vote.timestamp}"
}

/**
 * DataSnapshot을 VoteSelect 리스트로 변환
 */
fun DataSnapshot.toVoteSelectList(): List<VoteSelect> {
    return children.mapNotNull { childSnapshot ->
        val id = childSnapshot.key ?: return@mapNotNull null
        val nickname = childSnapshot.getValue(String::class.java) ?: return@mapNotNull null
        VoteSelect(id = id, nickname = nickname)
    }
}