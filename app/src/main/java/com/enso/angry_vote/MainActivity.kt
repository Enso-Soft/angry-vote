package com.enso.angry_vote

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.enso.angry_vote.adapter.vote_list.VoteListAdapter
import com.enso.angry_vote.adapter.vote_select.VoteSelectAdapter
import com.enso.angry_vote.base.BaseActivity
import com.enso.angry_vote.databinding.ActivityMainBinding
import com.enso.angry_vote.ext.toVoteList
import com.enso.angry_vote.ext.toVoteSelectList
import com.enso.angry_vote.model.Vote
import com.enso.angry_vote.model.VoteSelectItem
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.Calendar

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    // 상수 정의
    companion object {
        private const val FIREBASE_URL = "https://angry-vote-default-rtdb.asia-southeast1.firebasedatabase.app/"
        private const val VOTES_REF = "votes"
        private const val VOTE_SELECT_REF = "vote_select"
        private const val EMPTY_REASON_MESSAGE = "사유를 적어주십쇼"
        private const val VOTE_SUCCESS_MESSAGE = "%s님 한표 드림!"
        private const val VOTE_FAILURE_MESSAGE = "투표 실패 했는걸?"
    }

    private val database: FirebaseDatabase by lazy { Firebase.database(FIREBASE_URL) }
    private val votesRef by lazy { database.getReference(VOTES_REF) }
    private val voteSelectRef by lazy { database.getReference(VOTE_SELECT_REF) }

    private val voteSelectAdapter by lazy { VoteSelectAdapter() }
    private val voteListAdapter by lazy { VoteListAdapter() }

    private var isBindVote: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUi()
        subscribeVoteSelect()
        initListener()
    }

    private fun setupUi() {
        with(binding) {
            rvVoteSelect.apply {
                layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = voteSelectAdapter
                (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            }

            rvVoteList.apply {
                layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                adapter = voteListAdapter
            }
        }
    }

    private fun subscribeThisWeekVotes() {
        val (startOfWeek, endOfWeek) = calculateWeekTimeRange()

        // 쿼리 실행
        votesRef
            .orderByChild("timestamp")
            .startAt(startOfWeek.toDouble())
            .endAt(endOfWeek.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val weeklyVotes = snapshot.toVoteList()

                    // 데이터 최신순 정렬 후 목록 업데이트
                    voteListAdapter.submitList(
                        weeklyVotes.sortedByDescending { it.timestamp }
                    ) {
                        binding.rvVoteList.scrollToPosition(0)
                    }

                    // 투표 항목 카운트 업데이트
                    updateVoteSelectItemCounts(weeklyVotes)
                }

                override fun onCancelled(error: DatabaseError) {
                    // 에러 처리 추가
                    Toast.makeText(this@MainActivity, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun calculateWeekTimeRange(): Pair<Long, Long> {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfWeek = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return Pair(startOfWeek, endOfWeek)
    }

    private fun updateVoteSelectItemCounts(weeklyVotes: List<com.enso.angry_vote.model.VoteItem>) {
        val updatedList = voteSelectAdapter.currentList.map { voteSelect ->
            voteSelect.copy(
                voteCount = weeklyVotes.count { it.id == voteSelect.id }
            )
        }
        setVoteSelectItemList(updatedList)
    }

    private fun subscribeVoteSelect() {
        voteSelectRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val voteSelectList = snapshot.toVoteSelectList()
                val currentVoteSelectItems = voteSelectAdapter.currentList

                val updatedItems = voteSelectList.map { voteSelect ->
                    VoteSelectItem(
                        id = voteSelect.id,
                        nickname = voteSelect.nickname,
                        voteCount = currentVoteSelectItems.find { it.id == voteSelect.id }?.voteCount ?: 0
                    )
                }

                setVoteSelectItemList(updatedItems)

                if (!isBindVote) {
                    isBindVote = true
                    subscribeThisWeekVotes()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리 추가
                Toast.makeText(this@MainActivity, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initListener() {
        voteSelectAdapter.setOnVoteSelectListener(object : VoteSelectAdapter.OnVoteSelectListener {
            override fun onClick(item: VoteSelectItem) {
                submitVote(item)
            }
        })
    }

    private fun submitVote(item: VoteSelectItem) {
        val reasonText = binding.etReason.text.toString().trim()
        if (reasonText.isBlank()) {
            Toast.makeText(this@MainActivity, EMPTY_REASON_MESSAGE, Toast.LENGTH_SHORT).show()
            return
        }

        votesRef
            .push()
            .setValue(
                Vote(
                    id = item.id,
                    nickname = item.nickname,
                    reason = reasonText,
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, String.format(VOTE_SUCCESS_MESSAGE, item.nickname), Toast.LENGTH_SHORT).show()
                binding.etReason.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, VOTE_FAILURE_MESSAGE, Toast.LENGTH_SHORT).show()
            }
    }

    private fun setVoteSelectItemList(list: List<VoteSelectItem>) {
        voteSelectAdapter.submitList(list)
    }
}